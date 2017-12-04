package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

/**
  * OpDeleteFlags
  *
  * @param singleRemove If set, the database will remove only the first matching document
  *                     in the collection. Otherwise all matching documents will be removed.
  */
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

  /**
    * Construct OpDeleteFlags from an int
    *
    * @param raw integer containing the flags
    * @return OpDeleteFlags
    */
  def apply(raw: Int): OpDeleteFlags = {
    val bytes = raw.toBooleanList
    new OpDeleteFlags(
      bytes.head
    )
  }
}
