# request parameters and bodies

## defining
As can be seen previously, request parameters are created in a uniform way using the standardised objects Path, Header, Query, FormField and Body. The general form for definition is: ```<parameter location>.<required|optional>.<param type>("name")```
Since Path and Body parameters are always required, the middle step is omitted from this form for these types.

There are convenience methods for a standard set of "primitive" types, plus extensions for other common formats such as native Scala XML, Forms (body only) and JSON (more on this later).

## custom formats
These can be implemented by defining a ParameterSpec or BodySpec and passing this in instead of calling the ```<param type>``` method in the form above. These Spec objects define the serialization and deserialization mechanisms from the String format that comes in on the request. An example for a simple domain case class Birthday:
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
Note that in the above we are only concerned with the happy case on-the-wire values. The serialize and deserialize methods should throw exceptions if unsuccessful - these are caught by the request validation mechanism and turned into a rejected BadRequest (400) response which is returned to the caller.
