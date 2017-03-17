package com.scalanerds.wireserver

package messageTypes {

  import akka.util.ByteString

  /**
    * TCP Server commands and entities
    */
  case object GetPort
  case class Port(number: Int)

  /**
    * Serialized Wire packets
    * @param bytes
    */
  sealed abstract class WirePacket(bytes: ByteString)
  case class Response(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class Request(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class FromClient(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class FromServer(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class ToClient(bytes: ByteString) extends WirePacket(bytes: ByteString)
  case class ToServer(bytes: ByteString) extends WirePacket(bytes: ByteString)

}