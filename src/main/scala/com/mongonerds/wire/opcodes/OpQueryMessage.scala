package com.mongonerds.wire.opcodes

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString
import com.mongonerds.wire.{Message, MsgHeader}

object OpQueryMessage {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpQueryMessage = {
    var arr = content.splitAt(4)
    val flags = BigInt(arr._1.reverse).toInt
    arr = arr._2.span(_ != 0)
    val fullCollectionName = new String(arr._1, "UTF-8")
    arr = arr._2.drop(1).splitAt(4)
    val numberToSkip = BigInt(arr._1.reverse).toInt
    arr = arr._2.splitAt(4)
    val numberToReturn = BigInt(arr._1.reverse).toInt
    new OpQueryMessage(msgHeader, flags, fullCollectionName, numberToSkip, numberToReturn)
  }
}

class OpQueryMessage(val msgHeader: MsgHeader,
                     val flags: Int,
                     val fullCollectionName: String,
                     val numberToSkip: Int,
                     val numberToReturn: Int) extends Message {

  override def serialize = {
    val byteStringBuilder = ByteString.newBuilder
    byteStringBuilder.putInts(msgHeader.toArr)(LITTLE_ENDIAN)
    byteStringBuilder.result()
  }
}



