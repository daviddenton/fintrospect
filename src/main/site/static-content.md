# static content
Static files can easily be served from the classpath by using an instance of ```StaticModule```:
```
val publicModule = StaticModule(Root / "public", "classpathPathOfStaticContent")
```

<a class="next" href="http://fintrospect.io/testing"><button type="button" class="btn btn-sm btn-default">next: testing</button></a>
