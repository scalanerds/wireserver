package com.mongonerds.tools

import java.nio.ByteBuffer

object Tools {
  def intToByteArray(value: Int, endian: Boolean = true): Array[Byte] = {
    val arr = ByteBuffer.allocate(4).putInt(value).array
    if (endian) arr.reverse else arr
  }

  def longToByteArray(value: Long, endian: Boolean = true): Array[Byte] = {
    val arr = ByteBuffer.allocate(8).putLong(value).array
    if (endian) arr.reverse else arr
  }

  def intsAsByteArray(values: Int*): Array[Byte] = {
    values.map(intToByteArray(_)).reduce(_ ++ _)
  }

  def implicit takeBytes(arr: Array[Byte],
                take: Int = 4,
                drop: Int = 0): (Array[Byte], Array[Byte]) = {
    arr.drop(drop).splitAt(take)
  }
}

trait
