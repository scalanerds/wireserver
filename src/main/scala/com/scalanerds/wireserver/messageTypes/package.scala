package com.scalanerds.wireserver

import akka.util.ByteString

package messageTypes {
  /**
    * Serialized Wire packets
    */
  trait WirePacket {
    def bytes: ByteString
  }

  // Request handling flow stages
  case class BytesFromClient(bytes: ByteString) extends WirePacket
  case class BytesToServer(bytes: ByteString) extends WirePacket

  // Response handling flow stages
  case class BytesFromServer(bytes: ByteString) extends WirePacket
  case class BytesToClient(bytes: ByteString) extends WirePacket

}