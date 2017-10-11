package com.scalanerds.wireserver.wire.opcodes


import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.{Request, WithReply}
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import com.scalanerds.wireserver.wire.opcodes.flags.OpQueryFlags
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
  extends Request with WithReply {

  override def reply(content: Array[Byte]): OpReply = {
    OpReply(msgHeader.requestId, content = content)
  }

  override def reply(docs: Array[BsonDocument]): OpReply = {
    OpReply(msgHeader.requestId, documents = docs)
  }

  override def reply(doc: BsonDocument): OpReply = reply(Array(doc))

  override def reply(json: String): OpReply = reply(BsonDocument.parse(json))

  override def contentSerialize: Array[Byte] = {
    flags.serialize ++
      fullCollectionName.toByteArray ++
      Array(numberToSkip, numberToReturn).toByteArray ++
      query.toByteArray ++
      returnFieldsSelector.map(_.toByteArray).getOrElse(Array[Byte]())
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
    * @return
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

  override def command:String = {
    query
      .keySet
      .toArray
      .headOption
      .flatMap {
        _.asInstanceOfOption[String]
      }
      .getOrElse("find")
  }
}






