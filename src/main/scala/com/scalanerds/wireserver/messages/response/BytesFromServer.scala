package com.scalanerds.wireserver.messages.response

import akka.util.ByteString
import com.scalanerds.wireserver.messages.WirePacket

/** bytes from mongo server to wireserver */
case class BytesFromServer(bytes: ByteString) extends WirePacket
