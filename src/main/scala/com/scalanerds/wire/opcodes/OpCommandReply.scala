package com.scalanerds.wire.opcodes

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}

object OpCommandReply {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpCommandReply = ???
}

class OpCommandReply(val msgHeader: MsgHeader) extends Message {
  override def serialize: ByteString = ???
}
