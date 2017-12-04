package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

/**
  * OpReplyFlags
  *
  * @param cursorNotFound   Is set when getMore is called but the cursor id is not
  *                         valid at the server. Returned with zero results.
  * @param queryFailure     Is set when query failed. Results consist of one document
  *                         containing an “$err” field describing the failure.
  * @param shardConfigStale Drivers should ignore this. Only mongos will ever
  *                         see this set, in which case, it needs to update config from the server.
  * @param awaitCapable     Is set when the server supports the AwaitData Query option. If it doesn’t,
  *                         a client should sleep a little between getMore’s of a Tailable cursor.
  *                         Mongod version 1.6 supports AwaitData and thus always sets AwaitCapable.
  */
class OpReplyFlags(val cursorNotFound: Boolean = false,
                   val queryFailure: Boolean = false,
                   val shardConfigStale: Boolean = false,
                   val awaitCapable: Boolean = false) {
  def serialize: Seq[Byte] = {
    Seq[Byte](cursorNotFound, queryFailure, shardConfigStale, awaitCapable).binaryToInt.toByteList
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

  /**
    * Construct OpReplyFlags from an int
    *
    * @param raw integer containing the flags
    * @return OpReplyFlags
    */
  def apply(raw: Int): OpReplyFlags = {
    val bytes = raw.toBooleanList
    new OpReplyFlags(bytes.head, bytes(1), bytes(2), bytes(3))
  }
}
