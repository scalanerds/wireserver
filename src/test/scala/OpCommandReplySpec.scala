import akka.util.ByteString
import com.scalanerds.wireserver.wire.opcodes.OpCommandReply
import com.scalanerds.wireserver.wire.message.traits.Message
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson.BsonDocument
import org.scalatest.{FlatSpec, Matchers}

class OpCommandReplySpec extends FlatSpec with Matchers {
  val opCommandByteString = ByteString(-66, 0, 0, 0, 97, 0, 0, 0, 19, 0, 0, 0, -37, 7, 0, 0, -87, 0, 0, 0, 8, 105,
    115, 109, 97, 115, 116, 101, 114, 0, 1, 16, 109, 97, 120, 66, 115, 111, 110, 79, 98, 106, 101, 99, 116, 83, 105,
    122, 101, 0, 0, 0, 0, 1, 16, 109, 97, 120, 77, 101, 115, 115, 97, 103, 101, 83, 105, 122, 101, 66, 121, 116, 101,
    115, 0, 0, 108, -36, 2, 16, 109, 97, 120, 87, 114, 105, 116, 101, 66, 97, 116, 99, 104, 83, 105, 122, 101, 0,
    -24, 3, 0, 0, 9, 108, 111, 99, 97, 108, 84, 105, 109, 101, 0, 118, -50, -71, -93, 90, 1, 0, 0, 16, 109, 97, 120,
    87, 105, 114, 101, 86, 101, 114, 115, 105, 111, 110, 0, 5, 0, 0, 0, 16, 109, 105, 110, 87, 105, 114, 101, 86,
    101, 114, 115, 105, 111, 110, 0, 0, 0, 0, 0, 8, 114, 101, 97, 100, 79, 110, 108, 121, 0, 0, 1, 111, 107, 0, 0, 0,
    0, 0, 0, 0, -16, 63, 0, 5, 0, 0, 0, 0)
  val reply: OpCommandReply = Message(opCommandByteString).asInstanceOf[OpCommandReply]

  it should "have header" in {
    val header = reply.msgHeader
    header.opCode should be(OPCODES.opCommandReply)
    header.requestId should be(97)
    header.responseTo should be(19)
  }

  it should "parse content" in {
    val bson = BsonDocument.parse("{ \"ismaster\" : true , \"maxBsonObjectSize\" : 16777216 , \"maxMessageSizeBytes\" : " +
      "48000000 , \"maxWriteBatchSize\" : 1000 , \"localTime\" : { \"$date\" : 1488805547638} , \"maxWireVersion\" : " +
      "5 , \"minWireVersion\" : 0 , \"readOnly\" : false , \"ok\" : 1.0}")
    reply.metadata should equal(bson)

  }

  it should "serialize" in {
    reply.serialize === opCommandByteString
  }
}
