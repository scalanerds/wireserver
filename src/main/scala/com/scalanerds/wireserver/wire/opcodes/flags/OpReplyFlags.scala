package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpReplyFlags(val cursorNotFound: Boolean = false,
                   val queryFailure: Boolean = false,
                   val shardConfigStale: Boolean = false,
                   val awaitCapable: Boolean = false) {
  def serialize: Seq[Byte] = {
     Seq[Byte](cursorNotFound, queryFailure, shardConfigStale, awaitCapable)
      .binaryToInt.toByteList
  }

  override def toString: String = {
    s"""
       |cursorNotFound: $cursorNotFound
       |queryFailure: $queryFailure
       |shardConfigStale: $shardConfigStale
       |awaitCapable: $awaitCapable
     """.stripMargin
  }
}

object OpReplyFlags {
  def apply(raw: Int): OpReplyFlags = {
    val bytes = raw.toBooleanList
    new OpReplyFlags(bytes(0), bytes(1), bytes(2), bytes(3))
  }
}