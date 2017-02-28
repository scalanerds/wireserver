package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpQuery {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpQuery = {
    val it = content.iterator
    val flags = it.getInt
    val fullCollectionName = it.getString
    val numberToSkip = it.getInt
    val numberToReturn = it.getInt
    val bson = it.getBsonArray
    val query = bson(0)
    val returnFieldSelector = if (bson.length == 2) Some(bson(1)) else None
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
      flags.toByteArray ++
      fullCollectionName.toByteArray ++
      Array(numberToSkip, numberToSkip, numberToReturn).toByteArray ++
      query.toByteArray
    if (returnFieldsSelector.nonEmpty)
      content ++= returnFieldsSelector.get.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}



