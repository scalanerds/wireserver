package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BsonDocument

object OpCommandReply {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpCommandReply = {
    val it = content.iterator
    val metadata = it.getBson
    val commandReply = it.getBson
    val outputDocs = it.getBsonArray
    new OpCommandReply(msgHeader, metadata, commandReply, outputDocs)
  }
}

class OpCommandReply(val msgHeader: MsgHeader,
                     val metadata: BsonDocument = new BsonDocument(),
                     val commandReply: BsonDocument = new BsonDocument(),
                     val outputDocs: Array[BsonDocument] = Array()
                    ) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      metadata.toByteArray ++
      commandReply.toByteArray ++
      outputDocs.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
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
        outputDocs == that.outputDocs
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, metadata, commandReply, outputDocs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
