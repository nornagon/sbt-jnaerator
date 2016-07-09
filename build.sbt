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

pomExtra := {
  <scm>
    <url>https://github.com/nornagon/jnaerator</url>
    <connection>scm:git:git@github.com:nornagon/sbt-jnaerator.git</connection>
  </scm>
  <developers>
    <developer>
      <id>timcharper</id>
      <name>Tim Harper</name>
      <url>http://timcharper.com</url>
    </developer>
    <developer>
      <id>nornagon</id>
      <name>Jeremy Apthorp</name>
      <url>http://nornagon.net</url>
    </developer>
  </developers>
}

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false
