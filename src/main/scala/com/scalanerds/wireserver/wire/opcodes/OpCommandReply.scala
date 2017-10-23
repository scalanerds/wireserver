package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.traits.{Message, Response}
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson.BsonDocument

object OpCommandReply {
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpCommandReply = {
    val it = content.iterator
    val metadata = it.getBson
    val commandReply = it.getBson
    val outputDocs = it.getBsonList
    new OpCommandReply(msgHeader, metadata, commandReply, outputDocs)
  }

  def apply(replyTo: Int,
      metadata: BsonDocument = new BsonDocument()): OpCommandReply = {
    new OpCommandReply(new MsgHeader(
      responseTo = replyTo,
      opCode = OPCODES.opCommandReply
    ), metadata = metadata)
  }

  def apply(replyTo: Int,
      content: Seq[Byte]): OpCommandReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OPCODES.opCommandReply)
    OpCommandReply(msgHeader, content)
  }
}

class OpCommandReply(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opCommandReply),
    var metadata: BsonDocument = new BsonDocument(),
    var commandReply: BsonDocument = new BsonDocument(),
    var outputDocs: List[BsonDocument] = List()
) extends Message with Response {

  override def contentSerialize: Seq[Byte] = {
    metadata.toByteList ++
      commandReply.toByteList ++
      outputDocs.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |metadata: $metadata
       |commandReply: $commandReply
       |outpuDocs: ${outputDocs.mkString("\n")}
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpCommandReply]

  override def equals(other: Any): Boolean = other match {
    case that: OpCommandReply =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        metadata == that.metadata &&
        commandReply == that.commandReply &&
        (outputDocs sameElements that.outputDocs)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, metadata, commandReply, outputDocs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
