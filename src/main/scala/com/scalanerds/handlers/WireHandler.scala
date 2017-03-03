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
    log.debug("to client")
    parse(data)
    listener ! Packet(data)
  }

  def received(data: Packet): Unit = {
    log.debug("from client")
    parse(data.data)
    connection ! Write(data.data)
  }

  def received(msg: String): Unit = {
    println(s"Wire got string \n$msg")
  }

  def parse(data: ByteString): Unit = {

    Message(data) match {
      case _: OpReply => log.debug("OpReply\n" + data)
      case _: OpMsg => log.debug("OpMsg\n" + data)
      case _: OpUpdate => log.debug("OpUpdate\n" + data)
      case _: OpInsert => log.debug("OpInsert\n" + data)
      case _: OpQuery => log.debug("OpQuery\n" + data)
      case _: OpGetMore => log.debug("OpGetMore\n" + data)
      case _: OpDelete => log.debug("OpDelete\n" + data)
      case _: OpKillCursor => log.debug("OpKillCursor\n" + data)
      case _: OpCommand => log.debug("OpRCommand\n" + data)
      case _: OpCommandReply => log.debug("OpCommandReply\n" + data)
      case _ => log.debug("Unknown message\n" + data)
    }
  }
}
