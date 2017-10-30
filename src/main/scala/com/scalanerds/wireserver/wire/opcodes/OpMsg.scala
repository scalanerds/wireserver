package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Message


/**
  * Generic OpMessage
  *
  * Code 1000
  *
  * Generic msg command followed by a string
  *
  * @param msgHeader Message header.
  * @param message   Generic message.
  */
class OpMsg(val msgHeader: MsgHeader, val message: String) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Seq[Byte] = {
    message.toByteList
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
    Seq(msgHeader.opCode, message)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }
}

object OpMsg {
  /**
    * Construct OpMsg
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpMsg
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpMsg = {
    new OpMsg(msgHeader, content.toUTFString)
  }
}