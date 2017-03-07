package com.scalanerds.wire

import com.scalanerds.utils.Utils.{IntToByte, _}

class MsgHeader(val requestId: Int,
                val responseTo: Int,
                val opCode: Int,
                //The total size of the message in bytes.
                // This total includes the 4 bytes that holds the message length.
                var messageLength: Option[Int] = None) {
  def serialize: Array[Byte] = {
    Array(requestId, responseTo, opCode).toByteArray
  }

  override def toString: String = {
    s"""
       |Header
       |requestId: $requestId
       |responseTo: $responseTo
       |opCode: $opCode
       """.stripMargin
  }
}

object MsgHeader {
  def apply(data: Array[Byte]): MsgHeader = {
    val it = data.iterator
    val length = it.getInt
    val requestId = it.getInt
    val responseTo = it.getInt
    val opCode = it.getInt
    new MsgHeader(requestId, responseTo, opCode, Some(length))
  }
  // Error message header
  def apply() = new MsgHeader(0,0,0)
}