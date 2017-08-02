package com.scalanerds.wireserver.tcpserver

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Tcp}
import com.scalanerds.wireserver.messageTypes.{GetInfo, Info}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object TcpServer {
  def props(address: String = "localhost",
            port: Int = 3400): Props =
    Props(classOf[TcpServer], address, port)
}

abstract class TcpServer(address: String, port: Int) extends Actor {
  implicit val system: ActorSystem = context.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def handler: Sink[Tcp.IncomingConnection, Future[Done]]

  private val connections = Tcp().bind(address, port)
  private val binding = connections.to(handler).run()

  binding.onComplete {
    case Success(b) =>
      println("Server started, listening on: " + b.localAddress)
    case Failure(e) =>
      println(s"Server could not bind to $address:$port ${e.getMessage}")
      context stop self
  }

  override def receive: Receive = {
    case msg => println("Unhandled message:", msg)
  }
}
