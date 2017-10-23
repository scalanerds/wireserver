package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire._
import com.scalanerds.wireserver.wire.message.traits.{Request, WithReply}
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson._

object OpCommand {
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpCommand = {
    val it = content.iterator
    val database = it.getString
    val commandName = it.getString
    val metadata = it.getBson
    val commandArgs = it.getBson
    val inputDocs = it.getBsonList
    new OpCommand(msgHeader, database, commandName, metadata, commandArgs, inputDocs)
  }
}

class OpCommand(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opCommand),
                val database: String,
                val commandName: String,
                val metadata: BsonDocument = new BsonDocument(),
                val commandArgs: BsonDocument = new BsonDocument(),
                val inputDocs: List[BsonDocument] = List[BsonDocument]())
  extends Request with WithReply {

  override def contentSerialize: Seq[Byte] = {
    database.toByteList ++
      commandName.toByteList ++
      metadata.toByteList ++
      commandArgs.toByteList ++
      inputDocs.toByteList
  }

  override def reply(doc: BsonDocument): OpCommandReply = {
    OpCommandReply(msgHeader.requestId, doc)
  }

  override def reply(docs: List[BsonDocument]): OpCommandReply = {
    OpCommandReply(msgHeader.requestId, docs.head)
  }

  override def reply(json: String): OpCommandReply = reply(BsonDocument.parse(json))

  override def reply(content: Seq[Byte]): OpCommandReply = {
    OpCommandReply(msgHeader.requestId, content)
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |database: $database
       |commandName: $commandName
       |metadata: $metadata
       |commandArgs: $commandArgs
       |inputDocs: ${inputDocs.mkString("\n")}
       """.stripMargin
  }


  def canEqual(other: Any): Boolean = other.isInstanceOf[OpCommand]

  override def equals(other: Any): Boolean = other match {
    case that: OpCommand =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        database == that.database &&
        commandName == that.commandName &&
        metadata == that.metadata &&
        commandArgs == that.commandArgs &&
        inputDocs.sameElements(that.inputDocs)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, database, commandName,
      metadata.toJson, commandArgs.toJson, inputDocs.map(_.toJson).mkString)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  def collection: Option[String] = {
    val commandValue = metadata.get(commandName)
    commandValue match {
      case s: BsonString => Some(s.asString().getValue)
      case _ => None
    }
  }

  override def realm: String = {
    collection.map(database + '.' + _).getOrElse(database)
  }

  override def command: String = commandName
}
