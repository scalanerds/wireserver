package com.mongonerds.wire.opcodes


import akka.util.ByteString
import com.mongonerds.utils.Utils._
import com.mongonerds.wire.{Message, MsgHeader}
import org.bson.{BSON, BSONObject}

object OpReply {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpReply = {
    val it = content.iterator
    val responseFlags = it.cTake(4).toArray.toInt
    val cursorId = it.cTake(8).toArray.toLong
    val startingFrom = it.cTake(4).toArray.toInt
    val numberReturned = it.cTake(4).toArray.toInt
    val documents = it.toArray.toBSONArray
    new OpReply(msgHeader,
      responseFlags,
      cursorId,
      startingFrom,
      numberReturned,
      documents
    )
  }
}

class OpReply(val msgHeader: MsgHeader,
              val responseFlags: Int,
              val cursorId: Long,
              val startingFrom: Int,
              val numberReturned: Int,
              val documents: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
    responseFlags.toByteArray()  ++
    cursorId.toByteArray() ++
    startingFrom.toByteArray() ++
    numberReturned.toByteArray() ++
    documents.toByteArray

    ByteString((content.length + 4).toByteArray() ++ content)
  }
}



