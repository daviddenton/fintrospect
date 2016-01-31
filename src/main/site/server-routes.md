# server routes & modules
A RouteSpec needs to be bound to a standard Finagle Service to receive requests. Since these are very lightweight, we can create a new 
instance of the Service for every request, and bind the RouteSpec to a factory method which receives the dynamic ```Path``` parameters 
and returns the Service. Other parameters can be retrieved directly in a type-safe manner from the HTTP request by using ```<--()``` or 
```from()``` method on the parameter declaration.

#### validation
The presence validity of ALL parameters which are attached to a ```RouteSpec``` is verified by Fintrospect before requests make it to 
this bound ```Service```, so no validation code is required. The response returned to the client is:

- if there are any ```Path``` params which are missing or invalid (all are required): ```Not Found 404```
- if there are any ```Header```, ```Query```, or ```Body``` params are missing (required only) or invalid: ```Bad Request 400```

### simple example
```
val holidays = Query.required.*.localDate("datesTakenAsHoliday")
val includeManagement = Header.optional.boolean("includeManagement")

def findEmployeesOnHoliday(departmentId: Integer) = Service.mk[Request, Response] {
    request =>
        val holidayDates: Seq[LocalDate] = holidays <-- request
        val includeManagementFlag: Option[Boolean] = includeManagement <-- request
        val response = Response(Ok)
        val baseMsg = s"Everyone from department $departmentId was at work on $holidayDates"
        response.contentString = baseMsg + (if (includeManagementFlag.getOrElse(false)) "" else ", even the management") Future.value(response)
}

RouteSpec()
    .taking(holidays)
    .taking(includeManagement)
    .at(Method.Get) / "employee" / Path.integer("departmentId") bindTo findEmployeesOnHoliday
```

### modules
A Module is a collection of Routes that share a common URL context, which is built up from the ```Root``` object. Add the routes and 
then convert into a standard Finagle Service object which is then attached in the normal way to an HTTP server.
```
def listEmployees(): Service[Request, Response] = Service.mk(req => Future.value(Response()))

Http.serve(":8080",
    ModuleSpec(Root / "employee")
      .withRoute(RouteSpec("lists all employees").at(Method.Get) bindTo listEmployees)
      .toService
)

```
Modules with different root contexts can also be combined with one another and then converted to a Service:
```
Module.toService(ModuleSpec(Root / "a").combine(ModuleSpec(Root / "b")))
```

#### self-describing Module APIs
A big feature of the Fintrospect library is the ability to generate API documentation at runtime. This can be activated by passing 
in a ModuleRenderer implementation when creating the ModuleSpec and when this is done, a new endpoint is created at the root of the 
module context (overridable) which serves this documentation. Bundled with Fintrospect are Swagger (1.1 and 2.0) JSON and a simple 
JSON format. Other implementations are pluggable by implementing the ```ModuleRenderer```  trait - see the example code for a simple 
XML implementation.
```
ModuleSpec(Root / "employee", Swagger2dot0Json(ApiInfo("an employee discovery API", "3.0")))
```

#### security
Module routes can secured by adding an implementation of the ```Security``` trait - this essentially provides a filter through which 
all requests will be passed. An ApiKey implementation is bundled with the library which return an ```401 Unauthorized``` HTTP response code 
when a request does not pass authentication.
```
ModuleSpec(Root / "employee").securedBy(ApiKey(Header.required.string("api_key"), (key: String) => Future.value(key == "extremelySecretThing")))
```
