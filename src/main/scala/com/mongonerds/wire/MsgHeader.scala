package com.mongonerds.wire

import com.mongonerds.utils.Utils.IntToByte

class MsgHeader(val requestId: Int,
                val responseTo: Int,
                val opCode: Int) {
  def serialize: Array[Byte] = {
    Array(requestId, responseTo, opCode).toByteArray
  }
}
