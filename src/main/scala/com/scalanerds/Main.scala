package com.scalanerds

import akka.actor.ActorSystem
import com.scalanerds.handlers.WireHandlerProps
import com.scalanerds.tcpserver.TcpServer

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("server")
    val service = system.actorOf(TcpServer.props(WireHandlerProps), "ServerActor")
  }
}
