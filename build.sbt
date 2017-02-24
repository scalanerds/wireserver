name := """wireserver"""

version := "1.0"

scalaVersion := "2.12.1"

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.mongodb" % "bson" % "3.4.2"
  //"org.mongodb" %% "casbah" % "3.1.1"
 // "org.mongodb.scala" % "mongo-scala-bson_2.12" % "1.2.1"
//"org.mongodb.scala" % "mongo-scala-driver_2.12" % "1.2.1"
)