package com.scalanerds.wireserver.wire.message

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.opcodes.OpCode

/** Mongo wire message header
  *
  *
  * @param requestId request ID
  * @param responseTo id of the message that is being replied
  * @param opCode Mongo wire OpCode
  * @param raw message bytestring
  * @param messageLength message length
  */
class MsgHeader(val requestId: Int = MsgHeader.issueRequestId(),
                var responseTo: Int = 0,
                val opCode: OpCode,
                var raw: Option[ByteString] = None,
                //The total size of the message in bytes.
                // This total includes the 4 bytes that holds the message length.
                var messageLength: Option[Int] = None) {

  def copy: MsgHeader = {
    new MsgHeader(requestId, responseTo, opCode)
  }

  def serialize: Seq[Byte] = {
    List(requestId, responseTo, opCode.value).toByteList
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

  /** keep track of the request number */
  private var lastRequestId = 0

  /** issue a request ID */
  private def issueRequestId(): Int = { lastRequestId += 1; lastRequestId }

  def apply(header: Seq[Byte], raw: ByteString): Option[MsgHeader] = {
    val it = header.iterator
    for {
      length <- it.getIntOption
      requestId <- it.getIntOption
      responseTo <- it.getIntOption
      opCodeInt <- it.getIntOption
      opCode <- OpCode(opCodeInt)
    } yield {
      new MsgHeader(requestId, responseTo, opCode, Some(raw), Some(length))
    }
  }
}
