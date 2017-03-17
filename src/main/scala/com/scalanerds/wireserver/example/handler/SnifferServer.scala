package com.scalanerds.wireserver.example.handler

import java.net.InetSocketAddress

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ActorRef, Props}
import akka.io.Tcp._
import com.scalanerds.wireserver.example.tcpClient.TcpClient
import com.scalanerds.wireserver.handlers.{HandlerProps, MsgHandler}
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.wire.opcodes._


object SnifferServerProps extends HandlerProps {
  def props(connection: ActorRef) = Props(classOf[SnifferServer], connection)
}

class SnifferServer(connection: ActorRef) extends MsgHandler(connection) {

  var tcpClient: ActorRef = _

  override def receive: Receive = {

    /**
      * Handle server responses
      */
    case response: FromServer =>
      onReceived(response)

    /**
      * Pass all other messages to underlying Handler's own receiver
      */
    case msg =>
      super.receive(msg)
  }

  override def preStart(): Unit = {
    tcpClient = context.actorOf(Props(new TcpClient(self, new InetSocketAddress("localhost", 27017))), "sniffer")
    super.preStart()
  }

  override def onReceived(request: FromClient): Unit = {
    log.warning("alice says: " + connection.path) // \n" + data.mkString(", "))
    parse(request.bytes)
    tcpClient ! ToServer(request.bytes)
  }

  def onReceived(response: FromServer): Unit = {
    log.error("bob replies: " + connection.path) // \n" + packet.data.mkString(", "))
    parse(response.bytes)
    self ! ToClient(response.bytes)
  }

  override def onOpReply(msg: OpReply): Unit = log.debug(s"OpReply\n${msg.msgHeader}\n${msg.documents.mkString("\n")
  }\n")

  override def onOpMsg(msg: OpMsg): Unit = log.debug(s"OpMsg\n${msg.msgHeader}\n")

  override def onOpUpdate(msg: OpUpdate): Unit = log.debug(s"OpUpdate\n${msg.msgHeader}\n")

  override def onOpInsert(msg: OpInsert): Unit = log.debug(s"OpInsert\n${msg.msgHeader}\n")

  override def onOpQuery(msg: OpQuery): Unit = log.debug(s"OpQuery\n${msg.msgHeader}\n${msg.query}\n")

  override def onOpGetMore(msg: OpGetMore): Unit = log.debug(s"OpGetMore\n${msg.msgHeader}\n")

  override def onOpDelete(msg: OpDelete): Unit = log.debug(s"OpDelete\n${msg.msgHeader}\n")

  override def onOpKillCursor(msg: OpKillCursor): Unit = log.debug(s"OpKillCursor\n${msg.msgHeader}\n")

  override def onOpCommand(msg: OpCommand): Unit = log.debug(s"OpCommand\n${msg.msgHeader}\n")

  override def onOpCommandReply(msg: OpCommandReply): Unit = log.debug(s"OpCommandReply\n${msg.msgHeader}\n")

  override def onError(msg: Any): Unit = {
    log.debug("sniffer error")
    tcpClient ! DropConnection
    tcpClient ! Stop
    connection ! Close
    log.debug(s"Unknown message\n$msg\n")
  }
}
