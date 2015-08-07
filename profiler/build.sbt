
scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.ceedubs" %% "ficus" % "1.1.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "com.typesafe.play" %% "play-test" % play.core.PlayVersion.current
)

Keys.fork in Keys.run := true

javaOptions in Keys.run ++= Seq(
  "-agentlib:hprof=cpu=samples,depth=80,interval=2"
)
