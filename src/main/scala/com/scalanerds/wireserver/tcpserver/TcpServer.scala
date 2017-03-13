package com.scalanerds.wireserver.tcpserver

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.Tcp.{Bind, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.handlers.HandlerProps

// Packet to send messages to another actor eg. TcpClient
case class Packet(msg: String, data: ByteString)

object TcpServer {
  def props(handlerProps: HandlerProps, remote: InetSocketAddress): Props =
    Props(classOf[TcpServer], handlerProps, remote)
}

class TcpServer(handlerProps: HandlerProps, remote: InetSocketAddress) extends Actor {

  import context.system

  println("Starting server")
  IO(Tcp) ! Bind(self, remote)
  println(s"Listening on port ${remote.getPort} ...")

  override def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(_: Tcp.Bind) => context stop self

    case Connected(_, _) =>
      val handler = context.actorOf(handlerProps.props(sender))
      sender ! Register(handler)
  }
}
