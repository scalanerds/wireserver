package com.scalanerds.tcpserver

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.{Received, _}
import akka.util.ByteString

import scala.util.matching.Regex

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor {

  val abort: Regex = "(?i)abort".r
  val confirmedClose: Regex = "(?i)confirmedclose".r
  val close: Regex = "(?i)close".r

  def receive: Receive = {
    case str: String => received(str)
    case msg: Packet => received(msg)
    case Received(data) =>
      data.utf8String.trim match {
        case abort() => connection ! Abort
        case confirmedClose() => connection ! ConfirmedClose
        case close() => {
          connection ! Close
        }
        case _ => received(data)
      }

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
  }

  def received(str: ByteString): Unit

  def received(str: String): Unit

  def received(msg: Packet): Unit

  def peerClosed() = {
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