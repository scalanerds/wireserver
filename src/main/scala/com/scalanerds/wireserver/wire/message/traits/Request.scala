package com.scalanerds.wireserver.wire.message.traits

import com.scalanerds.wireserver.wire.opcodes._
import org.bson.BsonDocument

/**
  * Mongo wire request message
  */
trait Request extends Message {
  def realm: String

  /** mongo client request command e.g: isMaster, buildinfo ... */
  def command: String

  /** Message to Json */
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

  def payload: Option[BsonDocument] = this match {
    case op: OpQuery => Some(op.query)
    case op: OpCommand => Some(op.metadata)
    case _ => None
  }
}
