import java.time.LocalDateTime

import akka.util.ByteString
import org.scalatest.FlatSpec
import ru.roman3pm.stock.exchange.service.datatypes.ByteData

class ByteDataSpec extends FlatSpec{

  "ByteData apply method" should "correct deserialize ByteString" in {
    val data = Some(ByteData(LocalDateTime.of(2017, 12, 5, 13, 23, 1, 833000000), "GOOG", 93.25, 9000))
    val bs = ByteString(0, 26, 0, 0, 1, 96, 38, 51, 66, -55, 0, 4, 71, 79, 79, 71, 64, 87, 80, 0, 0, 0, 0, 0, 0, 0, 35, 40)

    val bd = ByteData(bs)

    assert(bd.equals(data))
  }
}
