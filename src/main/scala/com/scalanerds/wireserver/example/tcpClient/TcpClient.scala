package com.scalanerds.wireserver.example.tcpClient

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.{FromServer, ToServer, WirePacket}
import com.scalanerds.wireserver.tcpserver.TcpBuffer

abstract class TcpClient(listener: ActorRef, address: String, port: Int)
  extends Actor with TcpBuffer {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def connection: ActorRef

  override def receive: Receive = {

    case ToServer(bytes) =>
      connection ! beforeWrite(bytes)

    case segment: ByteString => buffer(segment)

  }

  def onReceived(msg: WirePacket): Unit = {
    listener ! msg.asInstanceOf[FromServer]
  }

  def packetWrapper(packet: ByteString): WirePacket = {
    FromServer(packet)
  }

  /**
    * Override this method to intercept outcoming bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes
}
