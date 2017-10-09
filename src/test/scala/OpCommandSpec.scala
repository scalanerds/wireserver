import akka.util.ByteString
import com.scalanerds.wireserver.wire.opcodes.OpCommand
import com.scalanerds.wireserver.wire.message.traits.Message
import com.scalanerds.wireserver.wire.opcodes.constants.OPCODES
import org.bson.BsonDocument
import org.scalatest.{FlatSpec, Matchers}

class OpCommandSpec extends FlatSpec with Matchers {
  val opCommandByteString = ByteString(88, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, -38, 7, 0, 0, 104, 111, 98, 98, 105,
    101, 115, 0, 108, 105, 115, 116, 67, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 0, 43, 0, 0, 0, 1, 108,
    105, 115, 116, 67, 111, 108, 108, 101, 99, 116, 105, 111, 110, 115, 0, 0, 0, 0, 0, 0, 0, -16, 63, 3, 102, 105,
    108, 116, 101, 114, 0, 5, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0)

  val command: OpCommand = Message(opCommandByteString).asInstanceOf[OpCommand]

  it should "have header" in {
    val header = command.msgHeader
    header.opCode should be(OPCODES.opCommand)
    header.requestId should be(30)
    header.responseTo should be(0)
  }

  it should "have metadata" in {
    val bson = BsonDocument.parse("{ \"listCollections\" : 1.0 , \"filter\" : { }}")
    command.metadata should equal(bson)
  }

  it should "serialize" in {
    command.serialize === opCommandByteString
  }
}
