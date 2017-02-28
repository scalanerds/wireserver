package com.mongonerds.wire.opcodes


import akka.util.ByteString
import com.mongonerds.utils.Utils._
import com.mongonerds.wire.{Message, MsgHeader}

object OpGetMore {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpGetMore = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val numberToReturn = it.getInt
    val cursorID = it.getLong
    new OpGetMore(msgHeader, fullCollectionName, numberToReturn, cursorID, reserved)
  }
}

class OpGetMore(val msgHeader: MsgHeader,
                val fullCollectionName: String,
                val numberToReturn: Int,
                val cursorID: Long,
                val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      fullCollectionName.toByteArray ++
      numberToReturn.toByteArray ++
      cursorID.toByteArray
    ByteString((content.length + 4).toByteArray ++ content)
  }
}