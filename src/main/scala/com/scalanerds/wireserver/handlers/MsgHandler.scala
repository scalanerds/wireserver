package com.scalanerds.wireserver.handlers


import akka.actor.ActorRef
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.Response
import com.scalanerds.wireserver.wire.Message
import com.scalanerds.wireserver.wire.opcodes._


class MsgHandler(connection: ActorRef) extends Handler(connection) {

  // data the server receives
  override def received(data: ByteString): Unit = {
    parse(data)
  }

  def parse(data: ByteString): Unit = {
    Message(data) match {
      case msg: OpReply => onOpReply(msg)
      case msg: OpMsg => onOpMsg(msg)
      case msg: OpUpdate => onOpUpdate(msg)
      case msg: OpInsert => onOpInsert(msg)
      case msg: OpQuery => onOpQuery(msg)
      case msg: OpGetMore => onOpGetMore(msg)
      case msg: OpDelete => onOpDelete(msg)
      case msg: OpKillCursor => onOpKillCursor(msg)
      case msg: OpCommand => onOpCommand(msg)
      case msg: OpCommandReply => onOpCommandReply(msg)
      case msg => onError(msg)
    }
  }

  def onOpReply(msg: OpReply): Unit = {}

  def onOpMsg(msg: OpMsg): Unit = {}

  def onOpUpdate(msg: OpUpdate): Unit = {}

  def onOpInsert(msg: OpInsert): Unit = {}

  def onOpQuery(msg: OpQuery): Unit = {}

  def onOpGetMore(msg: OpGetMore): Unit = {}

  def onOpDelete(msg: OpDelete): Unit = {}

  def onOpKillCursor(msg: OpKillCursor): Unit = {}

  def onOpCommand(msg: OpCommand): Unit = {}

  def onOpCommandReply(msg: OpCommandReply): Unit = {}

  def onError(msg: Any): Unit = {}

  override def received(response: Response): Unit = {
    context.parent ! response
  }
}
