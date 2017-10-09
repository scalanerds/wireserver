package com.scalanerds.wireserver.messages.request

import akka.util.ByteString
import com.scalanerds.wireserver.messages.WirePacket

/** bytes from wireserver to mongo server */
case class BytesToServer(bytes: ByteString) extends WirePacket
