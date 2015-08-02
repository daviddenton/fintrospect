
val orgName = "io.github.daviddenton"

val projectName = "fintrospect"

organization := orgName

name := projectName

description := "Library that adds self-documentation to Finagle server endpoint services"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", "2.11.5")

scalacOptions += "-deprecation"

scalacOptions += "-feature"

libraryDependencies ++= Seq(
  "net.sourceforge.argo" % "argo" % "3.12",
  "com.twitter" %% "finagle-http" % "6.27.0" % "provided",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test")

licenses +=("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))

pomExtra :=
  <url>http://{projectName}.github.io/</url>
    <scm>
      <url>git@github.com:daviddenton/{projectName}.git</url>
      <connection>scm:git:git@github.com:daviddenton/{projectName}.git</connection>
      <developerConnection>scm:git:git@github.com:daviddenton/{projectName}.git</developerConnection>
    </scm>
    <developers>
      <developer>
        <name>David Denton</name>
        <email>dev@fintrospect.io</email>
        <organization>{projectName}</organization>
        <organizationUrl>http://fintrospect.io</organizationUrl>
      </developer>
    </developers>

Seq(bintraySettings: _*)
