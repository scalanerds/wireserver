package com.mongonerds.wire.opcodes

import akka.util.ByteString
import com.mongonerds.utils.Utils._
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpInsert {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpInsert = {
    val it = content.iterator
    val flags = it.getInt
    val fullCollectionName = it.getString
    val documents = it.getBsonArray
    new OpInsert(msgHeader, flags, fullCollectionName, documents)
  }
}

class OpInsert(val msgHeader: MsgHeader, flags: Int, fullCollectionName : String,
               val documents: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      flags.toByteArray ++
      fullCollectionName.toByteArray ++
      documents.toByteArray
    ByteString((content.length + 4).toByteArray ++ content)
  }
}