package com.scalanerds.wireserver.wire.message.traits

import com.scalanerds.wireserver.wire.opcodes._
import org.bson.BsonDocument

/**
  * Response from Mongo server to Mongo client
  */
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
