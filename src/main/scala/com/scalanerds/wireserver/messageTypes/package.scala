package com.scalanerds.wireserver

import akka.util.ByteString
import com.scalanerds.wireserver.wire.{Request, Response}

package messageTypes {

  import java.net.InetSocketAddress

  case object Ready

  case object DropConnection

  /**
    * TCP Server commands and entities
    */
  case object GetInfo

  case class Info(local: InetSocketAddress, remote: InetSocketAddress)

  /**
    * Serialized Wire packets
    */
  trait WirePacket {
    def bytes: ByteString
  }

  case class ResponseBytes(bytes: ByteString) extends WirePacket
  case class RequestBytes(bytes: ByteString) extends WirePacket
  case class BytesFromClient(bytes: ByteString) extends WirePacket
  case class BytesFromServer(bytes: ByteString) extends WirePacket
  case class BytesToClient(bytes: ByteString) extends WirePacket
  case class BytesToServer(bytes: ByteString) extends WirePacket

  case class MessageFromClient(message: Request)
  case class MessageFromServer(message: Response)
  case class MessageToClient(message: Response)
  case class MessageToServer(message: Request)

}