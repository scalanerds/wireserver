package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.{Received, _}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.{GetPort, Response}
import com.scalanerds.wireserver.utils.Utils._

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor {

  val log = Logging(context.system, this)

  var storage = Vector.empty[ByteString]
  var stored = 0L
  var transferred = 0L
  var lastLen = Long.MaxValue

  def receive: Receive = {

    /*
    SIGNALS
     */

    // Messages coming from remote TCP peer (client)
    case Received(data) =>
      received(data)

    // Processed response, write to TCP peer (client)
    case Response(data) =>
      received(data)

    // Connection status signals
    case PeerClosed =>
      log.debug("[Server] Peer closed connection")
      peerClosed()
      stop()
    case ErrorClosed =>
      errorClosed()
      stop()
    case Closed =>
      closed()
      stop()
    case ConfirmedClosed =>
      confirmedClosed()
      stop()
    case Aborted =>
      aborted()
      stop()

    /*
    COMMANDS
     */

    // Close command, close connection as well
    case Close =>
      connection ! Close

    // GetPort command, forward to parent (TCP server)
    case GetPort =>
      context.parent forward GetPort
  }

  def received(data: ByteString): Unit

  def received(response: Response): Unit = {
    connection ! Write(response.bytes)
  }

  def peerClosed() {
    connection ! Close
  }

  def errorClosed() {
    println("ErrorClosed")
  }

  def closed() {
    println("Closed")
  }

  def confirmedClosed() {
    println("ConfirmedClosed")
  }

  def aborted() {
    println("Aborted")
  }

  def stop() {
    context stop self
  }

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


