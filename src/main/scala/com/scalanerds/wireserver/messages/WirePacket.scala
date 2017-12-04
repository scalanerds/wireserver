package com.scalanerds.wireserver.messages

import akka.util.ByteString

/** Serialized Wire packets */
trait WirePacket {
  def bytes: ByteString
}
