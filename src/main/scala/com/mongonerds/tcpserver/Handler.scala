package com.mongonerds.tcpserver

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.{Received, _}

import scala.util.matching.Regex

trait HandlerProps {
  def props(connection: ActorRef): Props
}

abstract class Handler(val connection: ActorRef) extends Actor {

  val abort : Regex = "(?i)abort".r
  val confirmedClose : Regex = "(?i)confirmedclose".r
  val close : Regex = "(?i)close".r

  def receive: Receive = {
    case Received(data) =>
      data.utf8String.trim match {
        case abort() => connection ! Abort
        case confirmedClose() => connection ! ConfirmedClose
        case close() => connection ! Close
        case str => received(str)
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

  def received(str: String): Unit

  def peerClosed() {
    println("PeerClosed")
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
    println("Stopping")
    context stop self
  }
}