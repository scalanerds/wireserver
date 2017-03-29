package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.{Message, MsgHeader, OPCODES}
import org.bson.BsonDocument

object OpCommand {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpCommand = {
    val it = content.iterator
    val database = it.getString
    val commandName = it.getString
    val metadata = it.getBson
    val commandArgs = it.getBson
    val inputDocs = it.getBsonArray
    new OpCommand(msgHeader, database, commandName, metadata, commandArgs, inputDocs)
  }
}

class OpCommand(val msgHeader: MsgHeader = new MsgHeader(opCode = OPCODES.opCommand),
                val database: String,
                val commandName: String,
                val metadata: BsonDocument = new BsonDocument(),
                val commandArgs: BsonDocument = new BsonDocument(),
                val inputDocs: Array[BsonDocument] = Array[BsonDocument]()) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      database.toByteArray ++
      commandName.toByteArray ++
      metadata.toByteArray ++
      commandArgs.toByteArray ++
      inputDocs.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)

  }

  def reply(doc: BsonDocument): OpCommandReply = {
    OpCommandReply(msgHeader.requestId, doc)
  }

  def reply(json: String) : OpCommandReply = reply(BsonDocument.parse(json))

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
    val state = Seq(msgHeader.opCode, database, commandName, metadata, commandArgs, inputDocs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
