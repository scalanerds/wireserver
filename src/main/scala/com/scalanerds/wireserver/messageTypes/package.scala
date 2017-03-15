package com.scalanerds.wireserver

package messageTypes {

  import akka.util.ByteString

  // TCP Server
  case object GetPort
  case class Port(number: Int)

  // Serialized Wire packets
  sealed abstract class WirePacket(bytes: ByteString)
  case class Response(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class Request(bytes: ByteString) extends WirePacket(bytes: ByteString)

}