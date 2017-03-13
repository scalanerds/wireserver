package com.scalanerds.wireserver.example.tcpClient

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.Response
import com.scalanerds.wireserver.tcpserver.Packet

class TcpClient(listener: ActorRef, remote: InetSocketAddress) extends Actor {

  import context.system

  connect()

  def receive: Receive = ready

  def ready: Receive = {
    case CommandFailed(_: Connect) =>
      println("Connection failed.")
      context stop self

    case Connected(_, _) =>
      println("Connect succeeded")
      val connection = sender()
      connection ! Register(self)
      context become listening(connection)
  }

  def listening(connection: ActorRef): Receive = {
    case Packet(_, data) =>
      connection ! Write(data)

    case data: ByteString =>
      listener ! Response(data)

    case CommandFailed(_: Write) =>
      listener ! "write failed"

    case Received(data) =>
      listener ! Response(data)

    case "close" =>
      listener ! "close"

    case "drop connection" =>
      connection ! Close
      context become ready
      connect()

    case _: ConnectionClosed =>
      listener ! "connection closed"
      context become ready
      connect()

    case PeerClosed =>
      context become ready
      connect()

    case msg => println(s"Something else is up.\n$msg")
  }

  def connect(): Unit = {
    println("Connecting client.")
    IO(Tcp) ! Connect(remote)
    println(s"Client connected to port ${remote.getPort}")
  }
}