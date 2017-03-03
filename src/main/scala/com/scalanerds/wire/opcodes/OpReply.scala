package com.scalanerds.wire.opcodes


import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject
import com.scalanerds.wire.conversions._

object OpReply {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpReply = {
    val it = content.iterator
    val responseFlags = OpReplyFlags(it.getInt)
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
              val responseFlags: OpReplyFlags,
              val cursorId: Long,
              val startingFrom: Int,
              val numberReturned: Int,
              val documents: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      responseFlags.serialize ++
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


object OpReplyFlags {
  def apply(raw: Int): OpReplyFlags = {
    val bytes = raw.toBooleanArray
    new OpReplyFlags(bytes(0),bytes(1),bytes(2),bytes(3))
  }
}

class OpReplyFlags(val cursorNotFound: Boolean = false,
                   val queryFailure: Boolean = false,
                   val shardConfigStale: Boolean = false,
                   val awaitCapable: Boolean = false) {
  def serialize: ByteString = {
    Array(cursorNotFound, queryFailure, shardConfigStale, awaitCapable)
      .asInstanceOf[ByteString]
  }

  override def toString: String = {
    s"""
       |cursorNotFound: $cursorNotFound
       |queryFailure: $queryFailure
       |shardConfigStale: $shardConfigStale
       |awaitCapable: $awaitCapable
     """.stripMargin
  }
}
