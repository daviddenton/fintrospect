
val orgName = "io.github.daviddenton"

val projectName = "fintrospect"

organization := orgName

name := projectName

description := "Library that adds self-documentation to Finagle server endpoint services"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.6", "2.11.7")

scalacOptions += "-deprecation"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "net.sourceforge.argo" % "argo" % "3.12",
  "io.spray" %% "spray-json" % "1.3.2" % "provided",
  "io.argonaut" %% "argonaut" % "6.0.4" % "provided",
  "com.typesafe.play" %% "play-json" % "2.4.3" % "provided",
  "org.json4s" %% "json4s" % "3.2.11" % "provided",
  "org.json4s" %% "json4s-native" % "3.2.11" % "provided",
  "org.json4s" %% "json4s-jackson" % "3.2.11" % "provided",
  "com.twitter" %% "finagle-http" % "6.30.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

licenses +=("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

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
    </developers>

Seq(bintraySettings: _*)
