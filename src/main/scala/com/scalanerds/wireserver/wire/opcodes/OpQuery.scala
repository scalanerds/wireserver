package com.scalanerds.wireserver.wire.opcodes


import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire._
import com.scalanerds.wireserver.wire.conversions._
import org.bson.{BsonDocument, BsonString}

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

class OpQuery(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opQuery),
              val flags: OpQueryFlags = new OpQueryFlags(),
              val fullCollectionName: String,
              val numberToSkip: Int = 0,
              val numberToReturn: Int = 1,
              val query: BsonDocument = new BsonDocument(),
              val returnFieldsSelector: Option[BsonDocument] = None)
  extends Message with Request {

  override def reply(content: Array[Byte]): OpReply = {
    OpReply(msgHeader.requestId, content = content)
  }

  override def reply(docs: Array[BsonDocument]): OpReply = {
    OpReply(msgHeader.requestId, documents = docs)
  }

  override def reply(doc: BsonDocument): OpReply = reply(Array(doc))

  override def reply(json: String): OpReply = reply(BsonDocument.parse(json))

  override def contentSerialize: Array[Byte] = {
    var content =
      flags.serialize ++
      fullCollectionName.toByteArray ++
      Array(numberToSkip, numberToReturn).toByteArray ++
      query.toByteArray
    if (returnFieldsSelector.nonEmpty)
      content ++= returnFieldsSelector.get.toByteArray
    content
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
    var state = Seq(msgHeader.opCode, flags, fullCollectionName, numberToSkip, numberToReturn)
    if (returnFieldsSelector.nonEmpty) state += returnFieldsSelector.get.toJson()
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  def collection = {
    val chunks = fullCollectionName.split("\\.")
    if (chunks.last == "$cmd") {
      val cmd = command
      if (cmd != null){
        chunks(0) +
          (query.get(cmd) match {
          case s: BsonString  => "." + s.asString().getValue
          case _              => ""
        })
      }

      else fullCollectionName
    } else fullCollectionName
  }

  override def realm: String = collection

  override def command: String = {
    val keys = query.keySet.toArray
    if (keys.nonEmpty)
      keys.head.asInstanceOf[String]
    else
      "find"
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

  override def hashCode(): Int = {
    val state = Seq(0, tailableCursor, slaveOk, opLogReply, noCursorTimeOut, awaitData, exhaust, partial)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
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


