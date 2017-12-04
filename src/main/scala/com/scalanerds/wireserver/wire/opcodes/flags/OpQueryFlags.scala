package com.scalanerds.wireserver.wire.opcodes.flags

import com.scalanerds.wireserver.utils.Conversions._

/**
  * OpQueryFlags
  *
  * @param tailableCursor  Tailable means cursor is not closed when the last data
  *                        is retrieved. Rather, the cursor marks the final object’s position. You can resume using
  *                        the cursor later, from where it was located, if more data were received. Like any “latent
  *                        cursor”, the cursor may become invalid at some point (CursorNotFound) – for example
  *                        if the final object it references were deleted.
  * @param slaveOk         Allow query of replica slave. Normally these return an error
  *                        except for namespace “local”.
  * @param opLogReply      Internal replication use only - driver should not set.
  * @param noCursorTimeOut The server normally times out idle cursors after an inactivity period
  *                        (10 minutes) to prevent excess memory use. Set this option to prevent that.
  * @param awaitData       Use with TailableCursor. If we are at the end of the data, block for a while
  *                        rather than returning no data. After a timeout period, we do return as normal.
  * @param exhaust         Stream the data down full blast in multiple “more” packages,
  *                        on the assumption that the client will fully read all data queried. Faster when you are
  *                        pulling a lot of data and know you want to pull it all down. Note: the client is not allowed
  *                        to not read all the data unless it closes the connection.
  * @param partial         Get partial results from a mongos if some shards are down
  *                        (instead of throwing an error)
  */
class OpQueryFlags(val tailableCursor: Boolean = false,
                   val slaveOk: Boolean = false,
                   val opLogReply: Boolean = false,
                   val noCursorTimeOut: Boolean = false,
                   val awaitData: Boolean = false,
                   val exhaust: Boolean = false,
                   val partial: Boolean = false) {
  def serialize: Seq[Byte] = {
    Seq[Byte](0,
              tailableCursor,
              slaveOk,
              opLogReply,
              noCursorTimeOut,
              awaitData,
              exhaust,
              partial).binaryToInt.toByteList
  }

  override def hashCode(): Int = {
    val state = Seq(0,
                    tailableCursor,
                    slaveOk,
                    opLogReply,
                    noCursorTimeOut,
                    awaitData,
                    exhaust,
                    partial)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString: String = {
    s"""
       |tailableCursor: $tailableCursor
       |slaveOk: $slaveOk
       |opLogReply: $opLogReply
       |noCursorTimeOut: $noCursorTimeOut
       |awaitData: $awaitData
       |exhaust: $exhaust
       |partial: $partial
     """.stripMargin
  }
}

object OpQueryFlags {

  /**
    * Construct OpQueryFlags from an int
    *
    * @param raw integer containing the flags
    * @return OpQueryFlags
    */
  def apply(raw: Int): OpQueryFlags = {
    val bytes = raw.toBooleanList
    new OpQueryFlags(bytes(1),
                     bytes(2),
                     bytes(3),
                     bytes(4),
                     bytes(5),
                     bytes(6),
                     bytes(7))
  }
}
