package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

/**
  * OpUpdate flags
  *
  * @param upsert      If set, the database will insert the supplied object into the
  *                    collection if no matching document is found.
  * @param multiUpdate corresponds to MultiUpdate.If set, the database will update all matching objects in
  *                    the collection. Otherwise only updates first matching document.
  */
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

  /**
    * Construct OpUpdateFlags from an int
    *
    * @param raw integer containing the flags
    * @return OpUpdateFlags
    */
  def apply(raw: Int): OpUpdateFlags = {
    val bytes = raw.toBooleanList
    new OpUpdateFlags(
      upsert = bytes.head,
      multiUpdate = bytes(1)
    )
  }
}
