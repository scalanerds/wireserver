package com.mongonerds.wire.opcodes

import akka.util.ByteString
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}
import com.mongonerds.utils.Utils._

object OpQuery {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpQuery = {
    val it = content.iterator
    val flags = it.cTake(4).toArray.toInt
    val fullCollectionName = it.takeWhile(_ != 0).toArray.toUTFString
    val numberToSkip = it.cTake(4).toArray.toInt
    val numberToReturn = it.cTake(4).toArray.toInt
    // take instead of cTake to avoid consuming the bytes
    val bsonLen = it.take(4).toArray
//    val query = BSON.decode(it.cTake(bsonLen.toInt).toArray)
//    val returnFieldSelector = if (it.length > 4) Option(BSON.decode(it.toArray)) else None
    val bson = it.toArray.toBSONArray
    val query = bson(0)
    val returnFieldSelector = if(bson.length == 2) Some(bson(1)) else None
    new OpQuery(msgHeader, flags, fullCollectionName, numberToSkip, numberToReturn, query, returnFieldSelector)
  }
}

class OpQuery(val msgHeader: MsgHeader,
              val flags: Int,
              val fullCollectionName: String,
              val numberToSkip: Int,
              val numberToReturn: Int,
              val query: BSONObject,
              val returnFieldsSelector: Option[BSONObject] = None) extends Message {

  override def serialize: ByteString = {
    var content = msgHeader.serialize ++
      flags.toByteArray() ++
      fullCollectionName.toCString ++
      Array(numberToSkip, numberToSkip, numberToReturn).toByteArray ++
      query.toByteArray
    if (returnFieldsSelector.nonEmpty)
      content ++= returnFieldsSelector.get.toByteArray
    ByteString((content.length + 4).toByteArray() ++ content)
  }
}



