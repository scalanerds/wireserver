package com.scalanerds.wireserver.example.tcpClient

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.stream.{ActorMaterializer, KillSwitches, SharedKillSwitch}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.request.BytesToServer
import com.scalanerds.wireserver.messages.response.BytesFromServer
import com.scalanerds.wireserver.messages.{GracefulKill, WirePacket}
import com.scalanerds.wireserver.utils.Logger

/**
  * Tcp Client
  *
  * @param listener actor to handle received messages
  * @param address URL to establish connection
  * @param port port to establish connection
  */
abstract class TcpClient(listener: ActorRef, address: String, port: Int)
  extends Actor with Logger {


  val killSwitch: SharedKillSwitch = KillSwitches.shared("switch")


  override def aroundPostStop(): Unit = {
    logger.debug("killswitch")
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
    * Override this method to intercept outgoing bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes
}
