package com.scalanerds.wireserver.example

import com.scalanerds.wireserver.example.handler.SnifferServerProps
import com.scalanerds.wireserver.WireServer

object Main extends App{
    WireServer(SnifferServerProps)
}
