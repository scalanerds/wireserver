package com.scalanerds.wireserver.wire.opcodes

import akka.util.ByteString
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.message.MsgHeader
import com.scalanerds.wireserver.wire.message.traits.Message


/**
  * Mongo client request
  *
  * Code 2007
  *
  * [[https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/ wire-protocol]]
  *
  * The OpKillCursors message is used to close an active cursor in the database.
  * This is necessary to ensure that database resources are reclaimed at the end of the query.
  *
  * If a cursor is read until exhausted (read until OpQuery or OpGetMore returns zero for the cursor id),
  * there is no need to kill the cursor.
  *
  * @param msgHeader         Message header.
  * @param numberOfCursorIDs The number of cursor IDs that are in the message.
  * @param cursorIDs         “List” of cursor IDs to be closed. If there are more than one,
  *                          they are written to the socket in sequence, one after another.
  * @param reserved          Integer value of 0. Reserved for future use.
  */
class OpKillCursors(val msgHeader: MsgHeader,
    val numberOfCursorIDs: Int,
    val cursorIDs: List[Long],
    val reserved: Int = 0) extends Message {
  override def serialize: ByteString = {
    val content = msgHeader.serialize ++ contentSerialize
    ByteString((content.length + 4).toByteArray ++ content)
  }

  /** serialize message into bytes */
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

  def canEqual(other: Any): Boolean = other.isInstanceOf[OpKillCursors]

  override def equals(other: Any): Boolean = other match {
    case that: OpKillCursors =>
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


object OpKillCursors {
  /**
    * Construct OpKillCursors
    *
    * @param msgHeader Message header.
    * @param content   Message bytes.
    * @return OpKillCursors
    */
  def apply(msgHeader: MsgHeader, content: Seq[Byte]): OpKillCursors = {
    val it = content.iterator
    val reserved = it.getInt
    val numberOfCursors = it.getInt
    val cursorIDs = it.getLongList(numberOfCursors)
    new OpKillCursors(msgHeader, numberOfCursors, cursorIDs, reserved)
  }
}
