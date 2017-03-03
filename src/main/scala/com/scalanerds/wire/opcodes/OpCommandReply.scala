package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

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
                     val metadata: BSONObject,
                     val commandReply: BSONObject,
                     val outputDocs: Array[BSONObject]
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
}
