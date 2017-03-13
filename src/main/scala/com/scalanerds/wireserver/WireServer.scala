package com.scalanerds.wireserver

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import com.scalanerds.wireserver.handlers.HandlerProps
import com.scalanerds.wireserver.tcpserver.TcpServer

class WireServer(val props: HandlerProps,
                 val connection: InetSocketAddress,
                 val name: String = "wireSystem") {
  val system = ActorSystem(name)
  val wireServer: ActorRef = system.actorOf(TcpServer.props(props, connection), "wireServer")
}

object WireServer {
  def apply(handler: HandlerProps,
            address: String = "localhost",
            port: Int = 3000,
            name: String = "wireSystem") = new WireServer(
    handler, new InetSocketAddress(address, port), name
  )
}