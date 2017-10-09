package com.scalanerds.wireserver.wire.message.traits

import com.scalanerds.wireserver.wire.opcodes._
import org.bson.BsonDocument

trait Request extends Message {
  def realm: String

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
        new OpInsert(cmd.msgHeader, cmd.flags, fullCollectionName, cmd.documents)
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
