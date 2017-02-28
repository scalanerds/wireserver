package com.mongonerds.utils

import java.nio.ByteBuffer

import akka.util.ByteString
import org.bson.{BSON, BSONObject}

import scala.language.implicitConversions


object Utils {

  implicit class consumable[T](i: Iterator[T]) {
    def cTake(n: Int): Iterator[T] = {
      (for (_ <- 1 to n) yield i.next()).iterator
    }

    def cDrop(n: Int) {
      (1 to n).foreach(_ => i.next())
    }
  }

  implicit class IntToByteArray(value: Int) {
    def toByteArray: Array[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.reverse
    }
  }

  implicit class LongToByteArray(value: Long) {
    def toByteArray: Array[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(8).putLong(value).array.reverse
    }
  }

  implicit class IntToByte(arr: Array[Int]) {
    def toByteArray: Array[Byte] = {
      arr.map(_.toByteArray).reduce(_ ++ _)
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
      arr.iterator.getBsonArray
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
    def toByteArray: Array[Byte] = {
      ByteString.fromString(s).toArray ++ Array[Byte](0)
    }
  }

  implicit class ByteIterator(i: Iterator[Byte]) {
    def getString: String = {
      i.takeWhile(_ != 0).toArray.toUTFString
    }

    def getInt: Int = {
      i.cTake(4).toArray.toInt
    }

    def getLong: Long = {
      i.cTake(8).toArray.toLong
    }

    def getBson: BSONObject = {
      i.cTake(i.take(4).toArray.toInt).toArray.toBson
    }

    def getBsonArray: Array[BSONObject] = {
      def transform(it: Iterator[Byte], acc: List[BSONObject]): List[BSONObject] = {
        if (it.isEmpty) acc
        else {
          val bson = it.getBson
          transform(it, bson :: acc)
        }
      }

      transform(i, Nil).toArray.reverse
    }
  }

}

