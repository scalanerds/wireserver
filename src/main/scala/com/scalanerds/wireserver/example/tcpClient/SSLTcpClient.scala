package com.scalanerds.wireserver.example.tcpClient

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill}
import akka.event.Logging
import akka.stream.TLSProtocol.{SessionBytes, SslTlsInbound, SslTlsOutbound}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source, TLS, Tcp}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.{FromServer, ToServer, WirePacket}
import com.scalanerds.wireserver.tcpserver.{TcpBuffer, TcpSSL}

import scala.concurrent.Future

class SSLTcpClient(listener: ActorRef, address: String, port: Int)
  extends Actor with TcpBuffer with TcpSSL {
  val log = Logging(context.system, this)

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  log.debug("Ctr SSLTcpCLient")

  private val clientSSL = TLS(sslContext("/client.keystore", "/truststore"), TLSProtocol.negotiateNewSession, TLSRole
    .client)

  val sink: Sink[SslTlsInbound, NotUsed] = Flow[SslTlsInbound].collect[ByteString] { case SessionBytes(_, bytes) =>
    bytes }.to(Sink.actorRef(self, PoisonPill))
  val conn: Flow[ByteString, ByteString, Future[Tcp.OutgoingConnection]] = Tcp().outgoingConnection(address, port)

  val sslFlow: Flow[SslTlsOutbound, SslTlsInbound, NotUsed] = clientSSL.join(conn)

  val connection: ActorRef = Source.actorRef(1, OverflowStrategy.fail).via(sslFlow).to(sink).run()


  // remove later
  override def receive: Receive = {

    case ToServer(bytes) =>
      log.debug(s"toserver $bytes")
      connection ! beforeWrite(bytes)

    case segment: ByteString => {
      log.debug(s"SSL ${segment.mkString("ByteString(", ", ", ")")}")
      buffer(segment)
    }

    case m => log.debug(s"unknown $m")
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
