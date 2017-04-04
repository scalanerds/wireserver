package com.scalanerds.wireserver.wire.opcodes


import akka.util.ByteString
import com.scalanerds.wireserver.utils.Utils._
import com.scalanerds.wireserver.wire.{Message, MsgHeader, Request}

object OpGetMore {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpGetMore = {
    val it = content.iterator
    val reserved = it.getInt
    val fullCollectionName = it.getString
    val numberToReturn = it.getInt
    val cursorID = it.getLong
    new OpGetMore(msgHeader, fullCollectionName, numberToReturn, cursorID, reserved)
  }
}

class OpGetMore(val msgHeader: MsgHeader,
                val fullCollectionName: String,
                val numberToReturn: Int,
                val cursorID: Long,
                val reserved: Int = 0) extends Message with Request {

  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Array[Byte] = {
    reserved.toByteArray ++
    fullCollectionName.toByteArray ++
    numberToReturn.toByteArray ++
    cursorID.toByteArray
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |fullCollectionName: $fullCollectionName
       |numberToReturn: $numberToReturn
       |cursorID: $cursorID
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpGetMore]

  override def equals(other: Any): Boolean = other match {
    case that: OpGetMore =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        fullCollectionName == that.fullCollectionName &&
        numberToReturn == that.numberToReturn &&
        cursorID == that.cursorID
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, fullCollectionName, numberToReturn, cursorID)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def realm: String = fullCollectionName

  override def command: String = "getMore"
}