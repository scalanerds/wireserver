package com.scalanerds.wireserver.wire.message

import akka.util.ByteString
import com.scalanerds.wireserver.wire.message.traits.Message


case class OpError(msgHeader: MsgHeader = MsgHeader(),
    content: Seq[Byte] = Seq[Byte](),
    error: String = "Error",
    raw: Option[ByteString] = None) extends Message {

  def realm: Option[String] = None

  override def contentSerialize: Seq[Byte] = Seq[Byte]()

  override def toString: String =
    s"""
       |Error
       |${msgHeader.serialize}
       |content : ${content.mkString(", ")}
       |error: $error
       |raw: ${raw.getOrElse(Nil).mkString(", ")}
    """.stripMargin
}


