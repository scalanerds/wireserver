package com.scalanerds.wire

import com.scalanerds.utils.Utils.IntToByte

class MsgHeader(val requestId: Int,
                val responseTo: Int,
                val opCode: Int) {
  def serialize: Array[Byte] = {
    Array(requestId, responseTo, opCode).toByteArray
  }
}
