package com.scalanerds.wire

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.scalanerds.wire.opcodes._

trait Message {
  val msgHeader: MsgHeader

  def serialize: ByteString

  override def toString: String
}

object Message {
  def apply(data: ByteString): Message = {
    val (header, content) = deserialize(data)
    header.opCode match {
      case OPCODES.opReply => OpReply(header, content)
      case OPCODES.opMsg => OpMsg(header, content)
      case OPCODES.opUpdate => OpUpdate(header, content)
      case OPCODES.opInsert => OpInsert(header, content)
      case OPCODES.opQuery => OpQuery(header, content)
      case OPCODES.opGetMore => OpGetMore(header, content)
      case OPCODES.opDelete => OpDelete(header, content)
      case OPCODES.opKillCursor => OpKillCursor(header, content)
      case OPCODES.opCommand => OpCommand(header, content)
      case OPCODES.opCommandReply => OpCommandReply(header, content)
    }
  }

  def deserialize(data: ByteString): (MsgHeader, Array[Byte]) = {
    val it = data.iterator
    it.drop(4)
    val requestId = it.getInt(LITTLE_ENDIAN)
    val responseTo = it.getInt(LITTLE_ENDIAN)
    val opCode = it.getInt(LITTLE_ENDIAN)
    val header = new MsgHeader(requestId, responseTo, opCode)
    (header, it.toArray)
  }
}
