import com.scalanerds.wire.opcodes.OpQueryFlags
import org.scalatest.{FlatSpec, Matchers}
import com.scalanerds.utils.Utils._

class OpFlagsSpec extends FlatSpec with Matchers {
  val queryFlags = OpQueryFlags(166)

  "queryFlags" should "serialize" in {
    queryFlags.serialize should equal(166.toByteArray)
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
