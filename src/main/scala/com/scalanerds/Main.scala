package com.scalanerds

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import com.scalanerds.handlers.{WireHandlerPropsSniffer}
import com.scalanerds.tcpserver.TcpServer
import com.scalanerds.utils.Sniffer

object Main {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("TestMessages")

    val sniffer = system.actorOf(Props(new Sniffer(new InetSocketAddress("localhost", 27017))), "sniffer")
    system.actorOf(TcpServer.props(new WireHandlerPropsSniffer(sniffer) , new InetSocketAddress("localhost", 3000)), "serverActor")
  }
}
