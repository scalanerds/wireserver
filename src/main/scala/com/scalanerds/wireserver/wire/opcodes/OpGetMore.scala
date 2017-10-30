package com.scalanerds.wireserver.wire.opcodes


import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Message


/**
  * Mongo client request
  *
  * Code 2005
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpGetMore message is used to query the database for documents in a collection.
  *
  * @param msgHeader          Message header.
  * @param fullCollectionName The full collection name; i.e. namespace. The full collection name
  *                           is the concatenation of the database name with the collection name,
  *                           using a . for the concatenation. For example, for the database foo
  *                           and the collection bar, the full collection name is foo.bar.
  * @param numberToReturn     Limits the number of documents in the first OpReply message to the query. However,
  *                           the database will still establish a cursor and return the cursorID to the client if
  *                           there are more results than numberToReturn. If the client driver offers ‘limit’
  *                           functionality (like the SQL LIMIT keyword), then it is up to the client driver to
  *                           ensure that no more than the specified number of document are returned to the
  *                           calling application. If numberToReturn is 0, the db will used the default return size.
  * @param cursorID           Cursor identifier that came in the OpReply. This must be the value that came from
  *                           the database.
  * @param reserved           Integer value of 0. Reserved for future use.
  */
class OpGetMore(val msgHeader: MsgHeader,
    val fullCollectionName: String,
    val numberToReturn: Int,
    val cursorID: Long,
    val reserved: Int = 0) extends Message {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Seq[Byte] = {
    reserved.toByteList ++
      fullCollectionName.toByteList ++
      numberToReturn.toByteList ++
      cursorID.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |numberToReturn: $numberToReturn
       |cursorID: $cursorID
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpGetMore]

  override def equals(other: Any): Boolean = other match {
    case that: OpGetMore =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        numberToReturn == that.numberToReturn &&
        cursorID == that.cursorID
    case _ => false
  }

  override def hashCode(): Int = {
    Seq(msgHeader.opCode, fullCollectionName, numberToReturn, cursorID)
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }
}

object OpGetMore {
  /**
    * Construct OpGetMore
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpGetMore
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpGetMore = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val numberToReturn = it.getInt
    val cursorID = it.getLong
    new OpGetMore(msgHeader, fullCollectionName, numberToReturn, cursorID, reserved)
  }
}
