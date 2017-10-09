package com.scalanerds.wireserver.messages.request

import akka.util.ByteString
import com.scalanerds.wireserver.messages.WirePacket

/** bytes from mongo client to wireserver */
case class BytesFromClient(bytes: ByteString) extends WirePacket
