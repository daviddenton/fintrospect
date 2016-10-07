package experiments

import com.twitter.finagle.Filter
import experiments.types.{Filt, ModifyPath}
import io.fintrospect.ContentType
import io.fintrospect.parameters.Body

case class Terms(summary: String,
                 description: Option[String],
                 filter: Option[Filt] = None,
                 produces: Set[ContentType] = Set.empty,
                 consumes: Set[ContentType] = Set.empty,
                 body: Option[Body[_]] = None,
                 pathFn: ModifyPath = identity) {
  val useFilter: Filt = filter.getOrElse(Filter.identity)

  def addPath(next: String): Terms = copy(pathFn = pathFn.andThen(_ / next))

  def withFilter(filter: Filt): Terms = copy(filter = Option(filter))

  def consuming(contentTypes: Seq[ContentType]): Terms = copy(consumes = consumes ++ contentTypes)

  def producing(contentTypes: Seq[ContentType]): Terms = copy(produces = produces ++ contentTypes)

  def body[T](bp: Body[T]): Terms = copy(body = Option(bp), consumes = consumes + bp.contentType)
}
