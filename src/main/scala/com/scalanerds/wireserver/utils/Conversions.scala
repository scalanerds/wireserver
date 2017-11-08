package com.scalanerds.wireserver.utils

import java.nio.ByteBuffer

import akka.util.ByteString
import org.bson.{BsonDocument, RawBsonDocument, codecs}

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.Try

object Conversions {

  /** implicit conversions for Byte to Boolean */
  implicit def byte2bool(b: Byte): Boolean = b.toInt != 0
  /** implicit conversions for Boolean to Byte */
  implicit def bool2byte(b: Boolean): Byte = (if (b) 1 else 0).toByte

  /** implicit conversions for Any */
  implicit class RichAny(a: Any) {
    def asInstanceOfOption[T: ClassTag]: Option[T] =
      Some(a) collect { case m: T => m }
  }

  implicit class RichBoolean(val b: Boolean) extends AnyVal {
    /**
      * turn a boolean in to an option that if true wraps the expression passed
      * as argument in Some else return None
      *
      *{{{
      * scala> (42 > 5).option("42 is greater")
      * res0: Option[String] = Some(42 is greater)
      *
      * scala> (42 < 5).option("42 is greater")
      * res1: Option[String] = None
      *}}}
      *
      */
    def option[T](f: => T): Option[T] = if (b) Some(f) else None
  }

  /** Implicit conversions for Int */
  implicit class RichInt(value: Int) {

    /** convert an integer to a list of bytes */
    def toByteList: Seq[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.toSeq.reverse
    }

    /** convert an integer to a list of booleans */
    def toBooleanList: List[Boolean] = {
      // get the first 8 bits which are required for the flags
      (0 until 8).map(i => ((value >> i) & 1) != 0).toList
    }

    /** convert an integer to an array of bytes */
    def toByteArray: Array[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(4).putInt(value).array.reverse
    }
  }

  /** Conversions for Long */
  implicit class RichLong(value: Long) {
    def toByteList: Seq[Byte] = {
      // LITTLE_ENDIAN
      ByteBuffer.allocate(8).putLong(value).array.toSeq.reverse
    }
  }

  /** Conversions for Int List */
  implicit class RichIntList(list: List[Int]) {
    def toByteList: Seq[Byte] = {
      list.map(_.toByteList).reduce(_ ++ _)
    }
  }

  /** Conversions for Byte Sequence */
  implicit class RichByteList(list: Seq[Byte]) {
    def toInt: Int = {
      BigInt(list.reverse.toArray).toInt
    }
    /** convert a list of bytes to a long */
    def toLong: Long = {
      BigInt(list.reverse.toArray).toLong
    }

    /** convert a list of bytes to a string */
    def toUTFString: String = {
      new String(list.toArray, "UTF-8")
    }

    /** convert a list of bytes to a BsonDocument */
    def toBson: BsonDocument = {
      new RawBsonDocument(list.toArray).asDocument()
    }

    /** convert a list of bytes to a list of BsonDocument */
    def toBSONList: List[BsonDocument] = {
      list.iterator.getBsonList
    }

    /** convert a list of bytes to int */
    def binaryToInt: Int = {
      list.zipWithIndex.map(p => p._1 << p._2).sum
    }
  }

 /** Conversion for BsonDocument */
  implicit class RichBson(bson: BsonDocument) {
    def toByteList: Seq[Byte] = {
      val buf = new RawBsonDocument(bson, new codecs.BsonDocumentCodec)
        .getByteBuffer
      val arr = new Array[Byte](buf.getInt)
      buf.clear().get(arr)
      arr.toSeq
    }
  }

  /** Conversion for List of BsonDocument */
  implicit class RichBsonList(arr: List[BsonDocument]) {
    def toByteList: Seq[Byte] = {
      arr.foldLeft(Seq[Byte]())(_ ++ _.toByteList)
    }
  }


  /** Conversion for String */
  implicit class RichString(s: String) {
    def toByteList: Seq[Byte] = {
      ByteString.fromString(s) ++ Seq[Byte](0)
    }
  }

  /** Conversion for Byte Iterator */
  implicit class RichByteIterator(i: Iterator[Byte]) {
    def getString: String = {
      i.takeWhile(_ != 0).toSeq.toUTFString
    }

    /** read a string from a byte iterator */
    def getStringOption: Option[String] = {
      Try(i.getString).toOption
    }
    /** read an int from a byte iterator */
    def getInt: Int = {
      i.take(4).toSeq.toInt
    }

    /** read an int from a byte iterator */
    def getIntOption: Option[Int] = {
      Try(i.getInt).toOption
    }

    /** read a long from a byte iterator */
    def getLong: Long = {
      i.take(8).toSeq.toLong
    }

    /** read a long from a byte iterator */
    def getLongOption: Option[Long] = {
      Try(i.getLong).toOption
    }

    /** read a BsonDocument from a byte iterator */
    def getBson: BsonDocument = {
      val length = i.take(4).toSeq
      (length ++ i.take(length.toInt - 4)).toBson
    }

    /** read a BsonDocument from a byte iterator */
    def getBsonOption: Option[BsonDocument] = {
      Try(i.getBson).toOption
    }

    /** read a list of BsonDocument from a byte iterator */
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

    /** read a list of BsonDocument from a byte iterator */
    def getBsonListOption: Option[List[BsonDocument]] = {
      Try(i.getBsonList).toOption
    }

    /** read a list of long from a byte iterator */
    def getLongList(n: Int): List[Long] = {
      (1 to n).map(_ => i.getLong).toList
    }

    /** read a list of long from a byte iterator */
    def getLongListOption(n: Int): Option[List[Long]] = {
      Try(i.getLongList(n)).toOption
    }

    /** read a list of int from a byte iterator */
    def getIntList(n: Int): List[Int] = {
      (1 to n).map(_ => i.getInt).toList
    }

    /** read a list of int from a byte iterator */
    def getIntListOption(n: Int): Option[List[Int]] = {
      Try(i.getIntList(n)).toOption
    }
  }
}
