lazy val baseSettings = Seq(
  name := "fintrospect",
  organization := "io.fintrospect",
  version := "13.5.0",
  scalaVersion := "2.11.8",
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  scalacOptions := Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    //    "-unchecked",
    //    "-Yno-adapted-args",
    //    "-Ywarn-dead-code",
    //    "-Ywarn-numeric-widen",
    //    "-Xfuture",
    //    "-Xlint"
    "-feature"
  ),
  pomExtra :=
    <url>http://fintrospect.io</url>
      <scm>
        <url>git@github.com:daviddenton/fintrospect.git</url>
        <connection>scm:git:git@github.com:daviddenton/fintrospect.git</connection>
        <developerConnection>scm:git:git@github.com:daviddenton/fintrospect.git</developerConnection>
      </scm>
      <developers>
        <developer>
          <name>David Denton</name>
          <email>dev@fintrospect.io</email>
          <organization>fintrospect</organization>
          <organizationUrl>http://fintrospect.io</organizationUrl>
        </developer>
      </developers>,
  bintrayOrganization := Some("fintrospect"),
  credentials += Credentials(Path.userHome / ".sonatype" / ".credentials")
)

lazy val core = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-core")
  .settings(libraryDependencies ++= Seq(
    "net.sourceforge.argo" % "argo" % "3.19",
    "com.twitter" %% "finagle-http" % "6.36.0",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  ))
  .settings(description := "Implement fast, type-safe HTTP contracts for Finagle (aka Twitter RPC)")

// JSON libraries
lazy val argonaut = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-argonaut")
  .settings(description := "Argonaut JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4")

lazy val circe = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-circe")
  .settings(description := "Circe JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies ++= Seq("io.circe" %% "circe-core" % "0.4.1",
    "io.circe" %% "circe-generic" % "0.4.1",
    "io.circe" %% "circe-parser" % "0.4.1")
  )

lazy val gson = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-gson")
  .settings(description := "GSON JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies += "com.google.code.gson" % "gson" % "2.7")

lazy val json4s = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-json4s")
  .settings(description := "Json4S JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies ++= Seq("org.json4s" %% "json4s-native" % "3.3.0",
    "org.json4s" %% "json4s-jackson" % "3.3.0"))

lazy val play = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-play")
  .settings(description := "Play JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.3")

lazy val spray = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-spray")
  .settings(description := "Spray JSON library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies += "io.spray" %% "spray-json" % "1.3.2")

// Templating libraries
lazy val handlebars = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-handlebars")
  .settings(description := "Handlebars templating library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies += "com.gilt" %% "handlebars-scala" % "2.0.1")

lazy val mustache = project
  .settings(baseSettings)
  .settings(moduleName := "fintrospect-mustache")
  .settings(description := "Mustache templating library support for Fintrospect")
  .dependsOn(core % "compile->test")
  .settings(libraryDependencies ++= Seq("com.github.spullara.mustache.java" % "compiler" % "0.9.2",
    "com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.2"))

lazy val examples = project.in(file("."))
  .settings(baseSettings)
  .settings(unidocSettings: _*)
  .settings(moduleName := "fintrospect-examples")
  .aggregate(core, argonaut, circe, gson, json4s, play, spray, handlebars, mustache)
  .dependsOn(core, argonaut, circe, gson, json4s, play, spray, handlebars, mustache)
  .settings(libraryDependencies += "com.github.finagle" %% "finagle-oauth2" % "0.1.6")
