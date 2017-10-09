package com.scalanerds.wireserver.wire.opcodes


import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.traits.{Message, Response}
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import com.scalanerds.wireserver.wire.opcodes.flags.OpReplyFlags
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
      documents: Array[BsonDocument] = Array[BsonDocument]()): OpReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OPCODES.opReply)
    new OpReply(msgHeader, documents = documents)
  }
}

class OpReply(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opReply),
    val responseFlags: OpReplyFlags = new OpReplyFlags(),
    val cursorId: Long = 0L,
    val startingFrom: Int = 0,
    var numberReturned: Int = 0,
    var documents: Array[BsonDocument]) extends Message with Response {

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





