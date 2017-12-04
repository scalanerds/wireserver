package com.scalanerds.wireserver.messages.response

import akka.util.ByteString
import com.scalanerds.wireserver.messages.WirePacket

/** bytes from wireserver to mongo client */
case class BytesToClient(bytes: ByteString) extends WirePacket
