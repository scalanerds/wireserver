package com.scalanerds.wireserver.tcpserver

import java.nio.ByteOrder

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString

trait TcpFraming {
  val framing: Flow[ByteString, ByteString, NotUsed] = Flow.fromGraph(GraphDSL.create() { b =>
    implicit val order = ByteOrder.LITTLE_ENDIAN

    class FrameParser extends GraphStage[FlowShape[ByteString, ByteString]] {

      val in = Inlet[ByteString]("FrameParser.in")
      val out = Outlet[ByteString]("FrameParser.out")
      override val shape = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

        // this holds the received but not yet parsed bytes
        var stash = ByteString.empty
        // this holds the current message length or -1 if at a boundary
        var needed = -1

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            if (isClosed(in)) run()
            else pull(in)
          }
        })
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val bytes = grab(in)
            //            println(s"stashing ${bytes.mkString("ByteString(", ", ", ")")}")
            stash = stash ++ bytes
            run()
          }

          override def onUpstreamFinish(): Unit = {
            // either we are done
            if (stash.isEmpty) completeStage()
            // or we still have bytes to emit
            // wait with completion and let run() complete when the
            // rest of the stash has been sent downstream
            else if (isAvailable(out)) run()
          }
        })

        private def run(): Unit = {
          if (needed == -1) {
            // are we at a boundary? then figure out next length
            if (stash.length < 4) {
              if (isClosed(in)) completeStage()
              else pull(in)
            } else {
              //              needed = stash.take(4).toArray.toInt
              needed = stash.iterator.getInt
              //              stash = stash.drop(4)
              run() // cycle back to possibly already emit the next chunk
            }
          } else if (stash.length < needed) {
            // we are in the middle of a message, need more bytes,
            // or have to stop if input closed
            if (isClosed(in)) completeStage()
            else pull(in)
          } else {
            // we have enough to emit at least one message, so do it
            val emit = stash.take(needed)
            stash = stash.drop(needed)
            needed = -1
            //            println(s"emit ${emit.mkString("ByteString(", ", ", ")")}")
            push(out, emit)
          }
        }
      }
    }

    val flow = b.add(Flow[ByteString].via(new FrameParser))

    FlowShape(flow.in, flow.out)
  })
}
