package com.scalanerds.wireserver.example

import com.scalanerds.wireserver.example.handler.SnifferServerProps
import com.scalanerds.wireserver.WireServer

object Main {
  def main(args: Array[String]): Unit = {
    WireServer(SnifferServerProps)
  }
}
