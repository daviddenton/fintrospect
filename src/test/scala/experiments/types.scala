package experiments

import com.twitter.finagle.Filter
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import io.fintrospect.parameters.{Binding, PathBindable, PathParameter, Rebindable, Retrieval}
import io.fintrospect.util.Extractor

object types {
  type Filt = Filter[Request, Response, Request, Response]
  type ModifyPath = Path => Path
  type PathParam[T] = PathParameter[T] with PathBindable[T]
  type RqParam[T] = Retrieval[Request, T] with Extractor[Request, T] with Rebindable[Request, T, Binding]
}
