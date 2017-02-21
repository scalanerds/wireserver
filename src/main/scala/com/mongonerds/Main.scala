package com.mongonerds

import akka.actor.ActorSystem
import com.mongonerds.handlers.EchoHandlerProps
import com.mongonerds.tcpserver.TcpServer

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello, world!")
    val system = ActorSystem("server")
    val service = system.actorOf(TcpServer.props(EchoHandlerProps), "ServerActor")
  }
}
