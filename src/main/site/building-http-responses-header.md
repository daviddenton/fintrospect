# building http responses
It's all very well being able to extract pieces of data from HTTP requests, but that's only half the story - we also want to be able to easily build responses. Fintrospect comes bundled with a extensible set of HTTP Response Builders to do this. The very simplest way is by using a ResponseBuilder object directly...
```
ResponseBuilder.toFuture(
    ResponseBuilder.HttpResponse(ContentTypes.APPLICATION_JSON).withCode(Status.Ok).withContent("some text").build()
)
```

However, this only handles Strings and Buffer types directly. Also bundled are a set of bindings which provide ResponseBuilders for 
handling content types like JSON or XML in a set of popular OSS libraries. These live in the ```io.fintrospect.formats``` package. Currently supported formats are in the table below:
