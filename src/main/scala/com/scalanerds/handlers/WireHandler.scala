package com.scalanerds.handlers

import akka.actor._
import akka.event.Logging
import akka.io.Tcp.{Close, Write}
import akka.util.ByteString
import com.scalanerds.tcpserver.{Handler, HandlerProps}
import com.scalanerds.utils.Packet
import com.scalanerds.wire.Message
import com.scalanerds.wire.opcodes._

object WireHandlerProps extends HandlerProps {
  def props(connection: ActorRef, listener: ActorRef) = Props(classOf[WireHandler], connection, listener)
}

class WireHandler(connection: ActorRef, val listener: ActorRef) extends Handler(connection) {
  val log = Logging(context.system, this)

  def received(data: ByteString): Unit = {
    log.debug("to mongod")
    parse(data)
    listener ! Packet(data)
  }

  def received(data: Packet): Unit = {
    log.debug("to mongocli")
    parse(data.data)
    connection ! Write(data.data)
  }

  def received(msg: String): Unit = {
    log.debug(s"Wire got string \n$msg")
    msg match {
      case "close" => connection ! Close
      case _ => log.debug("no match for message")
    }
  }

  override def peerClosed() = {
    listener ! "close mongod"
    connection ! Close
  }

  def parse(data: ByteString): Unit = {
    val byteString = data.mkString(", ")
    log.debug(byteString)
    val res = Message(data) match {
      case msg: OpReply => s"OpReply\n$msg\n"
      case msg: OpMsg => s"OpMsg\n$msg\n"
      case msg: OpUpdate => s"OpUpdate\n$msg\n"
      case msg: OpInsert => s"OpInsert\n$msg\n"
      case msg: OpQuery => s"OpQuery\n$msg\n"
      case msg: OpGetMore => s"OpGetMore\n$msg\n"
      case msg: OpDelete => s"OpDelete\n$msg\n"
      case msg: OpKillCursor => s"OpKillCursor\n$msg\n"
      case msg: OpCommand => s"OpCommand\n$msg\n"
      case msg: OpCommandReply => s"OpCommandReply\n$msg\n"
      case msg => {
        listener ! "close mongod"
        s"Unknown message\n$msg\n"
      }
    }
    log.debug(res)
  }
}
