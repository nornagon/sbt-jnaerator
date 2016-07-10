import bintray.Keys._

name := "sbt-jnaerator"

sbtPlugin := true

version := "0.4.0"

organization := "net.nornagon"

scalaVersion in Global := "2.10.5"

scalacOptions in Compile ++= Seq("-deprecation", "-target:jvm-1.7")

libraryDependencies ++= Seq(
  "com.nativelibs4java" % "jnaerator" % "0.12"
)

homepage := Some(url("https://github.com/nornagon/sbt-jnaerator"))

licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
publishMavenStyle := false
bintrayPublishSettings
repository in bintray := "sbt-plugins"
bintrayOrganization in bintray := None

publishArtifact in Test := false
