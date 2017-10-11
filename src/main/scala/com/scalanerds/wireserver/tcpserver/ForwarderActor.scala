package com.scalanerds.wireserver.tcpserver

import akka.actor.{Actor, ActorRef, PoisonPill, Stash}
import akka.util.ByteString
import com.scalanerds.wireserver.messages.GracefulKill

/** *
  * Actor that forwards the bytestrings after everything has been initialized
  *
  * this actor is required to avoid creating two actors that process the ByteStream, as one can't be shared
  * between the sink and the source of the connection because the flow in the router would fail in the merge
  *
  * @param streamHandler actor that will process the ByteStream
  */
class ForwarderActor(streamHandler: ActorRef) extends Actor with Stash {
  var src: Option[ActorRef] = None
  var gotBytes = false

  override def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case GracefulKill =>
      streamHandler ! GracefulKill
      self ! PoisonPill
    case ref: ActorRef if gotBytes =>
      streamHandler ! ref
      context.become(initialized)
      unstashAll()
    case ref: ActorRef =>
      src = Some(ref)
    case _: ByteString if src.nonEmpty =>
      stash()
      context.become(initialized)
      src.foreach(streamHandler ! _)
      unstashAll()
    case _: ByteString =>
      stash()
      gotBytes = true
    case m => println(s"forwarder uninitialized got unknown message $m")
  }

  def initialized: Receive = {

    case bytes: ByteString =>
      streamHandler forward bytes
    case m => s"forwarder got unknown message $m"
  }
}
