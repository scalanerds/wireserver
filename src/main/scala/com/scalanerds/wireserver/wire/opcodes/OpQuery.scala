package com.scalanerds.wireserver.wire.opcodes


import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.{Request, WithReply}
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import com.scalanerds.wireserver.wire.opcodes.flags.OpQueryFlags
import org.bson.{BsonDocument, BsonString}

/**
  * Mongo client request
  *
  * Code 2004
  *
  * The OpQuery message is used to query the database for documents in a collection.
  *
  * Flags
  *  - 0 is reserved. Must be set to 0.
  *  - 1 corresponds to TailableCursor. Tailable means cursor is not closed when the last data
  *    is retrieved. Rather, the cursor marks the final object’s position. You can resume using
  *    the cursor later, from where it was located, if more data were received. Like any “latent
  *    cursor”, the cursor may become invalid at some point (CursorNotFound) – for example
  *    if the final object it references were deleted.
  *  - 2 corresponds to SlaveOk. Allow query of replica slave. Normally these return an error
  *    except for namespace “local”.
  *  - 3 corresponds to opLogReplay. Internal replication use only - driver should not set.
  *  - 4 corresponds to noCursorTimeout. The server normally times out idle cursors after an
  *    inactivity period (10 minutes) to prevent excess memory use. Set this option to prevent that.
  *  - 5 corresponds to AwaitData. Use with TailableCursor. If we are at the end of the data,
  *    block for a while rather than returning no data. After a timeout period, we do return as normal.
  *  - 6 corresponds to Exhaust. Stream the data down full blast in multiple “more” packages,
  *    on the assumption that the client will fully read all data queried. Faster when you are
  *    pulling a lot of data and know you want to pull it all down. Note: the client is not allowed
  *    to not read all the data unless it closes the connection.
  *  - 7 corresponds to Partial. Get partial results from a mongos if some shards are down
  *    (instead of throwing an error)
  *  - 8-31 are reserved. Must be set to 0.
  *
  * @param msgHeader            Message header
  * @param flags                Bit vector to specify flags for the operation.
  * @param fullCollectionName   The full collection name; i.e. namespace. The full collection name is
  *                             the concatenation of the database name with the collection name,
  *                             using a . for the concatenation. For example, for the database foo
  *                             and the collection bar, the full collection name is foo.bar.
  * @param numberToSkip         Sets the number of documents to omit - starting from the first document
  *                             in the resulting dataset - when returning the result of the query.
  * @param numberToReturn       Limits the number of documents in the first OP_REPLY message to the query.
  *                             However, the database will still establish a cursor and return the cursorID
  *                             to the client if there are more results than numberToReturn.
  * @param query                BSON document that represents the query. The query will contain one or more elements,
  *                             all of which must match for a document to be included in the result set. Possible
  *                             elements include $query, $orderby, $hint, $explain, and $snapshot.
  * @param returnFieldsSelector Optional. BSON document that limits the fields in the returned documents.
  *                             The returnFieldsSelector contains one or more elements, each of which is
  *                             the name of a field that should be returned, and and the integer value 1.
  *                             In JSON notation, a returnFieldsSelector to limit to the fields a, b and c
  *                             would be:  { a : 1, b : 1, c : 1}
  */
class OpQuery(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opQuery),
    val flags: OpQueryFlags = new OpQueryFlags(),
    val fullCollectionName: String,
    val numberToSkip: Int = 0,
    val numberToReturn: Int = 1,
    val query: BsonDocument = new BsonDocument(),
    val returnFieldsSelector: Option[BsonDocument] = None)
  extends Request with WithReply {

  override def reply(content: Seq[Byte]): OpReply = {
    OpReply(msgHeader.requestId, content = content)
  }

  override def reply(docs: List[BsonDocument]): OpReply = {
    OpReply(msgHeader.requestId, documents = docs)
  }

  override def reply(doc: BsonDocument): OpReply = reply(List(doc))

  override def reply(json: String): OpReply = reply(BsonDocument.parse(json))

  override def contentSerialize: Seq[Byte] = {
    flags.serialize ++
      fullCollectionName.toByteList ++
      List(numberToSkip, numberToReturn).toByteList ++
      query.toByteList ++
      returnFieldsSelector.map(_.toByteList).getOrElse(Seq[Byte]())
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

  /** OpQuery hashcode
    *
    * @return hashcode
    */
  override def hashCode(): Int = {
    // gather in a list all the data required to calculate the hashcode
    val state =
      Seq(msgHeader.opCode, flags, fullCollectionName, numberToSkip, numberToReturn,
        query.toJson, returnFieldsSelector.map(_.toJson).getOrElse(""))

    state.map(_.hashCode()).foldLeft(0)(31 * _ + _)
  }

  /** generate the collection name
    *
    * @return collection name
    */
  def collection: String = {
    val chunks: Array[String] = fullCollectionName.split("\\.")

    val collectionName = for {
      head <- chunks.lastOption if head == "$cmd"
    } yield {
      val dbName = chunks(0)
      val collectionName =
        query.get(command) match {
          case s: BsonString => "." + s.asString().getValue
          case _ => ""
        }

      dbName + collectionName
    }
    collectionName.getOrElse(fullCollectionName)
  }


  override def realm: String = collection

  override def command: String = {
    query
      .keySet
      .toArray
      .headOption
      .flatMap(_.asInstanceOfOption[String])
      .getOrElse("find")
  }
}


object OpQuery {
  /**
    * Construct OpQuery from MsgHeader and bytes
    *
    * @param msgHeader Message header
    * @param content   Message bytes
    * @return OpQuery
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpQuery = {
    val it = content.iterator
    val flags = OpQueryFlags(it.getInt)
    val fullCollectionName = it.getString
    val numberToSkip = it.getInt
    val numberToReturn = it.getInt
    val bson = it.getBsonList
    val query = bson.head
    val returnFieldSelector = if (bson.length == 2) Some(bson(1)) else None
    new OpQuery(msgHeader, flags, fullCollectionName, numberToSkip, numberToReturn, query, returnFieldSelector)
  }
}
