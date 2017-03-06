package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.conversions._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

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
              val query: BSONObject,
              val returnFieldsSelector: Option[BSONObject] = None) extends Message {

  override def serialize: ByteString = {
    var content = msgHeader.serialize ++
      flags.serialize ++
      fullCollectionName.toByteArray ++
      Array(numberToSkip, numberToSkip, numberToReturn).toByteArray ++
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


