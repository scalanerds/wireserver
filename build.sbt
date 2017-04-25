name := """wireserver"""

version := "1.0"

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.0",
  "org.mongodb" % "bson" % "3.4.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.0",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  exclude("org.scala-lang", "scala-reflect")
)