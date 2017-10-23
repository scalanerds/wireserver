package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpQueryFlags(val tailableCursor: Boolean = false,
    val slaveOk: Boolean = false,
    val opLogReply: Boolean = false,
    val noCursorTimeOut: Boolean = false,
    val awaitData: Boolean = false,
    val exhaust: Boolean = false,
    val partial: Boolean = false) {
  def serialize: Seq[Byte] = {
     Seq[Byte](0, tailableCursor, slaveOk, opLogReply, noCursorTimeOut, awaitData, exhaust, partial)
      .binaryToInt.toByteList
  }

  override def hashCode(): Int = {
    val state = Seq(0, tailableCursor, slaveOk, opLogReply, noCursorTimeOut, awaitData, exhaust, partial)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = {
    s"""
       |tailableCursor: $tailableCursor
       |slaveOk: $slaveOk
       |opLogReply: $opLogReply
       |noCursorTimeOut: $noCursorTimeOut
       |awaitData: $awaitData
       |exhaust: $exhaust
       |partial: $partial
     """.stripMargin
  }
}

object OpQueryFlags {
  def apply(raw: Int): OpQueryFlags = {
    val bytes = raw.toBooleanList
    new OpQueryFlags(bytes(1), bytes(2), bytes(3), bytes(4), bytes(5), bytes(6), bytes(7))
  }
}