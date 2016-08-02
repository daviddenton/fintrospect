# request parameters & bodies
Fintrospect broadly abstracts the various parts of an HTTP Request ```Path```, ```Header```, ```Query``` and ```Body``` behind a common 
interface to which the main functions are:
1. Retrieve a valid value from an incoming request in a type-safe way using ```<--()``` or ```from()```
2. Bind a specified value into an outgoing request in a type-safe way using ```-->()``` or ```of()```

Parameters are created in a uniform way using the objects ```Path```, ```Header```, ```Query```, ```FormField``` and ```Body```. 
The general form for definition is as follows, although since ```Path``` and ```Body``` parameters are always required, the middle step is omitted: 
```
<parameter location class>.<required|optional>.<param type>("<name>")
```

Descriptions can be attached to these definitions for documentation purposes. Note the retrieved type for the optional param:
```
val anniversary = Header.required.localDate("anniversary", "the date you should not forget! format: yyyy-mm-dd")
val myAnniversary: LocalDate = age <-- request

val age = Header.optional.int("age", "your age")
val age: Option[Integer] = age <-- request

val ohDear = Body.xml("soapMessage", "This isn't Simple")
val ohDearDear: Elem = anniversary <-- request
```

There are convenience methods for a standard set of "primitive" types, plus extensions for such as native Scala XML, Forms and JSON.

Additionally, there is another form for parameters which can appear multiple times in a request - simply insert the ```*()``` method in the chain:
```
val kidsBirthdays = Query.required.*.localDate("birthdays", "the dates you should not forget! format: yyyy-mm-dd")
val ourKidsBirthdays: Seq[LocalDate] = kidsBirthdays <-- request
```

#### forms
These represent a slightly special case you first need to retrieve the form from the request, and then the fields from the form.
```
val name = FormField.required.string("name")
val isMarried = FormField.optional.boolean("married")
val form = Body.form(name, isMarried)
val myForm = form <-- request
val myName = name <-- myForm
val iAmMarried = isMarried <-- myForm
```

#### working with custom parameter types
Custom parameter and body types can be implemented by defining a ```ParameterSpec``` or ```BodySpec``` and passing an instance instead of calling the ```<param type>``` method 
in the form above. These Spec objects define:

- name and description of the entity type being handled
- for Parameters: the higher level ```ParamType``` of the on-the-wire representation. For custom formats, this is ```StringParamType```, although ```ObjectParamType```
should be used for JSON appearing in an request body
- for Bodies: the higher level ```ContentType```  of the on-the-wire representation. 
- functions representing the serialization and deserialization from the String format that comes in on the request. Note that we are only concerned 
with the happy case on-the-wire values. These throw exceptions if unsuccessful - these are caught by the request validation mechanism and turned into 
a rejected ```BadRequest``` (400) response which is returned to the caller.

An example for a simple domain case class Birthday:
```
case class Birthday(value: LocalDate)

object Birthday {
    def from(s: String) = Birthday(LocalDate.parse(s))
}

// 
val birthdayAsAQueryParam = Query.required(ParameterSpec[Birthday]("DOB", None, StringParamType, Birthday.from, _.toString))

/* Or, more simply, we can use map() on an existing ParameterSpec - this will handle the */

val birthdayAsAQueryParam = Query.required(ParameterSpec.localDate("DOB").map(Birthday(_), (b:Birthday) => b.value))

val birthdayAsABody = Body(BodySpec[Birthday](Option("DOB"), ContentTypes.TEXT_PLAIN, Birthday.from, _.toString))
```

#### usage of JSON libraries
Fintrospect comes with binding support for several JSON libraries, each represented by static instances of ```JsonFormat``` on a class named 
after the library in question. When defining a ```Body``` or ```Parameter```, if required you can also specify the library format for it to 
use (else it will default to the bundled Argo JSON library) - and if this is done centrally then you can switch out JSON libraries with only 
a single line of code change.

```
val jsonFormat = Argonaut.JsonFormat
val exampleObject = jsonFormat.obj("fieldName" -> json.string("hello"))
val json = Body.json(Option("my lovely JSON object"), exampleObject, jsonFormat)
val body: Json = json <-- request
```

Notice that in the above we specified an example of the JSON message. This is not mandatory, but allows the generation of 
<a href="http://json-schema.org/">JSON Schema</a> to be included in the auto-generated API documentation.

Additionally, in the case of some JSON libraries that provide auto marshalling and demarshalling to case class instances, you can remove the JSON step altogether:
```
case class Email(address: String)
val email = Body(jsonFormat.bodySpec[Email](Option("an email address")), Email("jim@example.com"), ObjectParamType)
val retrieved: Email = email <-- request
```

<a class="next" href="http://fintrospect.io/defining-routes"><button type="button" class="btn btn-sm btn-default">next: defining routes</button></a>
