package com.scalanerds.wireserver.example.tcpClient


import akka.actor.{ActorRef, PoisonPill}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.tcpserver.TcpFraming

class PlainTcpClient(listener: ActorRef, address: String, port: Int)
  extends TcpClient(listener, address, port) with TcpFraming {

  private val sink = Flow[ByteString].to(Sink.actorRef(self, PoisonPill))

  private val tcpFlow = Flow[ByteString].via(Tcp().outgoingConnection(address, port))
  val connection: ActorRef = Source.actorRef(1, OverflowStrategy.fail).via(tcpFlow).via(framing).to(sink).run()

}