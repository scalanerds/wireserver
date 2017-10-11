package com.scalanerds.wireserver.wire.message

import akka.util.ByteString
import com.scalanerds.wireserver.wire.message.traits.Message


case class OpError(msgHeader: MsgHeader = MsgHeader(),
    content: Array[Byte] = Array[Byte](),
    error: String = "Error",
    raw: Option[ByteString] = None) extends Message {

  def realm: Option[String] = None

  override def contentSerialize: Array[Byte] = Array[Byte]()

  override def toString: String =
    s"""
       |Error
       |${msgHeader.serialize}
       |content : ${content.mkString(", ")}
       |error: $error
       |raw: ${raw.getOrElse(Nil).mkString(", ")}
    """.stripMargin
}


