package uk.co.elder.app.charting.indicators

import uk.co.elder.dataSeriesChronoligicalOrdering
import uk.co.elder.app.charting.calculators.relativeStrengthIndex
import org.joda.time.LocalDate
import org.scalatest.{FunSpec, Matchers}

class RelativeStrengthIndexSpec extends FunSpec with Matchers {

  describe("Relative Strength Index Calculator") {
    val startDate = new LocalDate("2014-11-01")

    val data = Map(
      new LocalDate(2009,12, 14) -> BigDecimal(44.3389),
      new LocalDate(2009,12, 15) -> BigDecimal(44.0902),
      new LocalDate(2009,12, 16) -> BigDecimal(44.1497),
      new LocalDate(2009,12, 17) -> BigDecimal(43.6124),
      new LocalDate(2009,12, 18) -> BigDecimal(44.3278),
      new LocalDate(2009,12, 21) -> BigDecimal(44.8264),
      new LocalDate(2009,12, 22) -> BigDecimal(45.0955),
      new LocalDate(2009,12, 23) -> BigDecimal(45.4245),
      new LocalDate(2009,12, 24) -> BigDecimal(45.8433),
      new LocalDate(2009,12, 28) -> BigDecimal(46.0826),
      new LocalDate(2009,12, 29) -> BigDecimal(45.8931),
      new LocalDate(2009,12, 30) -> BigDecimal(46.0328),
      new LocalDate(2009,12, 31) -> BigDecimal(45.6140),
      new LocalDate(2010,1, 4) -> BigDecimal(46.2820),
      new LocalDate(2010,1, 5) -> BigDecimal(46.2820),
      new LocalDate(2010,1, 6) -> BigDecimal(46.0028),
      new LocalDate(2010,1, 7) -> BigDecimal(46.0328),
      new LocalDate(2010,1, 8) -> BigDecimal(46.4116)
    ).toList.reverse

    val expectedResult = Map(
      new LocalDate(2010,1, 5) -> BigDecimal(70.54),
      new LocalDate(2010,1, 6) -> BigDecimal(66.32),
      new LocalDate(2010,1, 7) -> BigDecimal(66.56),
      new LocalDate(2010,1, 8) -> BigDecimal(69.41)
    ).toList

    it("returns the correct result") {
      val result = relativeStrengthIndex(data, 14)
      result.sorted shouldEqual expectedResult.sorted
    }

    it("handles a set of data thats too small to calculate") {
      val result = relativeStrengthIndex(data.take(13), 14)
      result shouldEqual List()
    }

    it("handles starts calculating at the right point") {
      val result = relativeStrengthIndex(data.toList.sorted.take(15), 14)
      result.sorted shouldEqual List((new LocalDate(2010,1, 5), BigDecimal(70.54)))
    }
  }
}
