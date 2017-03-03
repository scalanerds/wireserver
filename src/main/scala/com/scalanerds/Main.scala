package com.scalanerds

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import com.scalanerds.handlers.WireHandlerProps
import com.scalanerds.tcpserver.TcpServer
import com.scalanerds.utils.{Sniffer, TcpClient}

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("TestMessages")
    val clientHandler = system.actorOf(Props[Sniffer])
    system.actorOf(Props(new TcpClient(new InetSocketAddress("localhost", 27017), clientHandler)), "clientActor")

    system.actorOf(TcpServer.props(WireHandlerProps, new InetSocketAddress("localhost", 3000),
      clientHandler), "serverActor")
  }
}
