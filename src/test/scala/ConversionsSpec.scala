import com.scalanerds.wireserver.utils.Conversions._
import org.bson.BsonDocument
import org.scalatest.{FlatSpec, Matchers}

class ConversionsSpec extends FlatSpec with Matchers {
  "Iterator" should "getIntArray" in {
    val arr = Seq[Byte](7, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0)
    val res = arr.iterator.getIntList(3)
    res should equal(Array(7, 8, 9))
  }
  "Iterator" should "getLongArray" in {
    val arr = Seq[Byte](24, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0)
    val res = arr.iterator.getLongList(2)
    res should equal(List[Long](24, 42))
  }
  "BSON" should "convert" in {
    val bson = BsonDocument.parse("{hey:\"there\"}")
    val byteArr = Seq[Byte](20, 0, 0, 0, 2, 104, 101, 121, 0, 6, 0, 0, 0, 116, 104, 101, 114, 101, 0, 0)
    bson.toByteList should equal(byteArr)
    byteArr.toBson should equal(bson)
  }

  "BSONArray" should "convert" in {
    val bsonArr = List[BsonDocument](
      BsonDocument.parse("{hey:\"there\"}"),
      BsonDocument.parse("{John:\"Doe\"}"),
      BsonDocument.parse("{Jane:\"Doe\"}"))
    val byteArr = Seq[Byte](20, 0, 0, 0, 2, 104, 101, 121, 0, 6, 0, 0, 0, 116, 104, 101, 114, 101, 0, 0, 19, 0,
      0, 0, 2, 74, 111, 104, 110, 0, 4, 0, 0, 0, 68, 111, 101, 0, 0, 19, 0, 0, 0, 2, 74, 97, 110, 101, 0, 4, 0, 0, 0,
      68, 111, 101, 0, 0)

    byteArr.toBson should equal(bsonArr(0))
    bsonArr.toByteList should equal(byteArr)
    byteArr.toBSONList should equal(bsonArr)
  }
}
