package com.scalanerds.wireserver

package messageTypes {

  import akka.util.ByteString

  case object Ready
  case object DropConnection

  /**
    * TCP Server commands and entities
    */
  case object GetPort
  case class Port(number: Int)

  /**
    * Serialized Wire packets
    */
  trait WirePacket {
    def bytes: ByteString
  }
  case class Response(bytes: ByteString) extends WirePacket
  case class Request(bytes: ByteString) extends WirePacket
  case class FromClient(bytes: ByteString) extends WirePacket
  case class FromServer(bytes: ByteString) extends WirePacket
  case class ToClient(bytes: ByteString) extends WirePacket
  case class ToServer(bytes: ByteString) extends WirePacket

}