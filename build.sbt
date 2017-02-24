name := """wireserver"""

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.mongodb" % "mongo-java-driver" % "3.4.2"
)