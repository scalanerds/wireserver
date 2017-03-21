package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp._
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.tcpserver.TcpBuffer


case object Ack extends Event

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor with TcpBuffer {
  val log = Logging(context.system, this)


  override def postStop(): Unit = {
    connection ! Close
    log.debug("walter died " + connection.path)
    super.postStop()
  }

  def receive: Receive = {
    /**
      * WirePacket receivers
      */
    case ToClient(bytes) =>
      connection ! Write(beforeWrite(bytes))

    case Received(segment: ByteString) =>
      buffer(segment)

    /**
      * TCP signals
      */
    case PeerClosed =>
      log.debug("server peerClosed " + connection.path)
      onPeerClosed()
    case ErrorClosed =>
      log.debug("server errorClosed " + connection.path)
      onErrorClosed()
      stop()
    case Closed =>
      log.debug("server closed " + connection.path)
      onClosed()
      stop()
    case Aborted =>
      log.debug("server aborted " + connection.path)
      onAborted()
      stop()

    /**
      * Listener signals
      */
    case Ready =>
      connection ! Register(self)

    /**
      * Commands
      */
    case GetPort =>
      context.parent forward GetPort
  }

  /**
    * Override this method to intercept outcoming bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes

  /**
    * Override this method to handle incoming requests
    *
    * @param request
    * @return
    */
  def onReceived(request: WirePacket): Unit = Unit

  def packetWrapper(packet: ByteString): WirePacket = {
    FromClient(packet)
  }

  def onPeerClosed() {
    stop()
  }

  def onErrorClosed() {
    log.debug("server ErrorClosed " + connection.path)
  }

  def onClosed() {
    log.debug("server Closed " + connection.path)
  }

  def onAborted() {
    log.debug("server Aborted " + connection.path)
  }

  /**
    * Stop this actor
    */
  def stop() {
    log.debug("server stop " + connection.path)
    context stop self
  }
}


