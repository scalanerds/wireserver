package com.scalanerds.wireserver.tcpserver

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.stream.TLSProtocol._
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.scalanerds.wireserver.messages.GracefulKill

import scala.concurrent.Future

object DualTcpServer {
  def props(props: (InetSocketAddress, InetSocketAddress) => Props, address: String = "localhost", port: Int = 3301):
  Props =
    Props(classOf[DualTcpServer], props, address, port)
}

/** *
  * Tcp server that works with plain and SSL connections
  *
  * @param props   the props of the actor that handles the ByteStream
  * @param address the server binding address
  * @param port    the server binding port
  */
class DualTcpServer(props: (InetSocketAddress, InetSocketAddress) => Props, address: String, port: Int) extends
  TcpServer(address, port) with TcpSSL with TcpFraming {
  private val serverSSL = TLS(sslContext("/server.keystore", "/truststore"),
    TLSProtocol.negotiateNewSession, TLSRole.server)

  override def handler: Sink[Tcp.IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { conn =>
    var isSSL: Option[Boolean] = None
    println("Client connected from: " + conn.remoteAddress + " dual")

    val streamHandler: ActorRef = context.actorOf(props(conn.remoteAddress, conn.localAddress))

    val getFlow = (name: String) => {
      val forwarderActor: ActorRef = context.actorOf(Props(new ForwarderActor(streamHandler)), name + Math.random())
      val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(forwarderActor, GracefulKill))
      val out = Source.actorRef[ByteString](100, OverflowStrategy.fail)
        .mapMaterializedValue(forwarderActor ! _)

      Flow.fromSinkAndSourceMat(framing.to(in), out)(Keep.none)
    }

    val plainFlow = getFlow("plainActor")

    val ssl = Flow[SslTlsInbound]
      .collect[ByteString] { case SessionBytes(_, bytes) => bytes }
      .via(getFlow("sslActor"))
      .map[SslTlsOutbound](SendBytes)

    // handle ssl connections
    val sslFlow: Flow[ByteString, ByteString, NotUsed] = serverSSL.reversed.join(ssl).alsoTo(Sink.onComplete(_ => {
      println("Client disconnected")
      streamHandler ! GracefulKill
    }))

    // redirects the stream to sslFlow or plainFlow
    val router = Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val src = b.add(Flow[ByteString].map(stream => {
        // checks if the first message is a handshake
        if (isSSL.isEmpty) isSSL = Some(stream(0) == 22 && stream.length != 22)
        stream
      }))

      val outbound = b.add(Flow[ByteString])

      val bcast = b.add(Broadcast[ByteString](2))
      val merge = b.add(Merge[ByteString](2))

      val plainFilter = Flow[ByteString].filter(_ => !isSSL.getOrElse(false))
      val sslFilter = Flow[ByteString].filter(_ => isSSL.getOrElse(false))

      src ~> bcast ~> plainFilter ~> plainFlow ~> merge ~> outbound
             bcast ~> sslFilter ~> sslFlow ~> merge

      FlowShape(src.in, outbound.out)
    })

    conn handleWith router
  }
}




