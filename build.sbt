lazy val baseSettings = Seq(
  name := "fintrospect-13",
  organization := "io.fintrospect",
  version := "0.0.5",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
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
  libraryDependencies := {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor >= 11 =>
        libraryDependencies.value ++ Seq(
          "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
          "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3")
      case _ => libraryDependencies.value
    }
  },
  libraryDependencies ++= Seq(
    "net.sourceforge.argo" % "argo" % "3.12",
    "com.twitter" %% "finagle-http" % "6.35.0",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test"
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
  bintrayOrganization := Some("fintrospect")
)

lazy val allSettings = baseSettings

lazy val core = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-core")
  .settings(description := "Implement fast, type-safe HTTP contracts for Finagle (aka Twitter RPC)")

// JSON libraries
lazy val argonaut = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-argonaut")
  .settings(description := "Argonaut JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4")

lazy val circe = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-circe")
  .settings(description := "Circe JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq("io.circe" %% "circe-core" % "0.4.1",
    "io.circe" %% "circe-generic" % "0.4.1",
    "io.circe" %% "circe-parser" % "0.4.1")
  )

lazy val gson = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-spray")
  .settings(description := "GSON JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies += "com.google.code.gson" % "gson" % "2.5")

lazy val json4s = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-json4s")
  .settings(description := "Json4S JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq("org.json4s" %% "json4s-native" % "3.3.0",
    "org.json4s" %% "json4s-jackson" % "3.3.0"))

lazy val play = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-play")
  .settings(description := "Play JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies += "com.typesafe.play" %% "play-json" % "2.4.3")

lazy val spray = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-spray")
  .settings(description := "Spray JSON library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies += "io.spray" %% "spray-json" % "1.3.2")

// Templating libraries
lazy val handlebars = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-handlebars")
  .settings(description := "Handlebars templating library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies += "com.gilt" %% "handlebars-scala" % "2.0.1")

lazy val mustache = project
  .settings(allSettings)
  .settings(moduleName := "fintrospect-mustache")
  .settings(description := "Mustache templating library support for Fintrospect")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq("com.github.spullara.mustache.java" % "compiler" % "0.9.1",
    "com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.1"))

lazy val examples = project.in(file("."))
  .settings(allSettings)
  .settings(moduleName := "fintrospect-main")
  .settings(libraryDependencies += "com.github.finagle" %% "finagle-oauth2" % "0.1.6")
  .settings(libraryDependencies += "com.google.code.gson" % "gson" % "2.5")
  .dependsOn(core, argonaut, circe, gson, json4s, play, spray, handlebars, mustache)
