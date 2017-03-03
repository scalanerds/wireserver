package com.scalanerds.wire.opcodes


import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpReply {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpReply = {
    val it = content.iterator
    val responseFlags = it.getInt
    val cursorId = it.getLong
    val startingFrom = it.getInt
    val numberReturned = it.getInt
    val documents = it.getBsonArray
    new OpReply(msgHeader,
      responseFlags,
      cursorId,
      startingFrom,
      numberReturned,
      documents
    )
  }
}

class OpReply(val msgHeader: MsgHeader,
              val responseFlags: Int,
              val cursorId: Long,
              val startingFrom: Int,
              val numberReturned: Int,
              val documents: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      responseFlags.toByteArray  ++
      cursorId.toByteArray ++
      startingFrom.toByteArray ++
      numberReturned.toByteArray ++
      documents.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |responseFlags: $responseFlags
       |cursorId: $cursorId
       |startingFrom: $startingFrom
       |numberReturned: $numberReturned
       |documents: ${documents.mkString("\n")}
     """.stripMargin
  }
}



