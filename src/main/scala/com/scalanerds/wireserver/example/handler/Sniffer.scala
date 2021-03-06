package com.scalanerds.wireserver.example.handler

import java.net.InetSocketAddress

import akka.actor.{ActorRef, PoisonPill, Props}
import com.scalanerds.wireserver.example.tcpClient.PlainTcpClient
import com.scalanerds.wireserver.handlers.MsgHandler
import com.scalanerds.wireserver.messages._
import com.scalanerds.wireserver.messages.request.{
  BytesFromClient,
  BytesToServer
}
import com.scalanerds.wireserver.messages.response.{
  BytesFromServer,
  BytesToClient
}
import com.scalanerds.wireserver.wire.opcodes._

object Sniffer {
  def props(remote: InetSocketAddress, local: InetSocketAddress) =
    Props(classOf[Sniffer], remote, local)
}

class Sniffer(remote: InetSocketAddress, local: InetSocketAddress)
    extends MsgHandler {
  log.debug(s"\nsniffer remote address: $remote\nsniffer local address $local")

  var tcpClient: Option[ActorRef] = None

  override def preStart(): Unit = {
    tcpClient = Some(
      context.actorOf(Props(new PlainTcpClient(self, "localhost", 27017)),
                      "sniffer"))
    super.preStart()
  }

  override def initialized: Receive = {
    case response: BytesFromServer =>
      onReceived(response)
    case msg =>
      super.initialized(msg)
  }

  override def onReceived(msg: WirePacket): Unit = msg match {
    case BytesFromClient(bytes) =>
      // log
      connection.foreach { conn =>
        log.warning(
          "alice says: " + conn.path + "\n" + bytes
            .mkString("ByteString(", ", ", ")"))
      }

      parse(bytes)
      // send bytes to tcpClient
      tcpClient.foreach(_ ! BytesToServer(bytes))
    case BytesFromServer(bytes) =>
      connection.foreach { conn =>
        log.error(
          "bob replies: " + conn.path + "\n" + bytes
            .mkString("ByteString(", ", ", ")"))
      }
      parse(bytes)
      self ! BytesToClient(bytes)
  }

  override def onOpReply(msg: OpReply): Unit =
    log.debug(s"OpReply\n${msg.msgHeader}\n${msg.documents.mkString("\n")}\n")

  override def onOpMsg(msg: OpMsg): Unit =
    log.debug(s"OpMsg\n${msg.msgHeader}\n")

  override def onOpUpdate(msg: OpUpdate): Unit =
    log.debug(s"OpUpdate\n${msg.msgHeader}\n")

  override def onOpInsert(msg: OpInsert): Unit =
    log.debug(s"OpInsert\n${msg.msgHeader}\n")

  override def onOpQuery(msg: OpQuery): Unit =
    log.debug(s"OpQuery\n${msg.msgHeader}\n${msg.query}\n")

  override def onOpGetMore(msg: OpGetMore): Unit =
    log.debug(s"OpGetMore\n${msg.msgHeader}\n")

  override def onOpDelete(msg: OpDelete): Unit =
    log.debug(s"OpDelete\n${msg.msgHeader}\n")

  override def onOpKillCursor(msg: OpKillCursors): Unit =
    log.debug(s"OpKillCursor\n${msg.msgHeader}\n")

  override def onOpCommand(msg: OpCommand): Unit =
    log.debug(s"OpCommand\n${msg.msgHeader}\n")

  override def onOpCommandReply(msg: OpCommandReply): Unit =
    log.debug(s"OpCommandReply\n${msg.msgHeader}\n")

  override def onError(msg: Any): Unit = {
    log.debug("sniffer error")
    // on error kill the tcpClient actor
    tcpClient.foreach(_ ! PoisonPill)
    context stop self
    log.debug(s"Unknown message\n$msg\n")
  }

  override def stop() {
    logger.debug("sending poisonPill to tcpCLient")
    // stop the tcp client
    tcpClient.foreach(_ ! PoisonPill)
    super.stop()
  }
}
