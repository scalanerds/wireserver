package com.scalanerds.wireserver.wire.message.traits

import org.bson.BsonDocument

trait WithReply {
  def reply(content: Seq[Byte]): Response

  def reply(docs: List[BsonDocument]): Response

  def reply(doc: BsonDocument): Response

  def reply(json: String): Response
}
