package com.scalanerds.wireserver.example.tcpClient

import akka.NotUsed
import akka.actor.{ActorRef, PoisonPill}
import akka.stream.TLSProtocol.{SessionBytes, SslTlsInbound, SslTlsOutbound}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, TLS, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.tcpserver.TcpSSL

class SSLTcpClient(listener: ActorRef, address: String, port: Int)
  extends TcpClient(listener, address, port) with TcpSSL {

  private val clientSSL = TLS(sslContext("/client.keystore", "/truststore"), TLSProtocol.negotiateNewSession, TLSRole
    .client)

  private val sink = Flow[SslTlsInbound].collect[ByteString] { case SessionBytes(_, bytes) =>
    bytes
  }.to(Sink.actorRef(self, PoisonPill))

  private val sslFlow: Flow[SslTlsOutbound, SslTlsInbound, NotUsed] = clientSSL.join(Tcp().outgoingConnection
  (address, port))

  val connection: ActorRef = Source.actorRef(1, OverflowStrategy.fail).via(sslFlow).to(sink).run()

}
