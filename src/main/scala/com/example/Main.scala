package com.temp

import com.example.handler.SnifferServerProps
import com.scalanerds.wireServer.WireServer

object Main {
  def main(args: Array[String]): Unit = {
    WireServer(SnifferServerProps)
  }
}
