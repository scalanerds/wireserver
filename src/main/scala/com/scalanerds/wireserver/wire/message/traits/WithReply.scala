package com.scalanerds.wireserver.wire.message.traits

import org.bson.BsonDocument

trait WithReply {
  def reply(content: Array[Byte]): Response

  def reply(docs: Array[BsonDocument]): Response

  def reply(doc: BsonDocument): Response

  def reply(json: String): Response
}
