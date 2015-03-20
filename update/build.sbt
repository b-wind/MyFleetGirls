import AssemblyKeys._

// put this at the top of the file

name := "MyFleetGirlsUpdate"

crossPaths := false

autoScalaLibrary := false

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
)

assemblySettings

mainClass in assembly := Some("Main")
