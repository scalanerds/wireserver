package com.scalanerds.wireserver.tcpserver


import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.stream.TLSProtocol._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, TLS, Tcp}
import akka.stream.{OverflowStrategy, TLSProtocol, TLSRole}
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.scalanerds.wireserver.messages.GracefulKill

import scala.concurrent.Future

/***
  * TLS - enabled tcp server
  * @param props  the props of the actor that will process the ByteStreams
  * @param address the server binding address
  * @param port the server binding port
  */
class SSLTcpServer(props: (InetSocketAddress, InetSocketAddress) => Props, address: String, port: Int)
  extends TcpServer(address, port) with TcpSSL with TcpFraming {
  private val serverSSL = TLS(sslContext("/server.keystore", "/truststore"),
    TLSProtocol.negotiateNewSession, TLSRole.server)

  override def handler: Sink[Tcp.IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { conn =>
    logger.debug("Client connected from: " + conn.remoteAddress)

    val actor: ActorRef = context.actorOf(props(conn.remoteAddress, conn.localAddress))
    val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(actor, GracefulKill))

    val out: Source[ByteString, Unit] = Source.actorRef[ByteString](100, OverflowStrategy.fail)
      .mapMaterializedValue(actor ! _)
    val flow: Flow[ByteString, ByteString, NotUsed] = Flow.fromSinkAndSourceMat(framing.to(in), out)(Keep.none)

    val ssl = Flow[SslTlsInbound]
      .collect[ByteString] { case SessionBytes(_, bytes) => bytes }
      .via(flow)
      .map[SslTlsOutbound](SendBytes)

    conn handleWith serverSSL.reversed.join(ssl).alsoTo(Sink.onComplete(_ => logger.debug("Client disconnected")))
  }
}


object SSLTcpServer {
  /** SSL Tcp server props */
  def props(props: (InetSocketAddress, InetSocketAddress) => Props, address: String = "localhost", port: Int = 6600):
  Props =
    Props(classOf[SSLTcpServer], props, address, port)
}
