package com.scalanerds.wireserver.wire.opcodes


import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.{Message, Response}
import com.scalanerds.wireserver.wire.opcodes.flags.OpReplyFlags
import org.bson.BsonDocument

/**
  * Mongo database reply
  *
  * Code 1
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpReply message is sent by the database in response to an OpQuery or OpGetMore message.
  *
  * Flags
  *   - 0 corresponds to CursorNotFound. Is set when getMore is called but the cursor id is not
  *     valid at the server. Returned with zero results.
  *   - 1 corresponds to QueryFailure. Is set when query failed. Results consist of one document
  *     containing an “$err” field describing the failure.
  *   - 2 corresponds to ShardConfigStale. Drivers should ignore this. Only mongos will ever
  *     see this set, in which case, it needs to update config from the server.
  *   - 3 corresponds to AwaitCapable. Is set when the server supports the AwaitData Query
  *     option. If it doesn’t, a client should sleep a little between getMore’s of a Tailable cursor.
  *     Mongod version 1.6 supports AwaitData and thus always sets AwaitCapable.
  *   - 4-31 are reserved. Ignore.
  *
  * @param msgHeader      Message header.
  * @param responseFlags  Bit vector.
  * @param cursorId       The cursorID that this OpReply is a part of.
  * @param startingFrom   Starting position in the cursor.
  * @param numberReturned Number of documents in the reply.
  * @param documents      Returned documents.
  */
class OpReply(val msgHeader: MsgHeader = new MsgHeader(opCode = OpReplyCode),
    val responseFlags: OpReplyFlags = new OpReplyFlags(),
    val cursorId: Long = 0L,
    val startingFrom: Int = 0,
    var numberReturned: Int = 0,
    var documents: List[BsonDocument]) extends Message with Response {

  numberReturned = documents.length

  /** serialize message into bytes */
  def contentSerialize: Seq[Byte] = {
    responseFlags.serialize ++
      cursorId.toByteList ++
      startingFrom.toByteList ++
      numberReturned.toByteList ++
      documents.toByteList
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
    Seq(msgHeader.opCode, responseFlags, cursorId, startingFrom, numberReturned, documents)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }
}


object OpReply {
  /**
    * Construct OpReply
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpReply
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpReply = {
    val it = content.iterator
    val responseFlags = OpReplyFlags(it.getInt)
    val cursorId = it.getLong
    val startingFrom = it.getInt
    val numberReturned = it.getInt
    val documents = it.getBsonList
    new OpReply(msgHeader,
      responseFlags,
      cursorId,
      startingFrom,
      numberReturned,
      documents
    )
  }

  /**
    * Construct OpReply from requestID and bytes
    *
    * @param replyTo requestID from the original request.
    * @param content Message bytes.
    * @return OpReply
    */
  def apply(replyTo: Int, content: Seq[Byte]): OpReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OpReplyCode)
    OpReply(msgHeader, content)
  }

  /**
    * Construct OpReply fromRequestId and BsonDocuments
    *
    * @param replyTo   requestID from the original request.
    * @param documents BsonDocuments
    * @return OpReply
    */
  def apply(replyTo: Int, documents: List[BsonDocument] = List[BsonDocument]()): OpReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OpReplyCode)
    new OpReply(msgHeader, documents = documents)
  }
}
