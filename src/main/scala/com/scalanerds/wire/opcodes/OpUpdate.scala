package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wire.conversions._
import com.scalanerds.wire.{Message, MsgHeader}
import com.scalanerds.utils.Utils._
import org.bson.BSONObject

object OpUpdate {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpUpdate = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpUpdateFlags(it.getInt)
    val bson = it.getBsonArray
    val selector = bson(0)
    val update = bson(1)
    new OpUpdate(msgHeader, fullCollectionName, flags, selector, update, reserved)
  }
}

class OpUpdate(val msgHeader: MsgHeader,
               val fullCollectionName: String,
               val flags: OpUpdateFlags,
               val selector: BSONObject,
               val update: BSONObject,
               val reserved: Int = 0) extends Message {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      fullCollectionName.toByteArray ++
      flags.serialize ++
      selector.toByteArray ++
      update.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}

object OpUpdateFlags {
  def apply(raw: Int): OpUpdateFlags = {
    val bytes = raw.toByteArray
    new OpUpdateFlags(
      upsert = bytes(0),
      multiUpdate = bytes(1)
    )
  }
}

class OpUpdateFlags(val upsert: Boolean = false,
                    val multiUpdate: Boolean = false) {
  def serialize: ByteString = {
    Array(upsert, multiUpdate).asInstanceOf[ByteString]
  }
}