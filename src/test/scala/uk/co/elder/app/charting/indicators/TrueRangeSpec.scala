package uk.co.elder.app.charting.indicators

import uk.co.elder.app.model.{Close, High, Low}
import uk.co.elder.app.charting.ChartTestUtil._
import uk.co.elder.app.charting.calculators._
import org.joda.time.LocalDate
import org.scalatest.{FunSpec, Matchers}
import uk.co.elder.dataSeriesChronoligicalOrdering

class TrueRangeSpec extends FunSpec with Matchers {
  describe("True Range Calculator") {
    val startDate = new LocalDate("2014-11-01")

    val data  =
      Map(
        startDate ->             TrueRangeComponents(Close(44.52), High(44.53), Low(43.98)),
        startDate.plusDays(1) -> TrueRangeComponents(Close(44.65), High(44.93), Low(44.36)),
        startDate.plusDays(2) -> TrueRangeComponents(Close(45.22), High(45.39), Low(44.70)),
        startDate.plusDays(3) -> TrueRangeComponents(Close(45.45), High(45.70), Low(45.13)),
        startDate.plusDays(4) -> TrueRangeComponents(Close(45.49), High(45.63), Low(44.89))
      ).toList.reverse


    it("return the correct true ranges for a period") {
      val result = movingTrueRange(data)
      val expectedResult = dateVsPriceGraph(startDate.plusDays(1), Seq(0.57, 0.74, 0.57, 0.74))
      result.sorted shouldEqual expectedResult.sorted
    }
  }
}
