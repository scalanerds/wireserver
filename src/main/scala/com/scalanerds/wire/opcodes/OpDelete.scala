package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wire.conversions._
import com.scalanerds.wire.{Message, MsgHeader}
import com.scalanerds.utils.Utils._
import org.bson.BSONObject

object OpDelete {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpDelete = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpDeleteFlags(it.getInt)
    val selector = it.getBson
    new OpDelete(msgHeader, fullCollectionName, flags, selector, reserved)
  }
}

class OpDelete(val msgHeader: MsgHeader,
               val fullCollectionName: String,
               val flags: OpDeleteFlags,
               val selector: BSONObject,
               val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      fullCollectionName.toByteArray ++
      flags.serialize ++
      selector.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}


object OpDeleteFlags {
  def apply(raw: Int): OpDeleteFlags = {
    val bytes = raw.toByteArray
    new OpDeleteFlags(
      bytes(0)
    )
  }
}

class OpDeleteFlags(val singleRemove: Boolean = false) {
  def serialize: ByteString = {
    Array(singleRemove).asInstanceOf[ByteString]
  }
}