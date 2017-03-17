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

  def received(str: String) {
    log.debug(s"unknown $str")
  }

  def peerClosed() {
    stop()
  }

  def errorClosed() {
    log.debug("server ErrorClosed " + connection.path)
  }

  def closed() {
    log.debug("server Closed " + connection.path)
  }

  def aborted() {
    log.debug("server Aborted " + connection.path)
  }

  def stop() {
    log.debug("server stop " + connection.path)
    context stop self
  }

  var storage = Vector.empty[ByteString]
  var storedBytes = 0L
  // length of the ByteString as declared in the first 4 bytes of the first segment
  var frameLength : Option[Int] = None

  private def buffer(data: ByteString): Unit = {
    // get the length of the ByteString by reading the first 4 bytes as Int
    val msgLength = data.take(4).toArray.toInt
    // if we don't have anything in buffer and the length is equal to the ByteString length
    // then the frame is complete
    if (storedBytes == 0 && msgLength == data.length) {
      resetBuffer()
      received(data)
    } else {
      //if is the first incomplete ByteString then store the msgLength
      if (frameLength.isEmpty)
        frameLength = Some(msgLength)
      // store the incomplete ByteString
      storage :+= data
      // store how many bytes we have stored
      storedBytes += data.size
      // check if the frame is complete
      if (storedBytes >= frameLength.get) {
        // join the segments
        val buf = ByteString(storage.flatten.toArray)
        resetBuffer()
        received(buf)
      }
    }
  }

  private def resetBuffer() = {
    context.unbecome()
    storage = Vector.empty[ByteString]
    storedBytes = 0
    frameLength = None
  }
}


