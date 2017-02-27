package com.mongonerds.wire.opcodes

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}

object OpQuery {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpQuery = {
    var arr = content.splitAt(4)
    val flags = BigInt(arr._1.reverse).toInt
    arr = arr._2.span(_ != 0)
    val fullCollectionName = new String(arr._1, "UTF-8")

    arr = arr._2.drop(1).splitAt(4)
    val numberToSkip = BigInt(arr._1.reverse).toInt
    arr = arr._2.splitAt(4)
    val numberToReturn = BigInt(arr._1.reverse).toInt
    val bsonLength = BigInt(arr._2.take(4).reverse).toInt
    arr = arr._2.splitAt(bsonLength)
    val query = BSON.decode(arr._1)
    val returnFieldSelector = if (arr._2.length > 4) BSON.decode(arr._2) else null
    new OpQuery(msgHeader, flags, fullCollectionName, numberToSkip, numberToReturn, query, returnFieldSelector)
  }
}

class OpQuery(val msgHeader: MsgHeader,
              val flags: Int,
              val fullCollectionName: String,
              val numberToSkip: Int,
              val numberToReturn: Int,
              val query: BSONObject,
              val returnFieldsSelector: BSONObject = null) extends Message {

  override def serialize: ByteString = {
    var content = msgHeader.serialize ++
      Message.toByteArray(flags) ++
      ByteString.fromString(fullCollectionName) ++
      Array[Byte](0) ++
      Message.intsAsByteArray(numberToSkip, numberToSkip, numberToReturn) ++
      BSON.encode(query)
    if (returnFieldsSelector != null)
      content ++= BSON.encode(returnFieldsSelector)
    ByteString(Message.toByteArray(content.length + 4) ++ content)
  }
}



