package com.scalanerds.wireserver.tcpserver

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.{ActorRef, PoisonPill, Props}
import akka.stream.TLSProtocol._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, TLS, Tcp}
import akka.stream.{OverflowStrategy, TLSProtocol, TLSRole}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future

object SSLTcpServer {
  def props(props: Props, address: String = "localhost", port: Int = 6000): Props =
    Props(classOf[SSLTcpServer], props, address, port)

  private def sslContext: SSLContext = {

    val password = "123456".toCharArray

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
    keyStore.load(getClass.getResourceAsStream("/keystore"), password)

    val trustStore = KeyStore.getInstance(KeyStore.getDefaultType)
    trustStore.load(getClass.getResourceAsStream("/truststore"), password)

    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    keyManagerFactory.init(keyStore, password)

    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    trustManagerFactory.init(trustStore)

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    context
  }
}

class SSLTcpServer(props: Props, address: String, port: Int) extends TcpServer(address, port) {
  private val serverSSL = TLS(SSLTcpServer.sslContext, TLSProtocol.negotiateNewSession, TLSRole.server)

  override def handler: Sink[Tcp.IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { conn =>
    println("Client connected from: " + conn.remoteAddress)

    val actor: ActorRef = context.actorOf(props)
    val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(actor, PoisonPill))

    val out: Source[ByteString, Unit] = Source.actorRef[ByteString](1, OverflowStrategy.fail)
      .mapMaterializedValue(actor ! _)
    val flow: Flow[ByteString, ByteString, NotUsed] = Flow.fromSinkAndSourceMat(in, out)(Keep.none)

    val ssl = Flow[SslTlsInbound]
      .collect[ByteString] { case SessionBytes(_, bytes) => println(bytes.mkString("ssl ByteString(", ", ", ")"));
      bytes
    }
      .via(flow)
      .map[SslTlsOutbound](SendBytes)

    conn handleWith serverSSL.reversed.join(ssl).alsoTo(Sink.onComplete(_ => println("Client disconnected")))
  }
}

