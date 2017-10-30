package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Request
import com.scalanerds.wireserver.wire.opcodes.flags.OpDeleteFlags
import org.bson.BsonDocument


/**
  * Mongo client request
  *
  * Code 2006
  *
  * The OpDelete message is used to remove one or more documents from a collection.
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpDelete message is used to remove one or more documents from a collection.
  *
  * Flags
  *   - 0 corresponds to SingleRemove. If set, the database will remove only the first matching
  *     document in the collection. Otherwise all matching documents will be removed.
  *   - 1-31 are reserved. Must be set to 0.
  *
  * @param msgHeader          Message header.
  * @param fullCollectionName The full collection name; i.e. namespace. The full collection name
  *                           is the concatenation of the database name with the collection name,
  *                           using a . for the concatenation. For example, for the database foo
  *                           and the collection bar, the full collection name is foo.bar.
  * @param flags              Bit vector to specify flags for the operation.
  * @param selector           BSON document that represent the query used to select the documents to be removed.
  *                           The selector will contain one or more elements, all of which must match for a
  *                           document to be removed from the collection.
  * @param reserved           Integer value of 0. Reserved for future use.
  */
class OpDelete(val msgHeader: MsgHeader,
    val fullCollectionName: String,
    val flags: OpDeleteFlags,
    val selector: BsonDocument,
    val reserved: Int = 0) extends Request {

  override def serialize: ByteString = {
    val content: Seq[Byte] = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Seq[Byte] = {
    reserved.toByteList ++
      fullCollectionName.toByteList ++
      flags.serialize ++
      selector.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |flags: $flags
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpDelete]

  override def equals(other: Any): Boolean = other match {
    case that: OpDelete =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        flags == that.flags &&
        selector == that.selector
    case _ => false
  }

  override def hashCode(): Int = {
    Seq(msgHeader.opCode, fullCollectionName, flags, selector)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }

  override def realm: String = fullCollectionName

  override def command: String = "delete"

}


object OpDelete {
  /**
    * Construct OpDelete
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpDelete
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpDelete = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpDeleteFlags(it.getInt)
    val selector = it.getBson
    new OpDelete(msgHeader, fullCollectionName, flags, selector, reserved)
  }
}
