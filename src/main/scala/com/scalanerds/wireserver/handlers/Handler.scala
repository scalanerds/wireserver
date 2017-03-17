package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import akka.io.Tcp.{Received, _}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.{FromClient, GetPort, Ready, ToClient}
import com.scalanerds.wireserver.utils.Utils._

case object Ack extends Event

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor {
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
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes

  /**
    * Override this method to handle incoming requests
    * @param request
    * @return
    */
  def onReceived(request: FromClient): Unit = Unit

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

  var storage = Vector.empty[ByteString]
  var storedBytes = 0L
  // length of the ByteString as declared in the first 4 bytes of the first segment
  var frameLength : Option[Int] = None

  /**
    * Buffer that joins consequent segments into a single frame
    * @param segment
    */
  private def buffer(segment: ByteString): Unit = {
    // get the length of the ByteString by reading the first 4 bytes as Int
    val msgLength = segment.take(4).toArray.toInt
    // if we don't have anything in buffer and the length is equal to the ByteString length
    // then the frame is complete
    if (storedBytes == 0 && msgLength == segment.length) {
      resetBuffer()
      onReceived(FromClient(segment))
    } else {
      //if is the first incomplete ByteString then store the msgLength
      if (frameLength.isEmpty)
        frameLength = Some(msgLength)
      // store the incomplete ByteString
      storage :+= segment
      // store how many bytes we have stored
      storedBytes += segment.size
      // check if the frame is complete
      if (storedBytes >= frameLength.get) {
        // join the segments
        val frame = ByteString(storage.flatten.toArray)
        resetBuffer()
        onReceived(FromClient(frame))
      }
    }
  }

  /**
    * Reset the framing buffer
    */
  private def resetBuffer() = {
    context.unbecome()
    storage = Vector.empty[ByteString]
    storedBytes = 0
    frameLength = None
  }
}


