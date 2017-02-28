package com.mongonerds.wire

import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.mongonerds.wire.opcodes.{OpQuery, OpReply}

trait Message {
  val msgHeader: MsgHeader

  def serialize: ByteString
}

object Message {
  def apply(data: ByteString): Message = {
    val (header, content) = deserialize(data)
    header.opCode match {
      case OpCodes.opReply => OpReply(header, content)
      case OpCodes.opMsg => ???
      case OpCodes.opUpdate => ???
      case OpCodes.opInsert => ???
      case OpCodes.opQuery => OpQuery(header, content)
      case OpCodes.opGetMore => ???
      case OpCodes.opDelete => ???
      case OpCodes.opKillCursor => ???
      case OpCodes.opCommand => ???
      case OpCodes.opCommandReply => ???
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
