package com.scalanerds.wireserver.tcpserver

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.event.Logging
import akka.io.Tcp.{Bind, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.handlers.HandlerProps
import com.scalanerds.wireserver.messages.{GetPort, Port}

// Packet to send messages to another actor eg. TcpClient
case class Packet(msg: String, data: ByteString)

object TcpServer {
  def props(handlerProps: HandlerProps, remote: InetSocketAddress): Props =
    Props(classOf[TcpServer], handlerProps, remote)
}

class TcpServer(handlerProps: HandlerProps, socket: InetSocketAddress) extends Actor {

  import context.system
  val log = Logging(context.system, this)

  println("Starting server")
  IO(Tcp) ! Bind(self, socket)
  println(s"Listening on port ${socket.getPort} ...")

  override def receive: PartialFunction[Any, Unit] = {
    case CommandFailed(_: Tcp.Bind) => context stop self

    case Connected(_, _) =>
      context.actorOf(handlerProps.props(sender))
      log.debug("Walter is born " + sender.path)


    case GetPort =>
      sender ! Port(number = socket.getPort)
  }
}
