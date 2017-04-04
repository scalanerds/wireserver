package com.scalanerds.wireserver.wire

import akka.util.ByteString
import com.scalanerds.wireserver.wire.opcodes._
import com.scalanerds.wireserver.utils.Utils._

import scala.util.{Failure, Success, Try}

trait Message {
  val msgHeader: MsgHeader

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

trait Request extends Message {
  def realm  : String
  def command: String
}

trait Response extends Message

case class OpError(msgHeader: MsgHeader = MsgHeader(),
              content: Array[Byte] = Array[Byte](),
              error: String = "Error",
              raw: Option[ByteString] = None) extends Message {

  def realm = null

  override def contentSerialize: Array[Byte] = Array[Byte]()

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
