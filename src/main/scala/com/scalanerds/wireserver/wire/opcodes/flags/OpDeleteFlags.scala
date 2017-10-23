package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpDeleteFlags(val singleRemove: Boolean = false) {
  def serialize: Seq[Byte] = {
     Seq[Byte](singleRemove).binaryToInt.toByteList
  }

  override def toString: String = {
    s"""
       |singleRemove: $singleRemove
     """.stripMargin
  }
}

object OpDeleteFlags {
  def apply(raw: Int): OpDeleteFlags = {
    val bytes = raw.toBooleanList
    new OpDeleteFlags(
      bytes(0)
    )
  }
}