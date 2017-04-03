package com.scalanerds.wireserver.example.tcpClient

import akka.stream.OverflowStrategy
import akka.actor.{ActorRef, Kill, PoisonPill}
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.tcpserver.TcpFraming

class PlainTcpClient(listener: ActorRef, address: String, port: Int)
  extends TcpClient(listener, address, port) with TcpFraming {

  private val sink = Flow[ByteString].to(Sink.actorRef(self, PoisonPill))

  private val tcpFlow = Flow[ByteString].via(Tcp().outgoingConnection(address, port)).alsoTo(Sink.onComplete(_ => {
    println("Bob died")
    //TODO update parent supervisor strategy to restart the actor
    self ! Kill
  }))
  val connection: ActorRef = Source.actorRef(1, OverflowStrategy.fail).via(tcpFlow).via(framing).to(sink).run()
}