# static content
Static files can easily be served from the either the Classpath or a Directory by using an instance of ```StaticModule``` with an 
appropriately injected ```ResourceLoader```:
```
val cpModule = StaticModule(Root / "public", ResourceLoader.Classpath("package/path"))
val dirModule = StaticModule(Root / "public", ResourceLoader.Directory("file/dir/path"))
```

Note that due to security concerns, `ResourceLoader.Classpath` should be configured to use a package which does NOT overlap with class assets.

<a class="next" href="http://fintrospect.io/testing"><button type="button" class="btn btn-sm btn-default">next: testing</button></a>
