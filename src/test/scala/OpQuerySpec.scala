import akka.util.ByteString
import com.scalanerds.utils.Utils._
import com.scalanerds.wire.opcodes.OpQuery
import com.scalanerds.wire.{Message, OPCODES}
import org.bson.BSONObject
import org.scalatest.{FlatSpec, Matchers}

class OpQuerySpec extends FlatSpec with Matchers {
  val msgQueryByteString = ByteString(9, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -44, 7, 0, 0, 0, 0, 0, 0, 97, 100, 109,
    105, 110, 46, 36, 99, 109, 100, 0, 0, 0, 0, 0, 1, 0, 0, 0, -30, 0, 0, 0, 16, 105, 115, 77, 97, 115, 116, 101,
    114, 0, 1, 0, 0, 0, 3, 99, 108, 105, 101, 110, 116, 0, -57, 0, 0, 0, 3, 97, 112, 112, 108, 105, 99, 97, 116, 105,
    111, 110, 0, 29, 0, 0, 0, 2, 110, 97, 109, 101, 0, 14, 0, 0, 0, 77, 111, 110, 103, 111, 68, 66, 32, 83, 104, 101,
    108, 108, 0, 0, 3, 100, 114, 105, 118, 101, 114, 0, 58, 0, 0, 0, 2, 110, 97, 109, 101, 0, 24, 0, 0, 0, 77, 111,
    110, 103, 111, 68, 66, 32, 73, 110, 116, 101, 114, 110, 97, 108, 32, 67, 108, 105, 101, 110, 116, 0, 2, 118, 101,
    114, 115, 105, 111, 110, 0, 6, 0, 0, 0, 51, 46, 52, 46, 50, 0, 0, 3, 111, 115, 0, 82, 0, 0, 0, 2, 116, 121, 112,
    101, 0, 6, 0, 0, 0, 76, 105, 110, 117, 120, 0, 2, 110, 97, 109, 101, 0, 7, 0, 0, 0, 85, 98, 117, 110, 116, 117,
    0, 2, 97, 114, 99, 104, 105, 116, 101, 99, 116, 117, 114, 101, 0, 7, 0, 0, 0, 120, 56, 54, 95, 54, 52, 0, 2, 118,
    101, 114, 115, 105, 111, 110, 0, 6, 0, 0, 0, 49, 55, 46, 48, 52, 0, 0, 0, 0)
  val msgQuery: OpQuery = Message(msgQueryByteString).asInstanceOf[OpQuery]

  "msgQuery" should "have header" in {
    val header = msgQuery.msgHeader
    header.opCode should be(OPCODES.opQuery)
    header.requestId should be(0)
    header.responseTo should be(0)
  }

  "msgQuery" should "parse content" in {
    msgQuery.flags.serialize should equal(0.toByteArray)
    msgQuery.fullCollectionName should equal("admin.$cmd")
    msgQuery.numberToSkip should be(0)
    msgQuery.numberToReturn should be(1)
  }

  "msgQuery" should "be BSONObject" in {
    msgQuery.query shouldBe a[BSONObject]
  }

  "msgQuery" should "contain field" in {
    msgQuery.query.get("isMaster") should be(1)
  }

  "msgQuery" should "serialize" in {
    msgQuery.serialize === msgQueryByteString
  }
}
