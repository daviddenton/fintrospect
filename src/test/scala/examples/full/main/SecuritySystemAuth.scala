package examples.full.main

import com.twitter.util.Future
import io.fintrospect.parameters._

object SecuritySystemAuth {

  /**
    * Although not strictly required, a bug in the Swagger UI (https://github.com/swagger-api/swagger-ui/issues/1593)
    * currently means this parameter should be added to module routes in order to make the Swagger actually include the
    * key in requests from the Swagger UI. This does not mean that you need to do any actual auth though - that is still handled
    * by Fintrospect itself.
    */
  val apiKey = Header.required.string("key")

  def apply() = ApiKey(apiKey, (key: String) => Future.value(key.equals("realSecret")))
}
