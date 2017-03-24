package com.scalanerds.wireserver.example

import akka.actor.ActorSystem
import com.scalanerds.wireserver.example.handler.SnifferServerProps
import com.scalanerds.wireserver.tcpserver.{PlainTcpServer, SSLTcpServer}

object Main extends App {

  val system = ActorSystem()
  system.actorOf(PlainTcpServer.props(SnifferServerProps.props))
  system.actorOf(SSLTcpServer.props(SnifferServerProps.props))

}
