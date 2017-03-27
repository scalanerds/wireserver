package com.scalanerds.wireserver.tcpserver


import java.net.InetSocketAddress

import akka.actor.{ActorRef, PoisonPill, Props}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, Tcp}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future

object PlainTcpServer{
  def props(props: (InetSocketAddress, InetSocketAddress) => Props, address: String = "localhost", port: Int = 3000): Props =
    Props(classOf[PlainTcpServer], props, address, port)
}

class PlainTcpServer(props: (InetSocketAddress, InetSocketAddress) => Props, address: String, port: Int) extends TcpServer(address, port) {


  def handler: Sink[Tcp.IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { conn =>
    val actor: ActorRef = context.actorOf(props(conn.remoteAddress, conn.localAddress))

    val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(actor, PoisonPill))

    val out: Source[ByteString, Unit] = Source.actorRef[ByteString](1, OverflowStrategy.fail)
      .mapMaterializedValue(actor ! _)

    val flow: Flow[ByteString, ByteString, NotUsed] = Flow.fromSinkAndSourceMat(in, out)(Keep.none)
    println("Client connected from: " + conn.remoteAddress)

    conn handleWith flow
  }
}
