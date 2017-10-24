package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Request
import com.scalanerds.wireserver.wire.opcodes.flags.OpUpdateFlags
import org.bson.BsonDocument

/**
  * Mongo client request
  *
  * Code 2001
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpUpdate message is used to update a document in a collection.
  *
  * Flags
  *   - 0 corresponds to Upsert. If set, the database will insert the supplied object into the
  *     collection if no matching document is found.
  *   - 1 corresponds to MultiUpdate.If set, the database will update all matching objects in the
  *     collection. Otherwise only updates first matching document.
  *   - 2-31 are reserved. Must be set to 0.
  *
  * @param msgHeader          Message header.
  * @param fullCollectionName The full collection name; i.e. namespace. The full collection name
  *                           is the concatenation of the database name with the collection name,
  *                           using a . for the concatenation. For example, for the database foo
  *                           and the collection bar, the full collection name is foo.bar.
  * @param flags              Bit vector to specify flags for the operation.
  * @param selector           BSON document that specifies the query for selection of the document to update.
  * @param update             BSON document that specifies the update to be performed.
  * @param reserved           Integer value of 0. Reserved for future use.
  */
class OpUpdate(val msgHeader: MsgHeader,
    val fullCollectionName: String,
    val flags: OpUpdateFlags,
    val selector: BsonDocument,
    val update: BsonDocument,
    val reserved: Int = 0) extends Request {

  /** serialize message into bytes */
  override def contentSerialize: Seq[Byte] = {
    reserved.toByteList ++
      fullCollectionName.toByteList ++
      flags.serialize ++
      selector.toByteList ++
      update.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |flags: $flags
       |selector: $selector
       |update: $update
     """.stripMargin
  }

  override def equals(other: Any): Boolean = other match {
    case that: OpUpdate =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        flags == that.flags &&
        selector == that.selector &&
        update == that.update
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpUpdate]

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, fullCollectionName, flags, selector, update)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  // collection name
  override def realm: String = fullCollectionName

  override def command: String = "update"

}

object OpUpdate {
  /**
    * Construct OpUpdate
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpUpdate
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpUpdate = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpUpdateFlags(it.getInt)
    val bson = it.getBsonList
    val selector = bson.head
    val update = bson(1)
    new OpUpdate(msgHeader, fullCollectionName, flags, selector, update, reserved)
  }
}
