// put this at the top of the file

name := "MyFleetGirlsUpdate"

crossPaths := false

autoScalaLibrary := false

libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.12",
    "ch.qos.logback" % "logback-classic" % "1.1.2"
)

mainClass in assembly := Some("Main")

licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.html"))

homepage := Some(url("https://myfleet.moe"))
