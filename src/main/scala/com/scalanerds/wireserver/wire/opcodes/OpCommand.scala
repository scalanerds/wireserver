package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.{Request, WithReply}
import org.bson._

/**
  * Mongo intra-cluster message
  *
  * Code 2010
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * OpCommand is a wire protocol message used internally for intra-cluster database command requests
  * issued by one MongoDB server to another. The receiving database sends back an OpCommandReply as a
  * response to a OpCommand.
  *
  * @param msgHeader   Message header.
  * @param database    The name of the database to run the command on.
  * @param commandName The name of the command.
  * @param metadata    Available for the system to attach any metadata to internal commands that is not
  *                    part of the command parameters proper, as supplied by the client driver
  * @param commandArgs A BSON document containing the command arguments.
  * @param inputDocs   Zero or more documents acting as input to the command. Useful for commands that
  *                    can require a large amount of data sent from the client, such as a batch insert.
  */
class OpCommand(
    val msgHeader: MsgHeader = new MsgHeader(opCode = OpCommandCode),
    val database: String,
    val commandName: String,
    val metadata: BsonDocument = new BsonDocument(),
    val commandArgs: BsonDocument = new BsonDocument(),
    val inputDocs: List[BsonDocument] = List[BsonDocument]())
    extends Request
    with WithReply {

  override def contentSerialize: Seq[Byte] = {
    database.toByteList ++
      commandName.toByteList ++
      metadata.toByteList ++
      commandArgs.toByteList ++
      inputDocs.toByteList
  }

  override def reply(doc: BsonDocument): Option[OpCommandReply] = {
    OpCommandReply(msgHeader.requestId, doc)
  }

  override def reply(docs: List[BsonDocument]): Option[OpCommandReply] = {
    OpCommandReply(msgHeader.requestId, docs.head)
  }

  override def reply(json: String): Option[OpCommandReply] =
    reply(BsonDocument.parse(json))

  override def reply(content: Seq[Byte]): Option[OpCommandReply] = {
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
    val state = Seq(msgHeader.opCode,
                    database,
                    commandName,
                    metadata.toJson,
                    commandArgs.toJson,
                    inputDocs.map(_.toJson).mkString)
    state
      .map(_.hashCode())
      .foldLeft(0)((a, b) => 31 * a + b)
  }

  def collection: Option[String] = {
    Option(metadata.get(commandName)) collect {
      case s: BsonString => s.asString().getValue
    }
  }

  override def realm: String = {
    collection.fold(database)(database + '.' + _)
  }

  override def command: String = commandName
}

object OpCommand {

  /**
    * Construct OpCommand
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpCommand
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): Option[OpCommand] = {
    val it = content.iterator
    for {
      database <- it.getStringOption
      commandName <- it.getStringOption
      metadata <- it.getBsonOption
      commandArgs <- it.getBsonOption
      inputDocs <- it.getBsonListOption
    } yield
      new OpCommand(msgHeader,
                    database,
                    commandName,
                    metadata,
                    commandArgs,
                    inputDocs)
  }
}
