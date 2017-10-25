package com.scalanerds.wireserver.utils

import java.nio.ByteBuffer

import akka.util.ByteString
import org.bson.{BsonDocument, RawBsonDocument, codecs}

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.Try


object Conversions {

  implicit def byte2bool(b: Byte): Boolean = b.toInt != 0

  implicit def bool2byte(b: Boolean): Byte = (if (b) 1 else 0).toByte

  /** implicit conversions for Any */
  implicit class AnyUtils(a: Any) {
    def asInstanceOfOption[T: ClassTag]: Option[T] =
      Some(a) collect { case m: T => m }
  }

  implicit class IntToArray(value: Int) {
    def toByteArray: Array[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.reverse
    }
  }

  implicit class IntToList(value: Int) {
    def toByteList: Seq[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.toSeq.reverse
    }

    def toBooleanList: List[Boolean] = {
      // get the first 8 bits which are required for the flags
      (0 until 8).map(i => ((value >> i) & 1) != 0).toList
    }
  }

  implicit class LongToByteList(value: Long) {
    def toByteList: Seq[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(8).putLong(value).array.toSeq.reverse
    }
  }

  implicit class IntToByte(list: List[Int]) {
    def toByteList: Seq[Byte] = {
      list.map(_.toByteList).reduce(_ ++ _)
    }
  }

  implicit class ByteList(list: Seq[Byte]) {
    def toInt: Int = {
      BigInt(list.reverse.toArray).toInt
    }

    def toLong: Long = {
      BigInt(list.reverse.toArray).toLong
    }

    def toUTFString: String = {
      new String(list.toArray, "UTF-8")
    }

    def toBson: BsonDocument = {
      new RawBsonDocument(list.toArray).asDocument()
    }

    def toBSONList: List[BsonDocument] = {
      list.iterator.getBsonList
    }

    def binaryToInt: Int = {
      list.zipWithIndex.map(p => p._1 << p._2).sum
    }
  }

  implicit class BSONToByteList(bson: BsonDocument) {
    def toByteList: Seq[Byte] = {
      val buf = new RawBsonDocument(bson, new codecs.BsonDocumentCodec)
        .getByteBuffer
      val arr = new Array[Byte](buf.getInt)
      buf.clear().get(arr)
      arr.toSeq
    }
  }

  implicit class BSONListToByteList(arr: List[BsonDocument]) {
    def toByteList: Seq[Byte] = {
      arr.foldLeft(Seq[Byte]())(_ ++ _.toByteList)
    }
  }


  implicit class StringToList(s: String) {
    def toByteList: Seq[Byte] = {
      ByteString.fromString(s) ++ Seq[Byte](0)
    }
  }

  implicit class ByteIterator(i: Iterator[Byte]) {
    def getString: String = {
      i.takeWhile(_ != 0).toSeq.toUTFString
    }

    def getInt: Int = {
      i.take(4).toSeq.toInt
    }

    def getIntOption: Option[Int] = {
      Try(i.getInt).toOption
    }

    def getLong: Long = {
      i.take(8).toSeq.toLong
    }

    def getBson: BsonDocument = {
      val length = i.take(4).toSeq
      (length ++ i.take(length.toInt - 4)).toBson
    }

    def getBsonList: List[BsonDocument] = {
      def aux(it: Iterator[Byte], acc: List[BsonDocument]): List[BsonDocument] = {
        if (it.isEmpty) acc
        else {
          val bson = it.getBson
          aux(it, bson :: acc)
        }
      }

      aux(i, Nil).reverse
    }

    def getLongList(n: Int): List[Long] = {
      (1 to n).map(_ => i.getLong).toList
    }

    def getIntList(n: Int): List[Int] = {
      (1 to n).map(_ => i.getInt).toList
    }
  }

}

