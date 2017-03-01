package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wire.{Message, MsgHeader}
import com.scalanerds.utils.Utils._

object OpMsg {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpMsg = {
    new OpMsg(msgHeader, content.toUTFString)
  }
}

class OpMsg(val msgHeader: MsgHeader,
            val message: String) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      message.toByteArray
    ByteString((content.length + 4).toByteArray ++ content)
  }
}