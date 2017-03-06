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
    val byteString = data.mkString(", ")
    val res = Message(data) match {
      case msg: OpReply => s"OpReply\n$byteString\n$msg\n"
      case msg: OpMsg => s"OpMsg\n$byteString\n$msg\n"
      case msg: OpUpdate => s"OpUpdate\n$byteString\n$msg\n"
      case msg: OpInsert => s"OpInsert\n$byteString\n$msg\n"
      case msg: OpQuery => s"OpQuery\n$byteString\n$msg\n"
      case msg: OpGetMore => s"OpGetMore\n$byteString\n$msg\n"
      case msg: OpDelete => s"OpDelete\n$byteString\n$msg\n"
      case msg: OpKillCursor => s"OpKillCursor\n$byteString\n$msg\n"
      case msg: OpCommand => s"OpCommand\n$byteString\n$msg\n"
      case msg: OpCommandReply => s"OpCommandReply\n$byteString\n$msg\n"
      case msg => s"Unknown message\n$byteString\n$msg\n"
    }
    log.debug(res)
  }
}
