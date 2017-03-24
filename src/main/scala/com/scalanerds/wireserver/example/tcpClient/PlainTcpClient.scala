package com.scalanerds.wireserver.example.tcpClient


import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill, Stash}
import akka.event.Logging
import akka.io.Tcp._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, Tcp}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.tcpserver.TcpBuffer

class PlainTcpClient(listener: ActorRef, address: String, port: Int)
  extends TcpBuffer with Stash {

  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  var connection: ActorRef = _
  val log = Logging(context.system, this)

  override def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case ref: ActorRef =>
      connection = ref
      log.debug("context become")
      context.become(initialized)
      unstashAll()

    case _ => stash()
  }

  def initialized: Receive = {
    /**
      * WirePacket receivers
      */
    case Received(segment: ByteString) =>
      buffer(segment)

    case ToServer(bytes) =>
      connection ! beforeWrite(bytes)

    case segment : ByteString => buffer(segment)

  }

  val in: Sink[ByteString, NotUsed] = Flow[ByteString].to(Sink.actorRef(self, PoisonPill))

  val out: Source[ByteString, Unit] = Source.actorRef[ByteString](1, OverflowStrategy.fail)
    .mapMaterializedValue(self ! _)

  val flow: Flow[ByteString, ByteString, NotUsed] = Flow.fromSinkAndSourceMat(in, out)(Keep.none)


  val client = Tcp().outgoingConnection(address, port)

  client.join(flow).run()

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