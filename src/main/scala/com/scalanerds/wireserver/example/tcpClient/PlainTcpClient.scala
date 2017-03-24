package com.scalanerds.wireserver.example.tcpClient


import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill}
import akka.event.Logging
import akka.io.Tcp._
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.tcpserver.TcpBuffer

class PlainTcpClient(listener: ActorRef, address: String, port: Int)
  extends Actor with TcpBuffer {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val sink: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(self, PoisonPill))

  val tcpFlow = Flow[ByteString].via(Tcp().outgoingConnection(address, port))
  var connection = Source.actorRef(1, OverflowStrategy.fail).via(tcpFlow).to(sink).run()

  val log = Logging(context.system, this)

  override def receive: Receive = {
    /**
      * WirePacket receivers
      */
    case Received(segment: ByteString) =>
      buffer(segment)

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