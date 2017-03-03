package com.scalanerds.tcpserver

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Tcp}

object TcpServer {
  def props(handlerProps: HandlerProps, remote: InetSocketAddress , listener: ActorRef): Props =
    Props(classOf[TcpServer], handlerProps, remote, listener)
}

class TcpServer(handlerProps: HandlerProps, remote: InetSocketAddress, listener: ActorRef) extends Actor {

  import context.system
  println("Starting server")
  IO(Tcp) ! Tcp.Bind(self, remote)
  println(s"Listening on port ${remote.getPort} ...")

  override def receive: PartialFunction[Any, Unit] = {
    case Tcp.CommandFailed(_: Tcp.Bind) => context stop self

    case Tcp.Connected(_, _) =>
      val handler = context.actorOf(handlerProps.props(sender, listener))
      sender ! Tcp.Register(handler)

  }

}
