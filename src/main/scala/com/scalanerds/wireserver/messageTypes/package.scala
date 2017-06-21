package com.scalanerds.wireserver

import akka.util.ByteString
import com.scalanerds.wireserver.wire.{Request, Response}

package messageTypes {

  import java.net.InetSocketAddress

  import scala.concurrent.Promise

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

  // Request handling flow stages
  case class BytesFromClient(bytes: ByteString) extends WirePacket
  case class RequestFromClient(request: Request)
  case class RequestToEnforcer(request: Request)
  case class RequestAwaitForResponse(request: Request, promise: Promise[Response] = Promise[Response]())
  case class RequestToServer(request: Request)
  case class BytesToServer(bytes: ByteString) extends WirePacket

  // Response handling flow stages
  case class BytesFromServer(bytes: ByteString) extends WirePacket
  case class ResponseFromServer(response: Response)
  case class ResponseFromEnforcer(response: Response)
  case class ResponseFromMasker(response: Response, payload: Option[(Request, String)])
  case class ResponseToClient(response: Response)
  case class BytesToClient(bytes: ByteString) extends WirePacket

}