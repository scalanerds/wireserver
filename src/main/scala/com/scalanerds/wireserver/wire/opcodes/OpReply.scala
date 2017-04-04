package com.scalanerds.wireserver.wire.opcodes


import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.conversions._
import com.scalanerds.wireserver.wire.{Message, MsgHeader, OPCODES, Response}
import org.bson.BsonDocument

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

  def apply(replyTo: Int,
            content: Array[Byte]): OpReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OPCODES.opReply)
    OpReply(msgHeader, content)
  }

  def apply(replyTo: Int,
            documents:  Array[BsonDocument] = Array[BsonDocument]()): OpReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OPCODES.opReply)
    new OpReply(msgHeader, documents=documents)
  }
}

class OpReply(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opReply),
              val responseFlags: OpReplyFlags = new OpReplyFlags(),
              val cursorId: Long = 0L,
              val startingFrom: Int = 0,
              var numberReturned: Int = 0,
              val documents: Array[BsonDocument]) extends Message with Response {

  numberReturned = documents.length

  def contentSerialize: Array[Byte] = {
    responseFlags.serialize ++
    cursorId.toByteArray ++
    startingFrom.toByteArray ++
    numberReturned.toByteArray ++
    documents.toByteArray
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

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpReply]

  override def equals(other: Any): Boolean = other match {
    case that: OpReply =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        responseFlags == that.responseFlags &&
        cursorId == that.cursorId &&
        startingFrom == that.startingFrom &&
        numberReturned == that.numberReturned &&
        (documents sameElements that.documents)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, responseFlags, cursorId, startingFrom, numberReturned, documents)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}


object OpReplyFlags {
  def apply(raw: Int): OpReplyFlags = {
    val bytes = raw.toBooleanArray
    new OpReplyFlags(bytes(0), bytes(1), bytes(2), bytes(3))
  }
}

class OpReplyFlags(val cursorNotFound: Boolean = false,
                   val queryFailure: Boolean = false,
                   val shardConfigStale: Boolean = false,
                   val awaitCapable: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](cursorNotFound, queryFailure, shardConfigStale, awaitCapable)
      .binaryToInt.toByteArray
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
