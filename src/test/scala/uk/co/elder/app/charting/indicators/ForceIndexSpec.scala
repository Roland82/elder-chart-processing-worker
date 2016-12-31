package uk.co.elder.app.charting.indicators

import uk.co.elder.app.model.{Close, Volume}
import uk.co.elder.app.charting.calculators._
import org.joda.time.LocalDate
import org.scalatest.{FunSpec, Matchers}
import uk.co.elder.dataSeriesChronoligicalOrdering

class ForceIndexSpec extends FunSpec with Matchers {
  describe("Force index calculator") {
    val values = Map(
      new LocalDate(2010, 6, 25) -> ForceIndexComponents(Close(14.33), Volume(40000)),
      new LocalDate(2010, 6, 28) -> ForceIndexComponents(Close(14.23), Volume(45579)),
      new LocalDate(2010, 6, 29) -> ForceIndexComponents(Close(13.98), Volume(66285)),
      new LocalDate(2010, 6, 30) -> ForceIndexComponents(Close(13.96), Volume(51761)),
      new LocalDate(2010, 7, 1) -> ForceIndexComponents(Close(13.93), Volume(69341)),
      new LocalDate(2010, 7, 2) -> ForceIndexComponents(Close(13.84), Volume(41631)),
      new LocalDate(2010, 7, 6) -> ForceIndexComponents(Close(13.99), Volume(73499))
    ).toList.reverse


    it("return the correct result for a period") {
      val expected = Map(
        new LocalDate(2010, 6, 28) -> -4557.90,
        new LocalDate(2010, 6, 29) -> -16571.25,
        new LocalDate(2010, 6, 30) -> -1035.22,
        new LocalDate(2010, 7, 1) -> -2080.23,
        new LocalDate(2010, 7, 2) -> -3746.79,
        new LocalDate(2010, 7, 6) -> 11024.85
      ).toList

      val result = forceIndex(values)
      result.sorted shouldEqual expected.sorted
    }
  }
}
