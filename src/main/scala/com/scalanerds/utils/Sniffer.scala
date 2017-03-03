package com.scalanerds.utils

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString


case class Packet(data: ByteString)

class Sniffer extends Actor {
  var listener: ActorRef = _
  var connection: ActorRef = _

  def receive = {
    case conn: ActorRef => connection = conn
    case msg: String =>
      println(s"client got message $msg")
      sender ! "client got it!"

    case Packet(data) =>
      listener = sender()
      connection ! Write(data)

    case data: ByteString =>
      listener ! Packet(data)

    case CommandFailed(w: Write) =>
      listener ! "write failed"

    case Received(data) => {
      listener ! Packet(data)
    }

    case "close" =>
      connection ! Close

    case _: ConnectionClosed =>
      listener ! "connection closed"
      context stop self

    case PeerClosed => context stop self
  }
}

class TcpClient(remote: InetSocketAddress, handler: ActorRef) extends Actor {

  import context.system

  println("Connecting client.")
  IO(Tcp) ! Connect(remote)
  println(s"Client connected to port ${remote.getPort}")

  def receive = {

    case CommandFailed(_: Connect) =>
      println("Connection failed.")
      context stop self

    case c@Connected(_, _) =>
      println("Connect succeeded")
      val connection = sender()
      handler ! connection
      connection ! Register(handler)

    case _ => println("Something else is up.")
  }

}