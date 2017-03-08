package com.scalanerds

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import com.scalanerds.handlers.WireHandlerProps
import com.scalanerds.tcpserver.TcpServer
import com.scalanerds.utils.Sniffer

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("TestMessages")

    val sniffer = system.actorOf(Props(new Sniffer(new InetSocketAddress("localhost", 27017))), "sniffer")

    system.actorOf(TcpServer.props(WireHandlerProps, new InetSocketAddress("localhost", 3000),
      sniffer), "serverActor")
  }
}
