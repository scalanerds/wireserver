package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

class OpDeleteFlags(val singleRemove: Boolean = false) {
  def serialize: Array[Byte] = {
    Array[Byte](singleRemove).binaryToInt.toByteArray
  }

  override def toString: String = {
    s"""
       |singleRemove: $singleRemove
     """.stripMargin
  }
}

object OpDeleteFlags {
  def apply(raw: Int): OpDeleteFlags = {
    val bytes = raw.toBooleanArray
    new OpDeleteFlags(
      bytes(0)
    )
  }
}