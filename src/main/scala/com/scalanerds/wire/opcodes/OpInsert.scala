package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BSONObject

object OpInsert {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpInsert = {
    val it = content.iterator
    val flags = it.getInt
    val fullCollectionName = it.getString
    val documents = it.getBsonArray
    new OpInsert(msgHeader, flags, fullCollectionName, documents)
  }
}

class OpInsert(val msgHeader: MsgHeader,
               val flags: Int,
               val fullCollectionName: String,
               val documents: Array[BSONObject]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      flags.toByteArray ++
      fullCollectionName.toByteArray ++
      documents.toByteArray

    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |flags: $flags
       |fullCollectionName : $fullCollectionName
       |documents: ${documents.mkString("\n")}
     """.stripMargin
  }
}