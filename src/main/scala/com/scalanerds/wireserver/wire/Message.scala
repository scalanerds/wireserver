package com.scalanerds.wireserver.wire

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.opcodes._
import org.bson.BsonDocument
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

trait Request extends Message {
  def realm  : String
  def command: String

  def JSON: String = {
    this match {
      case opQuery: OpQuery =>
        opQuery.query.toJson(jsonSettings)
      case opCommand: OpCommand =>
        opCommand.metadata.toJson(jsonSettings)
      case opInsert: OpInsert => opInsert.documents.map(_.toJson(jsonSettings)).mkString("[", ",", "]")
      case opDelete: OpDelete => opDelete.selector.toJson(jsonSettings)
      case opUpdate: OpUpdate => opUpdate.selector.toJson(jsonSettings)
    }
  }

  def shiftDatabase(database: String): Request = {
    val shifted = this match {
      case cmd: OpQuery =>
        val fullCollectionName = database + '.' + cmd.fullCollectionName.split('.')(1)
        new OpQuery(cmd.msgHeader, cmd.flags, fullCollectionName, cmd.numberToSkip,
          cmd.numberToReturn, cmd.query, cmd.returnFieldsSelector)
      case cmd: OpCommand =>
        new OpCommand(cmd.msgHeader, database, cmd.commandName, cmd.metadata, cmd.commandArgs, cmd.inputDocs)

      case cmd: OpInsert =>
        val fullCollectionName = database + '.' + cmd.fullCollectionName.split('.')(1)
        new OpInsert(cmd.msgHeader,cmd.flags, fullCollectionName, cmd.documents)
      case cmd: OpDelete =>
        val fullCollectionName = database + '.' + cmd.fullCollectionName.split('.')(1)
        new OpDelete(cmd.msgHeader, fullCollectionName, cmd.flags, cmd.selector, cmd.reserved)
      case cmd: OpUpdate =>
        val fullCollectionName = database + '.' + cmd.fullCollectionName.split('.')(1)
        new OpUpdate(cmd.msgHeader, fullCollectionName, cmd.flags, cmd.selector, cmd.update, cmd.reserved)
    }
    shifted.msgHeader.raw = None
    shifted
  }

  def payload: Option[BsonDocument] = this match {
    case op: OpQuery => Some(op.query)
    case op: OpCommand => Some(op.metadata)
    case _: OpInsert => None
    case _: OpDelete => None
    case _: OpUpdate => None
  }



  override def hashCode(): Int = this match {
    case op: OpCommand => op.hashCode()
    case op: OpQuery => op.hashCode()
    case op: OpInsert => op.hashCode()
    case op: OpDelete => op.hashCode()
    case op: OpUpdate => op.hashCode()
    case _ => super.hashCode()
  }
}

trait Response extends Message {
  def JSON: String = {
    this match {
      case opReply: OpReply =>
        opReply.documents
        .map((doc: BsonDocument) => doc.toJson(jsonSettings))
        .mkString("[\\n    ", ",\\n    ", "\\n]")
      case opCommandReply: OpCommandReply =>
        opCommandReply.commandReply.toJson(jsonSettings)
    }
  }
}

trait WithReply {
  def reply(content: Array[Byte]): Response
  def reply(docs: Array[BsonDocument]): Response
  def reply(doc: BsonDocument): Response
  def reply(json: String): Response
}

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
