package com.mongonerds.handlers

import akka.actor._
import akka.io.Tcp.Write
import akka.util.ByteString
import com.mongonerds.tcpserver.{Handler, HandlerProps}
import com.mongonerds.wire.Message

object WireHandlerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[WireHandler], connection)
}

class WireHandler(connection: ActorRef) extends Handler(connection) {

  def received(data: ByteString): Unit = {
    println("< " + data)

    this.parse(data)

    connection ! Write(ByteString(data + "\n"))
  }

  def parse(data: ByteString): Unit = {
//    println(Message.deserialize(data).opCode)
    println("parse logic")
  }
}
