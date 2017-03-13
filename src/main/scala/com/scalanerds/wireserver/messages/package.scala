package com.scalanerds.wireserver

package messages {

  import akka.util.ByteString

  case object GetPort

  case class Port(number: Int)

  case class Response(data: ByteString)

}