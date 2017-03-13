package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.conversions._
import com.scalanerds.wireserver.wire.{Message, MsgHeader, OPCODES}
import org.bson.BsonDocument

object OpQuery {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpQuery = {
    val it = content.iterator
    val flags = OpQueryFlags(it.getInt)
    val fullCollectionName = it.getString
    val numberToSkip = it.getInt
    val numberToReturn = it.getInt
    val bson = it.getBsonArray
    val query = bson(0)
    val returnFieldSelector = if (bson.length == 2) Some(bson(1)) else None
    new OpQuery(msgHeader, flags, fullCollectionName, numberToSkip, numberToReturn, query, returnFieldSelector)
  }
}

class OpQuery(val msgHeader: MsgHeader,
              val flags: OpQueryFlags,
              val fullCollectionName: String,
              val numberToSkip: Int,
              val numberToReturn: Int,
              val query: BsonDocument,
              val returnFieldsSelector: Option[BsonDocument] = None) extends Message {

  def reply(docs: Array[BsonDocument]): OpReply = {
    OpReply(
      MsgHeader(
        responseTo = msgHeader.requestId,
        opCode     = OPCODES.opReply
      ),
      responseFlags = new OpReplyFlags(),
      documents = docs
    )
  }

  def reply(doc: BsonDocument): OpReply = reply(Array(doc))

  def reply(json: String) : OpReply = reply(BsonDocument.parse(json))

  override def serialize: ByteString = {
    var content = msgHeader.serialize ++
    flags.serialize ++
    fullCollectionName.toByteArray ++
    Array(numberToSkip, numberToReturn).toByteArray ++
    query.toByteArray
    if (returnFieldsSelector.nonEmpty)
    content ++= returnFieldsSelector.get.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |flags: $flags
       |fullCollectionName: $fullCollectionName
       |numberToSkip: $numberToReturn
       |query: $query
       |returnFieldsSelector: ${returnFieldsSelector.getOrElse("")}
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpQuery]

  override def equals(other: Any): Boolean = other match {
    case that: OpQuery =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        flags == that.flags &&
        fullCollectionName == that.fullCollectionName &&
        numberToSkip == that.numberToSkip &&
        numberToReturn == that.numberToReturn &&
        query == that.query &&
        returnFieldsSelector == that.returnFieldsSelector
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, flags, fullCollectionName, numberToSkip, numberToReturn, query, returnFieldsSelector)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object OpQueryFlags {
  def apply(raw: Int): OpQueryFlags = {
    val bytes = raw.toBooleanArray
    new OpQueryFlags(bytes(1),bytes(2),bytes(3),bytes(4),bytes(5),bytes(6),bytes(7))
  }
}

class OpQueryFlags(val tailableCursor: Boolean = false,
                   val slaveOk: Boolean = false,
                   val opLogReply: Boolean = false,
                   val noCursorTimeOut: Boolean = false,
                   val awaitData: Boolean = false,
                   val exhaust: Boolean = false,
                   val partial: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](0, tailableCursor, slaveOk, opLogReply, noCursorTimeOut, awaitData, exhaust, partial)
      .binaryToInt.toByteArray
  }

  override def toString: String = {
    s"""
       |tailableCursor: $tailableCursor
       |slaveOk: $slaveOk
       |opLogReply: $opLogReply
       |noCursorTimeOut: $noCursorTimeOut
       |awaitData: $awaitData
       |exhaust: $exhaust
       |partial: $partial
     """.stripMargin
  }
}


