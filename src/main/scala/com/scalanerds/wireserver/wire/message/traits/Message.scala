package com.scalanerds.wireserver.wire.message.traits

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.{MsgHeader, OpError}
import com.scalanerds.wireserver.wire.opcodes._
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson.json.{JsonMode, JsonWriterSettings}

import scala.util.{Failure, Success, Try}

trait Message {
  val msgHeader: MsgHeader
  val jsonSettings = new JsonWriterSettings(JsonMode.STRICT, "    ", "\\n")

  def contentSerialize: Array[Byte]

  override def toString: String

  def serialize: ByteString = {
    if (msgHeader.raw.isEmpty) {
      val content = msgHeader.serialize ++ contentSerialize
      msgHeader.raw = Some(ByteString((content.length + 4).toByteArray ++ content))
    }
    msgHeader.raw.get
  }
}

object Message {
  def apply(raw: ByteString): Message = {
    Try {
      val (head, content) = raw.toArray.splitAt(16)
      val header = MsgHeader(head, raw)
      header.opCode match {
        case OPCODES.opReply => OpReply(header, content)
        case OPCODES.opMsg => OpMsg(header, content)
        case OPCODES.opUpdate => OpUpdate(header, content)
        case OPCODES.opInsert => OpInsert(header, content)
        case OPCODES.opQuery => OpQuery(header, content)
        case OPCODES.opGetMore => OpGetMore(header, content)
        case OPCODES.opDelete => OpDelete(header, content)
        case OPCODES.opKillCursor => OpKillCursor(header, content)
        case OPCODES.opCommand => OpCommand(header, content)
        case OPCODES.opCommandReply => OpCommandReply(header, content)
        case _ => OpError(header, content)
      }
    } match {
      case Success(message) => message
      case Failure(error) => OpError(error = error.getMessage, raw = Some(raw))
    }
  }
}