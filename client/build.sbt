import AssemblyKeys._

// put this at the top of the file

name := "MyFleetGirls"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "com.twitter" %% "finagle-http" % "6.24.0",
  "com.netaporter" %% "scala-uri" % "0.4.6",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.apache.httpcomponents" % "httpclient" % "4.4",
  "org.apache.httpcomponents" % "httpmime" % "4.4",
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "jul-to-slf4j" % "1.7.10",
  "org.apache.logging.log4j" % "log4j-core" % "2.2",
  "org.apache.logging.log4j" % "log4j-1.2-api" % "2.2",
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.2"
)

javaOptions   ++= Seq("-source", "1.7", "-target", "1.7")
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions")

assemblySettings

mainClass in assembly := Some("com.ponkotuy.run.Main")

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "com.ponkotuy.build"
