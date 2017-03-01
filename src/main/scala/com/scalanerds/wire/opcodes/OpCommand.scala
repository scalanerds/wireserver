package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

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

class OpCommand(val msgHeader: MsgHeader,
                val database: String,
                val commandName: String,
                val metadata: BSONObject,
                val commandArgs: BSONObject,
                val inputDocs: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
    database.toByteArray ++
    commandName.toByteArray ++
    metadata.toByteArray ++
    commandArgs.toByteArray ++
    inputDocs.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }
}
