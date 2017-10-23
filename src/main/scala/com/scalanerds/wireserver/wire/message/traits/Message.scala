package com.scalanerds.wireserver.wire.message.traits

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.{MsgHeader, OpError}
import com.scalanerds.wireserver.wire.opcodes._
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson.json.{JsonMode, JsonWriterSettings}

import scala.util.Try

/**
  * Trait for monguard messages
  */
trait Message {
  val msgHeader: MsgHeader
  val jsonSettings = new JsonWriterSettings(JsonMode.STRICT, "    ", "\\n")

  def contentSerialize: Seq[Byte]

  override def toString: String

  def serialize: ByteString = {
    msgHeader.raw.getOrElse {
      val content: Seq[Byte] = msgHeader.serialize ++ contentSerialize
      val raw: ByteString = ByteString((content.length + 4).toByteArray ++ content)
      msgHeader.raw = Some(raw)
      raw
    }
  }
}

/**
  * Mongo message
  *
  * when instantiated the ByteString is parsed in the appropiate type of message
  * Message types:
  *   - OpReply
  *   - OpMsg
  *   - OpUpdate
  *   - OpInsert
  *   - OpQuery
  *   - OpGetMore
  *   - OpDelete
  *   - OpKillCursor
  *   - OpCommand
  *   - OpCommandReply
  *
  *   - OpError used when
  */
object Message {
  def apply(raw: ByteString): Message = {

    Try(raw.toList.splitAt(16)).toOption.collect {
      case ((head, content)) =>
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
    }.getOrElse(OpError(error = "Error parsing ByteString", raw = Some(raw)))
  }
}