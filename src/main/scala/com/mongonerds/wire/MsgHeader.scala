package com.mongonerds.wire

class MsgHeader(val requestId: Int,
                val responseTo: Int,
                val opCode: Int){
  def toArr : Array[Int] ={
    Array(requestId, responseTo, opCode)
  }
}
