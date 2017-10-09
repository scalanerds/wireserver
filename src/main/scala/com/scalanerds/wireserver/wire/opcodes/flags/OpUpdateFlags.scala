package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpUpdateFlags(val upsert: Boolean = false,
    val multiUpdate: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](upsert, multiUpdate).binaryToInt.toByteArray
  }

  override def toString: String = {
    s"""
       |upsert: $upsert
       |multiUpdate: $multiUpdate
     """.stripMargin
  }
}

object OpUpdateFlags {
  def apply(raw: Int): OpUpdateFlags = {
    val bytes = raw.toBooleanArray
    new OpUpdateFlags(
      upsert = bytes(0),
      multiUpdate = bytes(1)
    )
  }
}