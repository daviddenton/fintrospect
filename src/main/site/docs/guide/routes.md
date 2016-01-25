# routes
A RouteSpec object defines the specification of the contract and the API follows the immutable builder pattern. Apart from the path elements (which terminate the builder), all of the "builder-y" calls here are optional, as are the descriptive strings (we'll see how they are used later). Here's the simplest possible REST-like example for getting all employees in a system:

```
RouteSpec("list all employees").at(Method.Get) / "employee"
```

Notice that the request in that  example was completely static? If we want an example of a dynamic endpoint, such as listing all users in a particular numerically-identified department, then we can introduce a Path parameter:
```
  RouteSpec("list all employees in a particular group").at(Method.Get) / "employee" / Path.integer("departmentId")
```
... and we can do the same for Header and Query parameters; both optional and mandatory parameters are supported, as are parameters that can appear multiple times.:
```
RouteSpec("list all employees in a particular group")
    .taking(Header.optional.boolean("listOnlyActive"))
    .taking(Query.required.*.localDate("datesTakenAsHoliday"))
    .at(Method.Get) / "employee" / Path.integer("departmentId")
```
Moving onto HTTP bodies - for example adding an employee via a HTTP Post and declaring the content types that we produce (although this is optional):

```
RouteSpec("add employee", "Insert a new employee, failing if it already exists")
    .producing(ContentTypes.TEXT_PLAIN)
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .at(Method.Post) / "user" / Path.integer("departmentId")
```
  ... or via a form submission and declaring possible responses:
```
RouteSpec("add user", "Insert a new employee, failing if it already exists")
    .body(Body.form(FormField.required.string("name"), FormField.required.localDate("dateOfBirth")))
    .returning(Created -> "Employee was created")
    .returning(Conflict -> "Employee already exists")
    .at(Method.Post) / "user" / Path.integer("departmentId")
```
