package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.wire.{Message, MsgHeader}
import com.scalanerds.wireserver.utils.Utils._

object OpMsg {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpMsg = {
    new OpMsg(msgHeader, content.toUTFString)
  }
}

class OpMsg(val msgHeader: MsgHeader,
            val message: String) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Array[Byte] = {
    message.toByteArray
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |message: $message
     """.stripMargin
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[OpMsg]

  override def equals(other: Any): Boolean = other match {
    case that: OpMsg =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        message == that.message
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, message)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}