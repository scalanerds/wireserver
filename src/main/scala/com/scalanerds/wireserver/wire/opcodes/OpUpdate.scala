package com.scalanerds.wireserver.wire.opcodes

import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Request
import com.scalanerds.wireserver.wire.opcodes.flags.OpUpdateFlags
import org.bson.BsonDocument

object OpUpdate {
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpUpdate = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val flags = OpUpdateFlags(it.getInt)
    val bson = it.getBsonList
    val selector = bson(0)
    val update = bson(1)
    new OpUpdate(msgHeader, fullCollectionName, flags, selector, update, reserved)
  }
}

class OpUpdate(val msgHeader: MsgHeader,
    val fullCollectionName: String,
    val flags: OpUpdateFlags,
    val selector: BsonDocument,
    val update: BsonDocument,
    val reserved: Int = 0) extends Request {

  override def contentSerialize: Seq[Byte] = {
    reserved.toByteList ++
      fullCollectionName.toByteList ++
      flags.serialize ++
      selector.toByteList ++
      update.toByteList
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |flags: $flags
       |selector: $selector
       |update: $update
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpUpdate]

  override def equals(other: Any): Boolean = other match {
    case that: OpUpdate =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        flags == that.flags &&
        selector == that.selector &&
        update == that.update
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, fullCollectionName, flags, selector, update)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def realm: String = fullCollectionName

  override def command: String = "update"

}



