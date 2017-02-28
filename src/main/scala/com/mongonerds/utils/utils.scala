package com.mongonerds.utils

import java.nio.ByteBuffer

import akka.util.ByteString
import org.bson.{BSON, BSONObject}


object Utils {
  implicit def consumable[T](i: Iterator[T]) = new {
    def cTake(n: Int) = {
      (for (_ <- 1 to n) yield i.next()).iterator
    }

    def cDrop(n: Int) {
      (1 to n).foreach(_ => i.next())
    }
  }

  implicit class IntToByteArray(value: Int) {
    def toByteArray(endian: Boolean = true): Array[Byte] = {
      val arr = ByteBuffer.allocate(4).putInt(value).array
      if (endian) arr.reverse else arr
    }
  }

  implicit class LongToByteArray(value: Long) {
    def toByteArray(endian: Boolean = true): Array[Byte] = {
      val arr = ByteBuffer.allocate(8).putLong(value).array
      if (endian) arr.reverse else arr
    }
  }

  implicit class IntToByte(arr: Array[Int]) {
    def toByteArray: Array[Byte] = {
      arr.map(_.toByteArray()).reduce(_ ++ _)
    }
  }

  implicit class ByteArray(arr: Array[Byte]) {
    def toInt: Int = {
      BigInt(arr.reverse).toInt
    }

    def toLong: Long = {
      BigInt(arr.reverse).toLong
    }

    def toUTFString: String = {
      new String(arr, "UTF-8")
    }

    def toBson: BSONObject = {
      BSON.decode(arr)
    }

    def toBSONArray: Array[BSONObject] = {
      def transform(it: Iterator[Byte], acc: List[BSONObject]): List[BSONObject] = {
        if (it.isEmpty) acc
        else {
          val len = it.take(4).toArray.toInt
          val bson = it.cTake(len).toArray.toBson
          transform(it, bson :: acc)
        }
      }
      transform(arr.iterator, Nil).toArray.reverse
    }
  }

  implicit class BSONToByteArray(bson: BSONObject) {
    def toByteArray: Array[Byte] = {
      BSON.encode(bson)
    }
  }

  implicit class BSONArrayToByteArray(arr: Array[BSONObject]) {
    def toByteArray: Array[Byte] = {
      arr.foldLeft(Array[Byte]())(_ ++ _.toByteArray)
    }
  }


  implicit class StringToArray(s: String) {
    def toCString: Array[Byte] = {
      ByteString.fromString(s).toArray ++ Array[Byte](0)
    }
  }

}

