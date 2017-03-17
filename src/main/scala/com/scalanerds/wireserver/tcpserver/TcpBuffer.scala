package com.scalanerds.wireserver.tcpserver

import akka.util.ByteString
import com.scalanerds.wireserver.messageTypes.{FromClient, WirePacket}
import com.scalanerds.wireserver.utils.Utils._

trait TcpBuffer {
  this: {
    def onReceived(data: WirePacket): Unit
    def packetWrapper(data: ByteString): WirePacket
  } =>

  var storage = Vector.empty[ByteString]
  var storedBytes = 0L
  // length of the ByteString as declared in the first 4 bytes of the first segment
  var frameLength : Option[Int] = None

  /**
    * Buffer that joins consequent segments into a single frame
    * @param segment
    */
  def buffer(segment: ByteString): Unit = {
    // get the length of the ByteString by reading the first 4 bytes as Int
    val msgLength = segment.take(4).toArray.toInt
    // if we don't have anything in buffer and the length is equal to the ByteString length
    // then the frame is complete
    if (storedBytes == 0 && msgLength == segment.length) {
      resetBuffer()
      onReceived(packetWrapper(segment))
    } else {
      //if is the first incomplete ByteString then store the msgLength
      if (frameLength.isEmpty)
        frameLength = Some(msgLength)
      // store the incomplete ByteString
      storage :+= segment
      // store how many bytes we have stored
      storedBytes += segment.size
      // check if the frame is complete
      if (storedBytes >= frameLength.get) {
        // join the segments
        val frame = ByteString(storage.flatten.toArray)
        resetBuffer()
        onReceived(packetWrapper(frame))
      }
    }
  }

  /**
    * Reset the framing buffer
    */
  private def resetBuffer() = {
    storage = Vector.empty[ByteString]
    storedBytes = 0
    frameLength = None
  }
}
