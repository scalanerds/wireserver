package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Request
import com.scalanerds.wireserver.wire.opcodes.flags.OpInsertFlags
import org.bson.BsonDocument


/**
  * Mongo client request
  *
  * Code 2002
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpInsert message is used to insert one or more documents into a collection.
  *
  * Flags
  *   - 0 corresponds to ContinueOnError. If set, the database will not stop processing a bulk insert
  *     if one fails (eg due to duplicate IDs). This makes bulk insert behave similarly to a series of
  *     single inserts, except lastError will be set if any insert fails, not just the last one. If
  *     multiple errors occur, only the most recent will be reported by getLastError.
  *   - 1-31 are reserved. Must be set to 0.
  *
  * @param msgHeader          Message header.
  * @param flags              Bit vector to specify flags for the operation.
  * @param fullCollectionName The full collection name; i.e. namespace. The full collection name
  *                           is the concatenation of the database name with the collection name,
  *                           using a . for the concatenation. For example, for the database foo
  *                           and the collection bar, the full collection name is foo.bar.
  * @param documents          One or more documents to insert into the collection. If there are more than one,
  *                           they are written to the socket in sequence, one after another.
  */
class OpInsert(val msgHeader: MsgHeader,
    val flags: OpInsertFlags,
    val fullCollectionName: String,
    val documents: List[BsonDocument]) extends Request {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  /** serialize message into bytes */
  override def contentSerialize: Seq[Byte] = {
    flags.serialize ++
      fullCollectionName.toByteList ++
      documents.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |flags: $flags
       |fullCollectionName : $fullCollectionName
       |documents: ${documents.mkString("\n")}
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpInsert]

  override def equals(other: Any): Boolean = other match {
    case that: OpInsert =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        flags == that.flags &&
        fullCollectionName == that.fullCollectionName &&
        (documents sameElements that.documents)
    case _ => false
  }

  override def hashCode(): Int = {
    Seq(msgHeader.opCode, flags, fullCollectionName, documents)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }

  override def realm: String = fullCollectionName

  override def command: String = "insert"

}


object OpInsert {
  /**
    * Construct OpInsert
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpInsert
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): Option[OpInsert] = {
    val it = content.iterator
    for {
      flagInt <- it.getIntOption
      flags = OpInsertFlags(flagInt)
      fullCollectionName <- it.getStringOption
      documents <- it.getBsonListOption
    } yield new OpInsert(msgHeader, flags, fullCollectionName, documents)
  }
}
