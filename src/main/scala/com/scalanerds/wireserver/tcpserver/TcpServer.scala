package com.scalanerds.wireserver.tcpserver

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Tcp}
import com.scalanerds.wireserver.utils.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Tcp Server
  *
  * @param address address used by the tcp server
  * @param port port used by the tcp server
  */
abstract class TcpServer(address: String, port: Int) extends Actor with Logger {
  implicit val system      : ActorSystem       = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def handler: Sink[Tcp.IncomingConnection, Future[Done]]

  private val connections = Tcp().bind(address, port)
  private val binding     = connections.to(handler).run()

  binding.onComplete {
    case Success(b) =>
      logger.debug("Server started, listening on: " + b.localAddress)
    case Failure(e) =>
      logger.debug(s"Server could not bind to $address:$port ${e.getMessage}")
      context stop self
  }

  override def receive: Receive = {
    case msg => logger.debug("Unhandled message:", msg)
  }
}

object TcpServer {
  /** tcp server props */
  def props(address: String = "localhost",
      port: Int = 3400): Props =
    Props(classOf[TcpServer], address, port)
}
