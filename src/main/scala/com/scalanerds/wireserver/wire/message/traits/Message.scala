package com.scalanerds.wireserver.wire.message.traits

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.opcodes._
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
      val raw: ByteString =
        ByteString((content.length + 4).toByteArray ++ content)
      msgHeader.raw = Some(raw)
      raw
    }
  }
}

/**
  * Mongo message
  *
  * when instantiated the ByteString is parsed into the appropriate type of message
  * Message types:
  *   OpCode name        value
  *   - OpReply          1
  *   - OpMsg            1000
  *   - OpUpdate         2001
  *   - OpInsert         2002
  *   - OpQuery          2004
  *   - OpGetMore        2005
  *   - OpDelete         2006
  *   - OpKillCursor     2007
  *   - OpCommand        2010
  *   - OpCommandReply   2011
  *
  */
object Message {
  def apply(raw: ByteString): Option[Message] = {
    Try(raw.toList.splitAt(16)).toOption.collect {
      case ((head, content)) => (MsgHeader(head, raw), content)
    } collect {
      case (Some(header), content) => (header, content, header.opCode)
    } collect {
      case (header, content, OpReplyCode)   => OpReply(header, content)
      case (header, content, OpMsgCode)     => OpMsg(header, content)
      case (header, content, OpUpdateCode)  => OpUpdate(header, content)
      case (header, content, OpInsertCode)  => OpInsert(header, content)
      case (header, content, OpQueryCode)   => OpQuery(header, content)
      case (header, content, OpGetMoreCode) => OpGetMore(header, content)
      case (header, content, OpDeleteCode)  => OpDelete(header, content)
      case (header, content, OpKillCursorsCode) =>
        OpKillCursors(header, content)
      case (header, content, OpCommandCode) => OpCommand(header, content)
      case (header, content, OpCommandReplyCode) =>
        OpCommandReply(header, content)
    } flatten
  }
}
