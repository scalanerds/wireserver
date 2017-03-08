package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.conversions._
import com.scalanerds.wire.{Message, MsgHeader}
import org.bson.BsonDocument


object OpInsert {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpInsert = {
    val it = content.iterator
    val flags = OpInsertFlags(it.getInt)
    val fullCollectionName = it.getString
    val documents = it.getBsonArray
    new OpInsert(msgHeader, flags, fullCollectionName, documents)
  }
}

class OpInsert(val msgHeader: MsgHeader,
               val flags: OpInsertFlags,
               val fullCollectionName: String,
               val documents: Array[BsonDocument]) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      flags.serialize ++
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

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpInsert]

  override def equals(other: Any): Boolean = other match {
    case that: OpInsert =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        flags == that.flags &&
        fullCollectionName == that.fullCollectionName &&
        documents == that.documents
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, flags, fullCollectionName, documents)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object OpInsertFlags {
  def apply(raw: Int): OpInsertFlags = {
    val bytes = raw.toBooleanArray
    new OpInsertFlags(
      bytes(0)
    )
  }
}

class OpInsertFlags(val continueOnError: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](continueOnError).binaryToInt.toByteArray
  }

  override def toString: String = {
    s"""
       |continueOnError: $continueOnError
     """.stripMargin
  }
}