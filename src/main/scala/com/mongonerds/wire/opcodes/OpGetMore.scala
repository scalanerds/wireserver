package com.mongonerds.wire.opcodes

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}

object OpGetMore {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpGetMore = ???
}

class OpGetMore(val msgHeader: MsgHeader) extends Message {
  override def serialize: ByteString = ???
}