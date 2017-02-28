package com.mongonerds.wire.opcodes

import akka.util.ByteString
import com.mongonerds.utils.Utils._
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpDelete {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpDelete = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = it.getInt
    val selector = it.getBson
    new OpDelete(msgHeader, fullCollectionName, flags, selector, reserved)
  }
}

class OpDelete(val msgHeader: MsgHeader,
               val fullCollectionName: String,
               val flags: Int,
               val selector: BSONObject,
               val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      fullCollectionName.toByteArray ++
      flags.toByteArray ++
      selector.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}