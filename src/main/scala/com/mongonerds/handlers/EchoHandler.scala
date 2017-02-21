package com.mongonerds.handlers

import akka.actor._
import akka.io.Tcp.Write
import akka.util.ByteString
import com.mongonerds.tcpserver.{Handler, HandlerProps}

object EchoHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[EchoHandler], connection)
}

class EchoHandler(connection: ActorRef) extends Handler(connection) {

  /**
    * Echoes incoming message.
    */
  def received(data: String) = connection ! Write(ByteString(data + "\n"))
}
