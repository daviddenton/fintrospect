import com.twitter.finagle.http.Method.Get
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}
import io.fintrospect.{Module, ModuleSpec, RouteSpec, SwaggerUiModule}

object SwaggerUiApp extends App {

  private val bizModule = ModuleSpec(Root / "bob", Swagger2dot0Json(ApiInfo("asd", "asd"))).withRoute(RouteSpec().at(Get) / "foo" bindTo (Service.mk { re: Request => ??? }))

  Http.serve(":9000", Module.toService(Module.combine(bizModule, SwaggerUiModule(bizModule))))
  Thread.currentThread().join()
}
