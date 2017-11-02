package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Stash}
import akka.event.Logging
import akka.util.ByteString
import com.scalanerds.wireserver.messages._
import com.scalanerds.wireserver.messages.request.BytesFromClient
import com.scalanerds.wireserver.messages.response.BytesToClient
import com.scalanerds.wireserver.utils.Logger
import com.scalanerds.wireserver.wire.message.traits.Message
import com.scalanerds.wireserver.wire.opcodes._

/**
  * Handle messages received from mongo client
  */
abstract class MsgHandler extends Actor with Stash with Logger {
  /** connection to mongo client */
  var connection: Option[ActorRef] = None
  val log = Logging(context.system, this)


  override def postStop(): Unit = {
    connection match {
      case Some(conn) =>
        log.debug("Walter died " + conn.path)
      case None =>
        log.debug("Walter died without processing valid messages")
    }
    super.postStop()
  }

  /** start the handler uninitialized */
  def receive: Receive = uninitialized

  /** receive while waiting for a connection to mongo client */
  def uninitialized: Receive = {
    case GracefulKill => stop()
    case ref: ActorRef =>
      connection = Some(ref)
      context.become(initialized)
      unstashAll()

    /** save received messages in stash */
    case _ => stash()
  }


  def initialized: Receive = {
    /**
      * WirePacket receivers
      */
    case BytesToClient(bytes) =>
      // send message to actor
      connection.foreach(_ ! beforeWrite(bytes))

    case GracefulKill =>
      logger.debug("GracefulKill")
      stop()

    case segment: ByteString =>
      onReceived(BytesFromClient(segment))

    case m =>
      logger.debug(s"unknown message $m")
      fail(stop)

  }

  /**
    * Override this method to intercept outgoing bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes


  /**
    * Override this method to handle incoming requests
    * By default, parse messages from received requests
    *
    * @param request mongo client request
    */
  def onReceived(request: WirePacket): Unit = {
    parse(request.bytes)
  }

  /** parse the byte string received from mongo client */
  def parse(data: ByteString): Unit = {
    Message(data).fold(onError(data)) {
      case msg: OpReply => onOpReply(msg)
      case msg: OpMsg => onOpMsg(msg)
      case msg: OpUpdate => onOpUpdate(msg)
      case msg: OpInsert => onOpInsert(msg)
      case msg: OpQuery => onOpQuery(msg)
      case msg: OpGetMore => onOpGetMore(msg)
      case msg: OpDelete => onOpDelete(msg)
      case msg: OpKillCursors => onOpKillCursor(msg)
      case msg: OpCommand => onOpCommand(msg)
      case msg: OpCommandReply => onOpCommandReply(msg)
    }
  }

  /////////////////////////////////////////////
  // unimplemented messages
  /////////////////////////////////////////////


  def onOpReply(msg: OpReply): Unit = {}

  def onOpMsg(msg: OpMsg): Unit = {}

  def onOpUpdate(msg: OpUpdate): Unit = {}

  def onOpInsert(msg: OpInsert): Unit = {}

  def onOpQuery(msg: OpQuery): Unit = {}

  def onOpGetMore(msg: OpGetMore): Unit = {}

  def onOpDelete(msg: OpDelete): Unit = {}

  def onOpKillCursor(msg: OpKillCursors): Unit = {}

  def onOpCommand(msg: OpCommand): Unit = {}

  def onOpCommandReply(msg: OpCommandReply): Unit = {}

  def onError(msg: Any): Unit = {}

  /**
    * Stop this actor
    *
    */
  def stop() {
    connection.foreach { conn =>
      log.debug("MsgHandler stop " + conn.path)
    }
    context stop self
  }
}


