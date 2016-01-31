# request parameters and bodies
Fintrospect broadly abstracts the various parts of an HTTP Request ```Path```, ```Header```, ```Query``` and ```Body``` behind a common 
interface to which the main functions are:
1. Retrieve a named value from an incoming request in a typesafe way (using ```<--()``` or ```from()```)
2. Bind a named value into an outgoing request in a typesafe way (using ```-->()``` or ```of()```)

Request parameters are created in a uniform way using the standardised objects ```Path```, ```Header```, ```Query```, ```FormField``` and ```Body```. 
The general form for definition is as follows, although since ```Path``` and ```Body``` parameters are always required, the middle step is omitted: 
```
    <parameter location class>.<required|optional>.<param type>("<name>")
```

Descriptions can be attached to these definitions for documentation purposes. Note the type for the optional param:
```
    val anniversary = Query.required.localDate("anniversary", "the date you should not forget! format: yyyy-mm-dd")
    val myAnniversary: LocalDate = anniversary <-- request
    
    val age = Header.optional.int("age", "your age")
    val age: Option[Integer] = age <-- request

    val age = Body.optional.int("age", "your age")
    val age: Option[Integer] = age <-- request
```

There are convenience methods for a standard set of "primitive" types, plus extensions for such as native Scala XML, Forms (body only) and JSON.

#### forms
These represent a slightly special case you first need to retrieve the form from the request, and then the fields from the form.
```
    val name = FormField.required.string("name")
    val isMarried = FormField.optional.boolean("married")
    val form = Body.form(name, isMarried)
    val myForm = form <-- request
    val myName = name <-- formInstance
    val iAmMarried = isMarried <-- formInstance
```

### custom formats
These can be implemented by defining a ```ParameterSpec``` or ```BodySpec``` and passing this in instead of calling the ```<param type>``` method 
in the form above. These Spec objects define the serialization and deserialization mechanisms from the String format that comes in on the 
request. An example for a simple domain case class Birthday:
```
case class Birthday(value: LocalDate) {
   override def toString = value.toString
}

object Birthday {
    def from(s: String) = Birthday(LocalDate.parse(s))
}

val birthdayAsAQueryParam = Query.required(ParameterSpec[Birthday]("DOB", None, StringParamType, Birthday.from, _.toString))

val birthdayAsABody = Body(BodySpec[Birthday](Option("DOB"), ContentTypes.TEXT_PLAIN, Birthday.from, _.toString))
```
Note that in the above we are only concerned with the happy case on-the-wire values. The serialize and deserialize methods should 
throw exceptions if unsuccessful - these are caught by the request validation mechanism and turned into a rejected ```BadRequest``` (400) 
response which is returned to the caller.
