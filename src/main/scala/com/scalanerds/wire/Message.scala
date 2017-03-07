package com.scalanerds.wire

import akka.util.ByteString
import com.scalanerds.wire.opcodes._

import scala.util.{Failure, Success, Try}

trait Message {
  val msgHeader: MsgHeader

  def serialize: ByteString

  override def toString: String
}

case class OpError(msgHeader: MsgHeader = MsgHeader(),
                   content: Array[Byte] = Array[Byte](),
                   error: String = "Error",
                   raw: Option[ByteString] = None) extends Message {
  override def serialize = ByteString()
}

object Message {
  def apply(data: ByteString): Message = {
    Try {
      val (head, content) = data.toArray.splitAt(16)
      val header = MsgHeader(head)
      header.opCode match {
        case OpCodes.opReply => OpReply(header, content)
        case OpCodes.opMsg => OpMsg(header, content)
        case OpCodes.opUpdate => OpUpdate(header, content)
        case OpCodes.opInsert => OpInsert(header, content)
        case OpCodes.opQuery => OpQuery(header, content)
        case OpCodes.opGetMore => OpGetMore(header, content)
        case OpCodes.opDelete => OpDelete(header, content)
        case OpCodes.opKillCursor => OpKillCursor(header, content)
        case OpCodes.opCommand => OpCommand(header, content)
        case OpCodes.opCommandReply => OpCommandReply(header, content)
        case _ => OpError(header, content)
      }
    } match {
      case Success(message) => message
      case Failure(error) => new OpError(error = error.getMessage, raw = Some(data))
    }
  }
}
