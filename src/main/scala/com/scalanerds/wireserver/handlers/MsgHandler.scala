package com.scalanerds.wireserver.handlers

import akka.actor.{Actor, ActorRef, Stash}
import akka.event.Logging
import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes._
import com.scalanerds.wireserver.wire.Message
import com.scalanerds.wireserver.wire.opcodes._


abstract class MsgHandler extends Actor with Stash {
  var connection: ActorRef = _
  val log = Logging(context.system, this)


  override def postStop(): Unit = {
    log.debug("walter died " + connection.path)
    super.postStop()
  }

  def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case ref: ActorRef =>
      connection = ref
      context.become(initialized)
      unstashAll()

    case _ => stash()
  }


  def initialized: Receive = {
    /**
      * WirePacket receivers
      */
    case BytesToClient(bytes) =>
      connection ! beforeWrite(bytes)

    case segment: ByteString =>
      onReceived(BytesFromClient(segment))

    case m => println(s"unknown message $m")

  }

  /**
    * Override this method to intercept outcoming bytes
    *
    * @param bytes
    * @return
    */
  def beforeWrite(bytes: ByteString): ByteString = bytes


  /**
    * Override this method to handle incoming requests
    * By default, parse messages from received requests
    *
    * @param request
    */
  def onReceived(request: WirePacket): Unit = {
    parse(request.bytes)
  }

  def parse(data: ByteString): Unit = {
    Message(data) match {
      case msg: OpReply => onOpReply(msg)
      case msg: OpMsg => onOpMsg(msg)
      case msg: OpUpdate => onOpUpdate(msg)
      case msg: OpInsert => onOpInsert(msg)
      case msg: OpQuery => onOpQuery(msg)
      case msg: OpGetMore => onOpGetMore(msg)
      case msg: OpDelete => onOpDelete(msg)
      case msg: OpKillCursor => onOpKillCursor(msg)
      case msg: OpCommand => onOpCommand(msg)
      case msg: OpCommandReply => onOpCommandReply(msg)
      case msg => onError(msg)
    }
  }

  def onOpReply(msg: OpReply): Unit = {}

  def onOpMsg(msg: OpMsg): Unit = {}

  def onOpUpdate(msg: OpUpdate): Unit = {}

  def onOpInsert(msg: OpInsert): Unit = {}

  def onOpQuery(msg: OpQuery): Unit = {}

  def onOpGetMore(msg: OpGetMore): Unit = {}

  def onOpDelete(msg: OpDelete): Unit = {}

  def onOpKillCursor(msg: OpKillCursor): Unit = {}

  def onOpCommand(msg: OpCommand): Unit = {}

  def onOpCommandReply(msg: OpCommandReply): Unit = {}

  def onError(msg: Any): Unit = {}

  /**
    * Stop this actor
    */
  def stop() {
    log.debug("MsgHandler stop " + connection.path)
    context stop self
  }
}


