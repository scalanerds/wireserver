name := """wireserver"""

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.mongodb" % "bson" % "3.4.2",
  "com.typesafe.akka" %% "akka-stream" % "2.4.17",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.17",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
  exclude("org.scala-lang", "scala-reflect")
  exclude("org.scala-lang.modules", "scala-xml_2.12")
)