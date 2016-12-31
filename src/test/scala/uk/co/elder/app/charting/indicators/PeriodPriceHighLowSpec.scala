package uk.co.elder.app.charting.indicators

import uk.co.elder.app.charting.calculators.highLow

import org.joda.time.LocalDate
import org.scalatest.{Matchers, FunSpec}
import uk.co.elder.app.charting.ChartTestUtil._
import uk.co.elder.dataSeriesChronoligicalOrdering

class PeriodPriceHighLowSpec extends FunSpec with Matchers {
  describe("Period Price High Low Indicator (n period week high/low)") {

    val data = dateVsPriceGraph(new LocalDate(2010, 6, 28),
      List(
        BigDecimal(2),
        BigDecimal(2),
        BigDecimal(10),
        BigDecimal(11),
        BigDecimal(10),
        BigDecimal(11.1),
        BigDecimal(5),
        BigDecimal(11.2),
        BigDecimal(2),
        BigDecimal(1.99),
        BigDecimal(2)
      )
    )


    it("return the correct result for a period") {
      val expected = Map(
        new LocalDate(2010, 7, 2) -> 0,
        new LocalDate(2010, 7, 3) -> 1,
        new LocalDate(2010, 7, 4) -> -1,
        new LocalDate(2010, 7, 5) -> 1,
        new LocalDate(2010, 7, 6) -> -1,
        new LocalDate(2010, 7, 7) -> -1,
        new LocalDate(2010, 7, 8) -> 0
      ).toList

      val result = highLow(data, 5)
      result.sorted shouldEqual expected.sorted
    }
  }
}
