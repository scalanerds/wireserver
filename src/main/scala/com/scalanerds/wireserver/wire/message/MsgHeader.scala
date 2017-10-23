package com.scalanerds.wireserver.wire.message

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions.{IntToByte, _}

class MsgHeader(val requestId: Int = MsgHeader.issueRequestId(),
                var responseTo: Int = 0,
                val opCode: Int,
                var raw: Option[ByteString] = None,
                //The total size of the message in bytes.
                // This total includes the 4 bytes that holds the message length.
                var messageLength: Option[Int] = None) {

  def copy: MsgHeader = {
    new MsgHeader(requestId, responseTo, opCode)
  }

  def serialize: Seq[Byte] = {
    List(requestId, responseTo, opCode).toByteList
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
  private var lastRequestId = 0
  private def issueRequestId(): Int = { lastRequestId += 1; lastRequestId }

  def apply(header: Seq[Byte], raw: ByteString): MsgHeader = {
    val it = header.iterator
    val length = it.getInt
    val requestId = it.getInt
    val responseTo = it.getInt
    val opCode = it.getInt
    new MsgHeader(requestId, responseTo, opCode, Some(raw), Some(length))
  }
  // Error message header
  def apply() = new MsgHeader(0,0,0)
}