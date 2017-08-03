package com.scalanerds.wireserver.example.tcpClient

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, KillSwitches, SharedKillSwitch}
import akka.util.ByteString
import com.scalanerds.wireserver.handlers.GracefulKill
import com.scalanerds.wireserver.messageTypes.{BytesFromServer, BytesToServer, WirePacket}


abstract class TcpClient(listener: ActorRef, address: String, port: Int)
  extends Actor {


  val killSwitch: SharedKillSwitch = KillSwitches.shared("switch")


  override def aroundPostStop(): Unit = {
    println("killswitch")
    killSwitch.shutdown()
    super.aroundPostStop()
  }

  implicit val system: ActorSystem = context.system

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def connection: ActorRef

  override def receive: Receive = {
    case GracefulKill =>
      killSwitch.shutdown()

    case BytesToServer(bytes) =>
      connection ! beforeWrite(bytes)

    case segment: ByteString => onReceived(segment)
  }

  def onReceived(msg: ByteString): Unit = {
    listener ! BytesFromServer(msg)
  }

  def packetWrapper(packet: ByteString): WirePacket = {
    BytesFromServer(packet)
  }

  /**
    * Override this method to intercept outcoming bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes
}
