package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.{Message, Response}
import org.bson.BsonDocument


/** Mongo server reply
  *
  * Code 2011
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpCommandReply is a wire protocol message used internally for replying to intra-cluster
  * OpCommand requests issued by one MongoDB server to another.
  *
  * @param msgHeader    Message header.
  * @param metadata     Available for the system to attach any metadata to internal commands that is not
  *                     part of the command parameters proper, as supplied by the client driver
  * @param commandReply A BSON document containing the command reply.
  * @param outputDocs   Useful for commands that can return a large amount of data, such as find
  *                     or aggregate. (This field is not currently in use.)
  */
class OpCommandReply(val msgHeader: MsgHeader = new MsgHeader(opCode = OpCommandReplyCode),
    var metadata: BsonDocument = new BsonDocument(),
    var commandReply: BsonDocument = new BsonDocument(),
    var outputDocs: List[BsonDocument] = List()
) extends Message with Response {

  override def contentSerialize: Seq[Byte] = {
    metadata.toByteList ++
      commandReply.toByteList ++
      outputDocs.toByteList
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
        (outputDocs sameElements that.outputDocs)
    case _ => false
  }

  override def hashCode(): Int = {
    Seq(msgHeader.opCode, metadata, commandReply, outputDocs)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }
}


object OpCommandReply {
  /**
    * Construct OpCommandReply
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpCommandReply
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpCommandReply = {
    val it = content.iterator
    val metadata = it.getBson
    val commandReply = it.getBson
    val outputDocs = it.getBsonList
    new OpCommandReply(msgHeader, metadata, commandReply, outputDocs)
  }

  /**
    * Construct OpCommandReply from requestID and metadata
    *
    * @param replyTo  requestID from the original request.
    * @param metadata Available for the system to attach any metadata to internal commands that is not
    *                 part of the command parameters proper, as supplied by the client driver
    * @return OpCommandReply
    */
  def apply(replyTo: Int, metadata: BsonDocument = new BsonDocument()): OpCommandReply = {
    new OpCommandReply(new MsgHeader(
      responseTo = replyTo,
      opCode = OpCommandReplyCode
    ), metadata = metadata)
  }

  /**
    * Construct OpCommandReply from requestID and bytes
    *
    * @param replyTo requestID from the original request.
    * @param content Message bytes.
    * @return OpCommandReply
    */
  def apply(replyTo: Int, content: Seq[Byte]): OpCommandReply = {
    val msgHeader = new MsgHeader(responseTo = replyTo, opCode = OpCommandReplyCode)
    OpCommandReply(msgHeader, content)
  }
}