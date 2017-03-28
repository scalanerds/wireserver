package com.scalanerds.wireserver

import akka.util.ByteString

package messageTypes {

  case object Ready

  case object DropConnection

  /**
    * TCP Server commands and entities
    */
  case object GetServerInfo

  case class ServerInfo(address: String, port: Int)

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