package com.mongonerds.wire

import java.nio.ByteOrder.LITTLE_ENDIAN

import akka.util.ByteString

class Message(val messageLength: Int,
              val requestId: Int,
              val responseTo: Int,
              val opCode: Int) {

}

object Message {

  def apply(data: ByteString) = {
    val it = data.iterator
    val messageLength = it.getInt(LITTLE_ENDIAN)
    val requestId = it.getInt(LITTLE_ENDIAN)
    val responseTo = it.getInt(LITTLE_ENDIAN)
    val opCode = it.getInt(LITTLE_ENDIAN)
    new Message(messageLength, requestId, responseTo, opCode)
  }
}
