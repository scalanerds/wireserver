package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

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

object OpInsertFlags {
  def apply(raw: Int): OpInsertFlags = {
    val bytes = raw.toBooleanArray
    new OpInsertFlags(
      bytes(0)
    )
  }
}