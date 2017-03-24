package com.scalanerds.wireserver.example.tcpClient

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.tcpserver.TcpBuffer

class TcpClient(listener: ActorRef, remote: InetSocketAddress) extends Actor with TcpBuffer {

  import context.system

  val log = Logging(context.system, this)
  var connection: ActorRef = _

  override def preStart(): Unit = {
    connect()
    super.preStart()
  }

  override def postStop(): Unit = {
    connection ! Close
    super.postStop()
  }

  def receive: Receive = ready

  def ready: Receive = {
    case CommandFailed(_: Connect) =>
      log.debug("Eve connection failed.")
      context stop self

    case Connected(_, _) =>
      log.debug("Eve connection succeeded")
      connection = sender()
      connection ! Register(self)
//      listener ! Ready

      context become listening(connection)

    case msg => log.debug(s"unknown message: $msg")
  }

  def listening(connection: ActorRef): Receive = {

    /**
      * WirePacket receivers
      */
    case Received(bytes) =>
      buffer(bytes)
//      listener ! FromServer(bytes)

    case ToServer(bytes) =>
      connection ! Write(beforeWrite(bytes))

    /**
      * TCP signals handling
      */
    case CommandFailed(_: Write) =>
      log.debug("client write failed")
      listener ! "write failed"

    case DropConnection =>
      log.debug("client drop connection")
      connection ! Close
      context become ready
      connect()

    case c: ConnectionClosed =>
      log.debug("client connectionClosed " + c.getErrorCause)
      listener ! "connection closed"
      context become ready
      connect()

    case PeerClosed =>
      log.debug("client peerClosed")
      context become ready
      connect()

    /**
      * Fallback
      */
    case msg =>
      log.warning(s"TCP Client reveived an unexpected message: \n$msg")
  }

  /**
    * Try to perform connection
    */
  def connect(): Unit = {
    log.debug("Connecting client.")
    IO(Tcp) ! Connect(remote)
    log.info(s"Client connected to port ${remote.getPort}")
  }

  def onReceived(msg: WirePacket): Unit = {
    listener ! msg.asInstanceOf[FromServer]
  }

  def packetWrapper(packet: ByteString): WirePacket = {
    FromServer(packet)
  }

  /**
    * Override this method to intercept outcoming bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes
}