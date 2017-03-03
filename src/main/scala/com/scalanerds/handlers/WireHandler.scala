package com.scalanerds.handlers

import akka.actor._
import akka.event.Logging
import akka.io.Tcp.Write
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
    println(s"Wire got string \n$msg")
  }

  def parse(data: ByteString): Unit = {

    Message(data) match {
      case msg: OpReply => log.debug(s"OpReply\n$data\n$msg\n")
      case msg: OpMsg => log.debug(s"OpMsg\n$data\n$msg\n")
      case msg: OpUpdate => log.debug(s"OpUpdate\n$data\n$msg\n")
      case msg: OpInsert => log.debug(s"OpInsert\n$data\n$msg\n")
      case msg: OpQuery => log.debug(s"OpQuery\n$data\n$msg\n")
      case msg: OpGetMore => log.debug(s"OpGetMore\n$data\n$msg\n")
      case msg: OpDelete => log.debug(s"OpDelete\n$data\n$msg\n")
      case msg: OpKillCursor => log.debug(s"OpKillCursor\n$data\n$msg\n")
      case msg: OpCommand => log.debug(s"OpCommand\n$data\n$msg\n")
      case msg: OpCommandReply => log.debug(s"OpCommandReply\n$data\n$msg\n")
      case msg => log.debug(s"Unknown message\n$data\n$msg\n")
    }
  }
}
