package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.conversions._
import com.scalanerds.wireserver.wire.{Message, MsgHeader, Request, Response}
import org.bson.BsonDocument

object OpDelete {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpDelete = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpDeleteFlags(it.getInt)
    val selector = it.getBson
    new OpDelete(msgHeader, fullCollectionName, flags, selector, reserved)
  }
}

class OpDelete(val msgHeader: MsgHeader,
               val fullCollectionName: String,
               val flags: OpDeleteFlags,
               val selector: BsonDocument,
               val reserved: Int = 0) extends Request {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Array[Byte] = {
    reserved.toByteArray ++
    fullCollectionName.toByteArray ++
    flags.serialize ++
    selector.toByteArray
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |flags: $flags
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpDelete]

  override def equals(other: Any): Boolean = other match {
    case that: OpDelete =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        flags == that.flags &&
        selector == that.selector
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, fullCollectionName, flags, selector)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def realm: String = fullCollectionName

  override def command: String = "delete"

}


object OpDeleteFlags {
  def apply(raw: Int): OpDeleteFlags = {
    val bytes = raw.toBooleanArray
    new OpDeleteFlags(
      bytes(0)
    )
  }
}

class OpDeleteFlags(val singleRemove: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](singleRemove).binaryToInt.toByteArray
  }

  override def toString: String = {
    s"""
       |singleRemove: $singleRemove
     """.stripMargin
  }
}