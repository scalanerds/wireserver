package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpUpdateFlags(val upsert: Boolean = false,
    val multiUpdate: Boolean = false) {
  def serialize: Seq[Byte] = {
     Seq[Byte](upsert, multiUpdate).binaryToInt.toByteList
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
    val bytes = raw.toBooleanList
    new OpUpdateFlags(
      upsert = bytes(0),
      multiUpdate = bytes(1)
    )
  }
}