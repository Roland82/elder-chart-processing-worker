package uk.co.elder.app.charting

import java.math.{MathContext, RoundingMode}

import uk.co.elder.app.charting.ChartTestUtil.dateVsPriceGraph
import uk.co.elder.app.charting.Smoothing._
import uk.co.elder.app.charting.indicators.DailyChange
import org.joda.time.LocalDate
import org.scalatest.{FunSpec, Matchers}
import uk.co.elder.dataSeriesChronoligicalOrdering
import scalaz.Scalaz.bigDecimalInstance

class SmoothingSpec extends FunSpec with Matchers {
  describe("Generic Moving Average statistic calculator") {

    val input = Map(
      new LocalDate(2010, 7, 12) -> DailyChange(1, -1),
      new LocalDate(2010, 7, 13) -> DailyChange(2, -2),
      new LocalDate(2010, 7, 14) -> DailyChange(3, -3),
      new LocalDate(2010, 7, 15) -> DailyChange(4, -4),
      new LocalDate(2010, 7, 16) -> DailyChange(5, -5),
      new LocalDate(2010, 7, 19) -> DailyChange(0, 0),
      new LocalDate(2010, 7, 20) -> DailyChange(6, -6),
      new LocalDate(2010, 7, 21) -> DailyChange(7, -7),
      new LocalDate(2010, 7, 22) -> DailyChange(8, -8),
      new LocalDate(2010, 7, 23) -> DailyChange(9, -9)
    ).toList.reverse

    val expectedResult = Map(
      new LocalDate(2010, 7, 16) -> DailyChange(3, -3),
      new LocalDate(2010, 7, 19) -> DailyChange(2.8, -2.8),
      new LocalDate(2010, 7, 20) -> DailyChange(3.6, -3.6),
      new LocalDate(2010, 7, 21) -> DailyChange(4.4, -4.4),
      new LocalDate(2010, 7, 22) -> DailyChange(5.2, -5.2),
      new LocalDate(2010, 7, 23) -> DailyChange(6, -6)
    ).toList

    it("return correct result") {
      val result = simpleMovingAverage(input, 5)
      result.sorted shouldEqual expectedResult.sorted
    }
  }

  describe("Moving Average statistic calculator") {
    it("return correct result") {
      val input = dateVsPriceGraph(new LocalDate("2014-11-01"), Seq(11, 12, 13, 14, 15, 16, 100, 90, 80))
      val expectedResult = dateVsPriceGraph(new LocalDate("2014-11-05"), Seq(13, 14, 31.6, 47, 60.2))
      val result = simpleMovingAverage(input, 5)
      result.sorted shouldEqual expectedResult.sorted
    }

    it("sorts data correctly") {
      val input = Map[LocalDate, BigDecimal](
        new LocalDate("2014-11-04") -> 14,
        new LocalDate("2014-11-08") -> 90,
        new LocalDate("2014-11-05") -> 15,
        new LocalDate("2014-11-01") -> 11,
        new LocalDate("2014-11-06") -> 16,
        new LocalDate("2014-11-07") -> 100,
        new LocalDate("2014-11-02") -> 12,
        new LocalDate("2014-11-09") -> 80,
        new LocalDate("2014-11-03") -> 13
      ).toList.reverse

      val expectedResult = dateVsPriceGraph(new LocalDate("2014-11-05"), Seq(13, 14, 31.6, 47, 60.2))
      val result = simpleMovingAverage(input, 5)
      result.sorted shouldEqual expectedResult.sorted
    }

    it("be able to count just the one date") {
      val input = dateVsPriceGraph(new LocalDate("2014-11-01"), Seq(11, 12, 13, 14, 15))
      val expectedResult = dateVsPriceGraph(new LocalDate("2014-11-05"), Seq(13))
      val result = simpleMovingAverage(input, 5)
      result.sorted shouldEqual expectedResult.sorted
    }

    it("return an empty map if there is not enough data to process for the average") {
      val input = dateVsPriceGraph(new LocalDate("2014-11-01"), Seq(11, 12, 13, 14, 15))
      val result = simpleMovingAverage(input, 10)
      result.sorted shouldEqual List.empty
    }

    it("be able to handle gaps in dates for pricing data correctly") {
      val inputWeek1 = dateVsPriceGraph(new LocalDate("2014-11-01"), Seq(11, 12, 13, 14, 15))
      val inputWeek2 = dateVsPriceGraph(new LocalDate("2014-11-08"), Seq(16, 17))

      val expectedResultWeek1 = dateVsPriceGraph(new LocalDate("2014-11-05"), Seq(13))
      val expectedResultWeek2 = dateVsPriceGraph(new LocalDate("2014-11-08"), Seq(14, 15))

      val result = simpleMovingAverage(inputWeek1 ++ inputWeek2, 5)
      result.sorted shouldEqual (expectedResultWeek1 ++ expectedResultWeek2).sorted
    }
  }

  describe("Wilder moving average") {
    val input = Map(
      new LocalDate(2010, 7, 12) -> BigDecimal(0.56870),
      new LocalDate(2010, 7, 13) -> BigDecimal(0.7483),
      new LocalDate(2010, 7, 14) -> BigDecimal(0.5687),
      new LocalDate(2010, 7, 15) -> BigDecimal(0.7482),
      new LocalDate(2010, 7, 16) -> BigDecimal(1.3250),
      new LocalDate(2010, 7, 19) -> BigDecimal(0.7084),
      new LocalDate(2010, 7, 20) -> BigDecimal(1.3967),
      new LocalDate(2010, 7, 21) -> BigDecimal(1.1972),
      new LocalDate(2010, 7, 22) -> BigDecimal(1.3369),
      new LocalDate(2010, 7, 23) -> BigDecimal(0.7183),
      new LocalDate(2010, 7, 26) -> BigDecimal(0.5487),
      new LocalDate(2010, 7, 27) -> BigDecimal(0.5088),
      new LocalDate(2010, 7, 28) -> BigDecimal(0.6984),
      new LocalDate(2010, 7, 29) -> BigDecimal(1.1573),
      new LocalDate(2010, 7, 30) -> BigDecimal(1.0176),
      new LocalDate(2010, 8, 2) -> BigDecimal(0.9777),
      new LocalDate(2010, 8, 3) -> BigDecimal(0.4490),
      new LocalDate(2010, 8, 4) -> BigDecimal(0.5188),
      new LocalDate(2010, 8, 5) -> BigDecimal(0.4170),
      new LocalDate(2010, 8, 6) -> BigDecimal(0.7981),
      new LocalDate(2010, 8, 9) -> BigDecimal(0.4290),
      new LocalDate(2010, 8, 10) -> BigDecimal(0.7981),
      new LocalDate(2010, 8, 11) -> BigDecimal(1.4591),
      new LocalDate(2010, 8, 12) -> BigDecimal(0.9478),
      new LocalDate(2010, 8, 13) -> BigDecimal(0.3492),
      new LocalDate(2010, 8, 16) -> BigDecimal(0.8081),
      new LocalDate(2010, 8, 17) -> BigDecimal(0.9777),
      new LocalDate(2010, 8, 18) -> BigDecimal(0.7083),
      new LocalDate(2010, 8, 19) -> BigDecimal(0.9877)
    ).toList.reverse

    it("returns an empty map when the period exceeds the available data to process") {
      val result = wilderMovingAverage(input, 200)
      result shouldEqual List.empty
    }

    it("should calculate a 14 day period properly") {
      val expectedResult = Map(
        new LocalDate(2010, 7, 29) -> BigDecimal(12.23),
        new LocalDate(2010, 7, 30) -> BigDecimal(12.37),
        new LocalDate(2010, 8, 2) -> BigDecimal (12.47),
        new LocalDate(2010, 8, 3) -> BigDecimal (12.03),
        new LocalDate(2010, 8, 4) -> BigDecimal (11.69),
        new LocalDate(2010, 8, 5) -> BigDecimal (11.27),
        new LocalDate(2010, 8, 6) -> BigDecimal (11.26),
        new LocalDate(2010, 8, 9) -> BigDecimal (10.89),
        new LocalDate(2010, 8, 10) -> BigDecimal(10.91),
        new LocalDate(2010, 8, 11) -> BigDecimal(11.59),
        new LocalDate(2010, 8, 12) -> BigDecimal(11.71),
        new LocalDate(2010, 8, 13) -> BigDecimal(11.22),
        new LocalDate(2010, 8, 16) -> BigDecimal(11.23),
        new LocalDate(2010, 8, 17) -> BigDecimal(11.40),
        new LocalDate(2010, 8, 18) -> BigDecimal(11.30),
        new LocalDate(2010, 8, 19) -> BigDecimal(11.48)
      ).toList

      val result = wilderMovingAverage(input, 14)
      result.map(e=> (e._1, e._2.round(new MathContext(4, RoundingMode.HALF_UP)))).sorted shouldEqual expectedResult.sorted

    }
  }

  describe("Exponential Moving Average") {

    val input = dateVsPriceGraph(new LocalDate("2010-03-01"),
      Seq(22.27, 22.19, 22.08, 22.17, 22.18, 22.13, 22.23, 22.43, 22.24, 22.29, 22.15, 22.39, 22.38, 22.61, 23.36, 24.05)
    )

    it("return the correct true ranges for a period") {
      val expectedResult = dateVsPriceGraph(new LocalDate("2010-03-10"), Seq(22.22, 22.21, 22.24, 22.27, 22.33, 22.52, 22.80))
      val result = exponentialMovingAverage(input, 10)
      result.sorted shouldEqual expectedResult.sorted
    }
  }
}
