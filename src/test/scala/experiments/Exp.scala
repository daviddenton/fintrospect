package experiments

import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finagle.{Filter, Service}
import com.twitter.util.Future
import experiments.types.{Filt, ModifyPath, PathParam, RqParam}
import io.fintrospect.ContentType
import io.fintrospect.parameters.{Binding, Body, PathBindable, PathParameter, Rebindable, Retrieval, Path => FPath}
import io.fintrospect.util.Extractor

object types {
  type Filt = Filter[Request, Response, Request, Response]
  type ModifyPath = Path => Path
  type PathParam[T] = PathParameter[T] with PathBindable[T]
  type RqParam[T] = Retrieval[Request, T] with Extractor[Request, T] with Rebindable[Request, T, Binding]
}

trait PathBuilder[RqParams, T <: Request => RqParams] {
}

abstract class SR(method: Method, pathFn: Path => Path) {
  def toPf(basePath: Path): PartialFunction[(Method, Path), Service[Request, Response]]

  def matches(actualMethod: Method, basePath: Path, actualPath: Path) = actualMethod == method && actualPath == pathFn(basePath)
}

case class PathBuilder0[Base](method: Method, contents: ContractContents, extract: Request => Base) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(contents = contents.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder1(method, contents, extract, next)

  def bindTo(fn: Base => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path => contents.useFilter.andThen(Service.mk[Request, Response] {
        (req: Request) => fn(extract(req))
      })
    }
  }
}

case class PathBuilder1[Base, PP0](method: Method, contents: ContractContents, extract: Request => Base, pp0: PathParameter[PP0]) extends PathBuilder[Base, Request => Base] {

  def /(next: String) = copy(contents = contents.addPath(next))

  def /[NEXT](next: PathParam[NEXT]) = PathBuilder2(method, contents, extract, pp0, next)

  def bindTo(fn: (PP0, Base) => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, extract(req))
      }
    }
  }
}

case class PathBuilder2[Base, PP0, PP1](method: Method, contents: ContractContents, extract: Request => Base, pp0: PathParameter[PP0], pp1: PathParameter[PP1]) extends PathBuilder[Base, Request => Base] {
  def bindTo(fn: (PP0, PP1, Base) => Future[Response]) = new SR(method, identity) {
    override def toPf(basePath: Path) = {
      case actualMethod -> path / pp0(s1) / pp1(s2) => Service.mk[Request, Response] {
        (req: Request) => fn(s1, s2, extract(req))
      }
    }
  }
}

case class ContractContents(
                             description: Option[String] = None,
                             filter: Option[Filt] = None,
                             produces: Set[ContentType] = Set.empty,
                             consumes: Set[ContentType] = Set.empty,
                             body: Option[Body[_]] = None,
                             pathFn: ModifyPath = identity) {
  val useFilter: Filt = filter.getOrElse(Filter.identity)

  def addPath(next: String): ContractContents = copy(pathFn = pathFn.andThen(_ / next))

  def withFilter(filter: Filt): ContractContents = copy(filter = Option(filter))

  def consuming(contentTypes: Seq[ContentType]): ContractContents = copy(consumes = consumes ++ contentTypes)

  def producing(contentTypes: Seq[ContentType]): ContractContents = copy(produces = produces ++ contentTypes)

  def body[T](bp: Body[T]): ContractContents = copy(body = Option(bp), consumes = consumes + bp.contentType)
}

abstract class Contract(rps: RqParam[_]*) {
  val params: Seq[RqParam[_]] = rps
}

case class Contract0(private val contents: ContractContents = ContractContents())
  extends Contract() {

  def taking[NEXT](next: RqParam[NEXT]): Contract1[NEXT] = Contract1(contents, next)

  def withFilter(filter: Filt) = copy(contents.withFilter(filter))

  def consuming(contentTypes: ContentType*) = copy(contents.consuming(contentTypes))

  def producing(contentTypes: ContentType*) = copy(contents.producing(contentTypes))

  def body[BODY](next: Body[BODY]) = Contract1(contents.body(next), next)

  def at(method: Method) = new PathBuilder0(method, contents, identity)
}

case class Contract1[RP0](private val contents: ContractContents = ContractContents(), private val rp0: RqParam[RP0])
  extends Contract(rp0) {

  def taking[NEXT](next: RqParam[NEXT]) = Contract2(contents, rp0, next)

  def withFilter(filter: Filt) = copy(contents.withFilter(filter))

  def consuming(contentTypes: ContentType*) = copy(contents.consuming(contentTypes))

  def producing(contentTypes: ContentType*) = copy(contents.producing(contentTypes))

  def body[BODY](next: Body[BODY]) = Contract2(contents.body(next), rp0, next)

  def at(method: Method) = new PathBuilder0(method, contents, req => (rp0.from(req), req))
}

case class Contract2[RP0, RP1](private val contents: ContractContents = ContractContents(), private val rp0: RqParam[RP0], private val rp1: RqParam[RP1])
  extends Contract(rp0, rp1) {

  def withFilter(filter: Filt) = copy(contents.withFilter(filter))

  def consuming(contentTypes: ContentType*) = copy(contents.consuming(contentTypes))

  def producing(contentTypes: ContentType*) = copy(contents.producing(contentTypes))

  def at(method: Method) = new PathBuilder0(method, contents, req => (rp0.from(req), rp1.from(req), req))
}


