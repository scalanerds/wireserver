package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.{Received, _}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.{GetPort, Ready}
import com.scalanerds.wireserver.tcpserver.Packet
import com.scalanerds.wireserver.utils.Utils._

case object Ack extends Event

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef)
  extends Actor {
  val log = Logging(context.system, this)

  override def postStop(): Unit = {
    connection ! Close
    log.debug("walter died " + connection.path)
    super.postStop()
  }

  def receive: Receive = {
    case str: String => received(str)
    case msg: Packet => received(msg)
    case Received(data) =>
      buffer(data)

    case PeerClosed =>
      log.debug("server peerClosed " + connection.path)
      peerClosed()
    case ErrorClosed =>
      log.debug("server errorClosed " + connection.path)
      errorClosed()
      stop()
    case Closed =>
      log.debug("server closed " + connection.path)
      closed()
      stop()
    case ConfirmedClosed =>
      log.debug("server confirmedClosed " + connection.path)
      confirmedClosed()
      stop()
    case Aborted =>
      log.debug("server aborted " + connection.path)
      aborted()
      stop()

    case GetPort =>
      context.parent forward GetPort

    case Ready => connection ! Register(self)

  }


  def received(data: ByteString): Unit

  def received(packet: Packet): Unit

  def received(str: String): Unit

  def peerClosed() {
    stop()
  }

  def errorClosed() {
    log.debug("server ErrorClosed " + connection.path)
  }

  def closed() {
    log.debug("server Closed " + connection.path)
  }

  def confirmedClosed() {
    log.debug("server ConfirmedClosed " + connection.path)
  }

  def aborted() {
    log.debug("server Aborted " + connection.path)
  }

  def stop() {
    log.debug("server stop " + connection.path)
    context stop self
  }

  var storage = Vector.empty[ByteString]
  var stored = 0L
  var lastLen = Long.MaxValue

  private def buffer(data: ByteString): Unit = {
    val len = data.take(4).toArray.toInt
    if (stored == 0 && len == data.length) {
      resetBuffer()
      received(data)
    } else {

      storage :+= data
      stored += data.size

      if (lastLen == Long.MaxValue)
        lastLen = len

      if (stored >= lastLen) {
        val buf = ByteString(storage.flatten.toArray)
        resetBuffer()
        received(buf)
      }
    }
  }

  private def resetBuffer() = {
    context.unbecome()
    storage = Vector.empty[ByteString]
    stored = 0
    lastLen = Long.MaxValue
  }
}


