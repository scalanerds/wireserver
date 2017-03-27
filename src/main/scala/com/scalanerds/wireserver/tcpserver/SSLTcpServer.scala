package com.scalanerds.wireserver.tcpserver



import java.net.InetSocketAddress

import akka.actor.{ActorRef, PoisonPill, Props}
import akka.stream.TLSProtocol._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, TLS, Tcp}
import akka.stream.{OverflowStrategy, TLSProtocol, TLSRole}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future

object SSLTcpServer {
  def props(props: (InetSocketAddress, InetSocketAddress) => Props, address: String = "localhost", port: Int = 6000): Props =
    Props(classOf[SSLTcpServer], props, address, port)

}

class SSLTcpServer(props: (InetSocketAddress, InetSocketAddress) => Props, address: String, port: Int) extends TcpServer(address, port) with TcpSSL {
  private val serverSSL = TLS(sslContext(), TLSProtocol.negotiateNewSession, TLSRole.server)

  override def handler: Sink[Tcp.IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { conn =>
    println("Client connected from: " + conn.remoteAddress)

    val actor: ActorRef = context.actorOf(props(conn.remoteAddress, conn.localAddress))
    val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(actor, PoisonPill))

    val out: Source[ByteString, Unit] = Source.actorRef[ByteString](1, OverflowStrategy.fail)
      .mapMaterializedValue(actor ! _)
    val flow: Flow[ByteString, ByteString, NotUsed] = Flow.fromSinkAndSourceMat(in, out)(Keep.none)

    val ssl = Flow[SslTlsInbound]
      .collect[ByteString] { case SessionBytes(_, bytes) => bytes }
      .via(flow)
      .map[SslTlsOutbound](SendBytes)

    conn handleWith serverSSL.reversed.join(ssl).alsoTo(Sink.onComplete(_ => println("Client disconnected")))
  }
}

