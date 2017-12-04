package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

/**
  * OpInsertFlags
  *
  * @param continueOnError If set, the database will not stop processing a bulk insert
  *                        if one fails (eg due to duplicate IDs). This makes bulk insert behave similarly
  *                        to a series of single inserts, except lastError will be set if any insert fails,
  *                        not just the last one. If multiple errors occur, only the most recent will be
  *                        reported by getLastError.
  */
class OpInsertFlags(val continueOnError: Boolean = false) {
  def serialize: Seq[Byte] = {
    Seq[Byte](continueOnError).binaryToInt.toByteList
  }

  override def toString: String = {
    s"""
       |continueOnError: $continueOnError
     """.stripMargin
  }
}

object OpInsertFlags {

  /**
    * Construct OpInsertFlags from an int
    *
    * @param raw integer containing the flags
    * @return OpInsertFlags
    */
  def apply(raw: Int): OpInsertFlags = {
    val bytes = raw.toBooleanList
    new OpInsertFlags(
      bytes.head
    )
  }
}
