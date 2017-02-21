package com.mongonerds.tcpserver

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}

object TcpServer {
  def props(handlerProps: HandlerProps): Props =
    Props(classOf[TcpServer], handlerProps)
}

class TcpServer(handlerProps: HandlerProps) extends Actor {

  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress("localhost", 3000))

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

    case Tcp.Connected(_, _) =>
      val handler = context.actorOf(handlerProps.props(sender))
      sender ! Tcp.Register(handler)
  }

}
