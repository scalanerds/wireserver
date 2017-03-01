import com.scalanerds.utils.Utils._
import org.scalatest.{FlatSpec, Matchers}

class UtilsSpec extends FlatSpec with Matchers {
  "Iterator" should "getIntArray" in {
    val arr = Array[Byte](7, 0, 0, 0, 8, 0, 0, 0, 9, 0, 0, 0)
    val res = arr.iterator.getIntArray(3)
    res should equal(Array(7,8,9))
  }
  "Iterator" should "getLongArray" in {
    val arr = Array[Byte](24, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0)
    val res = arr.iterator.getLongArray(2)
    res should equal(Array[Long](24, 42))

  }

}
