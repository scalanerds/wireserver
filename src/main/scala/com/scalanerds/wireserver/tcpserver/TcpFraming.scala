package com.scalanerds.wireserver.tcpserver

import java.nio.ByteOrder

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString

/**
  * Tcp messages are split in frames, frames sometimes don't have the same length
  * of the bytes received, the classes that mix this trait use framing to read from
  * the socket messages
  */
trait TcpFraming {

  /** framing used in the flow to get from the socket complete messages */
  val framing: Flow[ByteString, ByteString, NotUsed] =
    Flow.fromGraph(GraphDSL.create() { b =>
      implicit val order: ByteOrder = ByteOrder.LITTLE_ENDIAN

      class FrameParser extends GraphStage[FlowShape[ByteString, ByteString]] {

        val in: Inlet[ByteString] = Inlet[ByteString]("FrameParser.in")
        val out: Outlet[ByteString] = Outlet[ByteString]("FrameParser.out")
        override val shape: FlowShape[ByteString, ByteString] =
          FlowShape.of(in, out)

        override def createLogic(
            inheritedAttributes: Attributes): GraphStageLogic =
          new GraphStageLogic(shape) {

            /** this holds the received but not yet parsed bytes */
            var stash: ByteString = ByteString.empty

            /** this holds the current message length or -1 if at a boundary */
            var needed: Int = -1

            setHandler(out, new OutHandler {
              override def onPull(): Unit = {
                if (isClosed(in))
                  run()
                else
                  pull(in)
              }
            })
            setHandler(
              in,
              new InHandler {
                override def onPush(): Unit = {
                  val bytes = grab(in)
                  stash = stash ++ bytes
                  run()
                }

                override def onUpstreamFinish(): Unit = {
                  // either we are done
                  if (stash.isEmpty)
                    completeStage()
                  // or we still have bytes to emit
                  // wait with completion and let run() complete when the
                  // rest of the stash has been sent downstream
                  else if (isAvailable(out))
                    run()
                }
              }
            )

            private def run(): Unit = {
              needed match {
                // are we at a boundary? then figure out next length
                case -1 if stash.length < 4 =>
                  if (isClosed(in))
                    completeStage()
                  else
                    pull(in)
                case -1 =>
                  needed = stash.iterator.getInt
                  run() // cycle back to possibly already emit the next chunk
                case x if stash.length < x =>
                  // we are in the middle of a message, need more bytes,
                  // or have to stop if input closed
                  if (isClosed(in))
                    completeStage()
                  else
                    pull(in)
                case _ =>
                  val emit = stash.take(needed)
                  stash = stash.drop(needed)
                  needed = -1
                  push(out, emit)
              }
            }
          }
      }

      val flow = b.add(Flow[ByteString].via(new FrameParser))

      FlowShape(flow.in, flow.out)
    })
}
