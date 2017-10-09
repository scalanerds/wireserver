package com.scalanerds.wireserver.utils

import java.nio.ByteBuffer

import akka.util.ByteString
import org.bson.{BsonDocument, RawBsonDocument, codecs}

import scala.language.implicitConversions


object Conversions {

  implicit def byte2bool(b: Byte): Boolean = b.toInt != 0
  implicit def bool2byte(b: Boolean): Byte = (if (b) 1 else 0).toByte

  implicit class consumable[T](i: Iterator[T]) {
    def cTake(n: Int): Iterator[T] = {
      (1 to n).map(_ => i.next()).iterator
    }

    def cDrop(n: Int) {
      (1 to n).foreach(_ => i.next())
    }
  }

  implicit class IntToArray(value: Int) {
    def toByteArray: Array[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.reverse
    }

    def toBooleanArray: Array[Boolean] = {
      // get the first 8 bits which are required for the flags
      (0 until 8).map(i => ((value >> i) & 1) != 0).toArray
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

    def toBson: BsonDocument = {
      new RawBsonDocument(arr).asDocument()
    }

    def toBSONArray: Array[BsonDocument] = {
      arr.iterator.getBsonArray
    }

    def binaryToInt: Int = {
      arr.zipWithIndex.map(p => p._1 << p._2).sum
    }
  }

  implicit class BSONToByteArray(bson: BsonDocument) {
    def toByteArray: Array[Byte] = {
      val buf = new RawBsonDocument(bson, new codecs.BsonDocumentCodec)
        .getByteBuffer
      val arr = new Array[Byte](buf.getInt)
      buf.clear().get(arr)
      arr
    }
  }

  implicit class BSONArrayToByteArray(arr: Array[BsonDocument]) {
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

    def getBson: BsonDocument = {
      i.cTake(i.take(4).toArray.toInt).toArray.toBson
    }

    def getBsonArray: Array[BsonDocument] = {
      def aux(it: Iterator[Byte], acc: List[BsonDocument]): List[BsonDocument] = {
        if (it.isEmpty) acc
        else {
          val bson = it.getBson
          aux(it, bson :: acc)
        }
      }

      aux(i, Nil).toArray.reverse
    }

    def getLongArray(n: Int): Array[Long] = {
      (1 to n).map(_ => i.getLong).toArray
    }

    def getIntArray(n: Int): Array[Int] = {
      (1 to n).map(_ => i.getInt).toArray
    }
  }

}

