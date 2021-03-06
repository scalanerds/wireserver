import org.scalatest.{FlatSpec, Matchers}
import com.scalanerds.wireserver.utils.Conversions._
import com.scalanerds.wireserver.wire.opcodes.flags.OpQueryFlags

class OpFlagsSpec extends FlatSpec with Matchers {
  val queryFlags = OpQueryFlags(166)

  "queryFlags" should "serialize" in {
    queryFlags.serialize should equal(166.toByteList)
  }
  "queryFlags" should "match" in {
    queryFlags.tailableCursor should be(true)
    queryFlags.slaveOk  should be(true)
    queryFlags.opLogReply should be (false)
    queryFlags.noCursorTimeOut should be (false)
    queryFlags.awaitData should be(true)
    queryFlags.exhaust should be(false)
    queryFlags.partial should be(true)
  }
}
