import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.opcodes.OpReply
import com.scalanerds.wire.{Message, OpCodes}
import org.bson.BSONObject
import org.scalatest.{FlatSpec, Matchers}

class OpReplySpec extends FlatSpec with Matchers {
  val opReplyBs = ByteString(-52, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 1, 0, 0, 0, -88, 0, 0, 0, 8, 105, 115, 109, 97, 115, 116, 101, 114, 0, 1, 16, 108, 111, 99, 97, 108, 84,
    105, 109, 101, 0, -96, 1, 13, 88, 16, 109, 97, 120, 66, 115, 111, 110, 79, 98, 106, 101, 99, 116, 83, 105, 122,
    101, 0, 0, 0, 0, 1, 16, 109, 97, 120, 77, 101, 115, 115, 97, 103, 101, 83, 105, 122, 101, 66, 121, 116, 101, 115,
    0, 0, 108, -36, 2, 16, 109, 97, 120, 87, 105, 114, 101, 86, 101, 114, 115, 105, 111, 110, 0, 3, 0, 0, 0, 16, 109,
    97, 120, 87, 114, 105, 116, 101, 66, 97, 116, 99, 104, 83, 105, 122, 101, 0, -24, 3, 0, 0, 16, 109, 105, 110, 87,
    105, 114, 101, 86, 101, 114, 115, 105, 111, 110, 0, 0, 0, 0, 0, 2, 109, 115, 103, 0, 9, 0, 0, 0, 105, 115, 100,
    98, 103, 114, 105, 100, 0, 16, 111, 107, 0, 1, 0, 0, 0, 0)
  val msgReply: OpReply = Message(opReplyBs).asInstanceOf[OpReply]

  "msgQuery" should "have header" in {
    val header = msgReply.msgHeader
    header.opCode should be(OpCodes.opReply)
    header.requestId should be(0)
    header.responseTo should be(0)
  }

  "msgQuery" should "parse content" in {
    msgReply.responseFlags should be(8)
    msgReply.cursorId should be(0)
    msgReply.startingFrom should be(0)
    msgReply.numberReturned should be(1)
  }

  "msgQuery" should "be BSONObject" in {
    msgReply.documents(0) shouldBe a[BSONObject]
  }

  "msgQuery" should "contain field" in {
    val document = Array[Byte](-88, 0, 0, 0, 8, 105, 115, 109, 97, 115, 116, 101, 114, 0, 1, 16, 108, 111, 99, 97,
      108, 84, 105, 109, 101, 0, -96, 1, 13, 88, 16, 109, 97, 120, 66, 115, 111, 110, 79, 98, 106, 101, 99, 116, 83,
      105, 122, 101, 0, 0, 0, 0, 1, 16, 109, 97, 120, 77, 101, 115, 115, 97, 103, 101, 83, 105, 122, 101, 66, 121,
      116, 101, 115, 0, 0, 108, -36, 2, 16, 109, 97, 120, 87, 105, 114, 101, 86, 101, 114, 115, 105, 111, 110, 0, 3,
      0, 0, 0, 16, 109, 97, 120, 87, 114, 105, 116, 101, 66, 97, 116, 99, 104, 83, 105, 122, 101, 0, -24, 3, 0, 0,
      16, 109, 105, 110, 87, 105, 114, 101, 86, 101, 114, 115, 105, 111, 110, 0, 0, 0, 0, 0, 2, 109, 115, 103, 0, 9,
      0, 0, 0, 105, 115, 100, 98, 103, 114, 105, 100, 0, 16, 111, 107, 0, 1, 0, 0, 0, 0)
    msgReply.documents.toByteArray should equal(document)
  }

  "msgQuery" should "serialize" in {
    msgReply.serialize === opReplyBs
  }
}