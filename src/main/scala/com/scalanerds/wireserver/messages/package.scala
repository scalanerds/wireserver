package com.scalanerds.wireserver

package messages {


  case object GetPort

  case class Port(number: Int)

  case object Ready

  case object  DropConnection

}