package com.scalanerds.utils

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.scalanerds.tcpserver.Packet

class Sniffer(remote: InetSocketAddress) extends Actor {

  import context.system

  var listener: ActorRef = _
  println("Sniffer up")
  connect

  def receive = ready

  def ready: Receive = {
    case CommandFailed(_: Connect) =>
      println("Connection failed.")
      context stop self

    case c@Connected(_, _) =>
      println("Connect succeeded")
      val connection = sender()
      connection ! Register(self)
      context become listening(connection)
  }

  def listening(connection: ActorRef): Receive = {
    case Packet(data) =>
      listener = sender()
      connection ! Write(data)

    case data: ByteString =>
      listener ! Packet(data)

    case CommandFailed(w: Write) =>
      listener ! "write failed"

    case Received(data) =>
      listener ! Packet(data)

    case "close" =>
      listener ! "close"

    case "close mongod" =>
      connection ! Close
      context become ready
      connect

    case msg: String =>
      println(s"client received message:  $msg")
      sender ! "client got it!"

    case _: ConnectionClosed =>
      listener ! "connection closed"
      context become ready
      connect

    case PeerClosed =>
      context become ready
      connect

    case _ => println("Something else is up.")
  }

  def connect: Unit = {
    println("Connecting client.")
    IO(Tcp) ! Connect(remote)
    println(s"Client connected to port ${remote.getPort}")
  }
}