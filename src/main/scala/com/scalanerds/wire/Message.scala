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

  override def toString: String =
    s"""
      |Error
      |${msgHeader.serialize}
      |content : ${content.mkString(", ")}
      |error: $error
      |raw: ${raw.getOrElse(Nil).mkString(", ")}
    """.stripMargin
}

object Message {
  def apply(data: ByteString): Message = {
    Try {
      val (head, content) = data.toArray.splitAt(16)
      val header = MsgHeader(head)
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
      case Failure(error) => OpError(error = error.getMessage, raw = Some(data))
    }
  }
}
