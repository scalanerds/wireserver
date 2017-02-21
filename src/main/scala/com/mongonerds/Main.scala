package com.mongonerds

import akka.actor.ActorSystem
import com.mongonerds.handlers.WireHandlerProps
import com.mongonerds.tcpserver.TcpServer

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("server")
    val service = system.actorOf(TcpServer.props(WireHandlerProps), "ServerActor")
  }
}
