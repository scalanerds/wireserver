package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.{Received, _}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.GetPort
import com.scalanerds.wireserver.tcpserver.Packet
import com.scalanerds.wireserver.utils.Utils._

import scala.util.matching.Regex

case object Ack extends Event

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef)
  extends Actor {
  val log = Logging(context.system, this)

  context watch connection

  def receive: Receive = {
    case str: String => received(str)
    case msg: Packet => received(msg)
    case Received(data) =>
      buffer(data)

    case PeerClosed =>
      log.debug("server peerClosed")
      peerClosed()
      stop()
    case ErrorClosed =>
      log.debug("server errorClosed")
      errorClosed()
      stop()
    case Closed =>
      log.debug("server closed")
      closed()
      stop()
    case ConfirmedClosed =>
      log.debug("server confirmedClosed")
      confirmedClosed()
      stop()
    case Aborted =>
      log.debug("server aborted")
      aborted()
      stop()

    case GetPort =>
      context.parent forward GetPort
  }

  def received(data: ByteString): Unit

  def received(packet: Packet): Unit

  def received(str: String): Unit

  def peerClosed() {
    connection ! Close
  }

  def errorClosed() {
    log.debug("server ErrorClosed")
  }

  def closed() {
    log.debug("server Closed")
  }

  def confirmedClosed() {
    log.debug("server ConfirmedClosed")
  }

  def aborted() {
    log.debug("server Aborted")
  }

  def stop() {
    log.debug("server stop")
    context stop self
  }

  var storage = Vector.empty[ByteString]
  var stored = 0L
  var lastLen = Long.MaxValue

  private def buffer(data: ByteString): Unit = {
    val len = data.take(4).toArray.toInt
    log.debug(s"len: $len, realLen: ${data.length}\ndata: ${data.mkString(", ")}")

    if(stored == 0 && len == data.length){
      log.debug("forward as is")
      resetBuffer()
      received(data)
    } else {
      log.debug("append to buffer")
      storage :+= data
      stored += data.size

      if(lastLen == Long.MaxValue)
        lastLen = len

      if(stored >= lastLen) {
        log.debug("assemble buffer")
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


