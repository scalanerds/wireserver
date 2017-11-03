package com.scalanerds.wireserver.wire.message.traits

import org.bson.BsonDocument

/**
  * Used by OpRequest as an easy way to generate an OpReply
  */
trait WithReply {
  def reply(content: Seq[Byte]): Option[Response]

  def reply(docs: List[BsonDocument]): Option[Response]

  def reply(doc: BsonDocument): Option[Response]

  def reply(json: String): Option[Response]
}
