package com.scalanerds.wire.opcodes

import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.{Message, MsgHeader}


object OpKillCursor {
  def apply(msgHeader: MsgHeader, content: Array[Byte]): OpKillCursor = {
    val it = content.iterator
    val reserved = it.getInt
    val numberOfCursors = it.getInt
    val cursorIDs = it.getLongArray(numberOfCursors)
    new OpKillCursor(msgHeader, numberOfCursors, cursorIDs, reserved)
  }
}

class OpKillCursor(val msgHeader: MsgHeader,
                   val numberOfCursorIDs: Int,
                   val cursorIDs: Array[Long],
                   val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++
      reserved.toByteArray ++
      numberOfCursorIDs.toByteArray ++
      cursorIDs.map(_.toByteArray).reduce(_ ++ _)

    ByteString((content.length + 4).toByteArray ++ content)
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
        cursorIDs == that.cursorIDs
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(msgHeader.opCode, numberOfCursorIDs, cursorIDs)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}