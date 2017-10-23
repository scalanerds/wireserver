package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Message


object OpKillCursor {
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpKillCursor = {
    val it = content.iterator
    val reserved = it.getInt
    val numberOfCursors = it.getInt
    val cursorIDs = it.getLongList(numberOfCursors)
    new OpKillCursor(msgHeader, numberOfCursors, cursorIDs, reserved)
  }
}

class OpKillCursor(val msgHeader: MsgHeader,
    val numberOfCursorIDs: Int,
    val cursorIDs: List[Long],
    val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  override def contentSerialize: Seq[Byte] = {
    reserved.toByteList ++
      numberOfCursorIDs.toByteList ++
      cursorIDs.map(_.toByteList).reduce(_ ++ _)
  }

  override def toString: String = {
    s"""
       |$msgHeader
       |numberOfCursorIDs: $numberOfCursorIDs
       |cursorIDs: ${cursorIDs.mkString(", ")}
     """.stripMargin
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpKillCursor]

  override def equals(other: Any): Boolean = other match {
    case that: OpKillCursor =>
      (that canEqual this) &&
        msgHeader.opCode == that.msgHeader.opCode &&
        numberOfCursorIDs == that.numberOfCursorIDs &&
        (cursorIDs sameElements that.cursorIDs)
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, numberOfCursorIDs, cursorIDs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}