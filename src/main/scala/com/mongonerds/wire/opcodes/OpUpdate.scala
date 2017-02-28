package com.mongonerds.wire.opcodes

import akka.util.ByteString
import com.mongonerds.utils.Utils._
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpUpdate {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpUpdate = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = it.getInt
    val bson = it.getBsonArray
    val selector = bson(0)
    val update = bson(1)
    new OpUpdate(msgHeader, fullCollectionName, flags, selector, update, reserved)
  }
}

class OpUpdate(val msgHeader: MsgHeader, val fullCollectionName: String,
               val flags: Int, selector: BSONObject,
               val update: BSONObject, reserved: Int = 0) extends Message {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      fullCollectionName.toCString ++
      flags.toByteArray ++
      selector.toByteArray ++
      update.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}