package com.mongonerds.wire.opcodes

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}

object OpDelete {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpDelete = ???
}

class OpDelete(val msgHeader: MsgHeader, fullCollectionName : String,
               flags: Int, document: BSONObject, reserved: Int = 0) extends Message {
  override def serialize: ByteString = ???
}