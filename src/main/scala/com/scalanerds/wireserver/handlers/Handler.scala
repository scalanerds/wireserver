package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.{Received, _}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.{GetPort, Response}

import scala.util.matching.Regex

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor {

  val abort: Regex = "(?i)abort".r
  val confirmedClose: Regex = "(?i)confirmedclose".r
  val close: Regex = "(?i)close".r

  def receive: Receive = {

    case data: Response =>
      received(data)

    case Received(data) => data.utf8String.trim match {
      case abort() => connection ! Abort
      case confirmedClose() => connection ! ConfirmedClose
      case close() => connection ! Close
      case _ => received(data)
    }

    case Close =>
      connection ! Close

    case PeerClosed =>
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

    case GetPort =>
      context.parent forward GetPort
  }

  def received(data: ByteString): Unit

  def received(response: Response): Unit = {
    connection ! Write(response.data)
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
}


