package uk.co.elder.app.charting

import uk.co.elder.app.model.{Close, High, Low}
import uk.co.elder.app.charting.calculators._
import uk.co.elder.app.charting.indicators.{DirectionalIndicatorResult, DirectionalMovementComponents}
import uk.co.elder.dataSeriesChronoligicalOrdering
import org.joda.time.LocalDate
import org.scalatest.{FunSpec, Matchers}

import scalaz.std.AllInstances.intInstance

class TimeSeriesCalculatorsSpec extends FunSpec with Matchers {
  describe("Directional Movement") {
    val testValues = Map(
      new LocalDate(2009, 2, 11) -> DirectionalMovementComponents(High(30.1983), Low(29.4072), Close(29.8720)),
      new LocalDate(2009, 2, 12) -> DirectionalMovementComponents(High(30.2776), Low(29.3182), Close(30.2381)),
      new LocalDate(2009, 2, 13) -> DirectionalMovementComponents(High(30.4458), Low(29.9611), Close(30.0996)),
      new LocalDate(2009, 2, 17) -> DirectionalMovementComponents(High(29.3478), Low(28.7443), Close(28.9028)),
      new LocalDate(2009, 2, 18) -> DirectionalMovementComponents(High(29.3477), Low(28.5566), Close(28.9225)),
      new LocalDate(2009, 2, 19) -> DirectionalMovementComponents(High(29.2886), Low(28.4081), Close(28.4775)),
      new LocalDate(2009, 2, 20) -> DirectionalMovementComponents(High(28.8334), Low(28.0818), Close(28.5566)),
      new LocalDate(2009, 2, 23) -> DirectionalMovementComponents(High(28.7346), Low(27.4289), Close(27.5576)),
      new LocalDate(2009, 2, 24) -> DirectionalMovementComponents(High(28.6654), Low(27.6565), Close(28.4675)),
      new LocalDate(2009, 2, 25) -> DirectionalMovementComponents(High(28.8532), Low(27.8345), Close(28.2796)),
      new LocalDate(2009, 2, 26) -> DirectionalMovementComponents(High(28.6356), Low(27.3992), Close(27.4882)),
      new LocalDate(2009, 2, 27) -> DirectionalMovementComponents(High(27.6761), Low(27.0927), Close(27.2310)),
      new LocalDate(2009, 3, 2) -> DirectionalMovementComponents (High(27.2112), Low(26.1826), Close(26.3507)),
      new LocalDate(2009, 3, 3) -> DirectionalMovementComponents (High(26.8651), Low(26.1332), Close(26.3309)),
      new LocalDate(2009, 3, 4) -> DirectionalMovementComponents (High(27.4090), Low(26.6277), Close(27.0333)),
      new LocalDate(2009, 3, 5) -> DirectionalMovementComponents (High(26.9441), Low(26.1332), Close(26.2221)),
      new LocalDate(2009, 3, 6) -> DirectionalMovementComponents (High(26.5189), Low(25.4307), Close(26.0144)),
      new LocalDate(2009, 3, 9) -> DirectionalMovementComponents (High(26.5189), Low(25.3518), Close(25.4605)),
      new LocalDate(2009, 3, 10) -> DirectionalMovementComponents(High(27.0927), Low(25.8760), Close(27.0333)),
      new LocalDate(2009, 3, 11) -> DirectionalMovementComponents(High(27.6860), Low(26.9640), Close(27.4487)),
      new LocalDate(2009, 3, 12) -> DirectionalMovementComponents(High(28.4477), Low(27.1421), Close(28.3586)),
      new LocalDate(2009, 3, 13) -> DirectionalMovementComponents(High(28.5267), Low(28.0123), Close(28.4278)),
      new LocalDate(2009, 3, 16) -> DirectionalMovementComponents(High(28.6654), Low(27.8840), Close(27.9530)),
      new LocalDate(2009, 3, 17) -> DirectionalMovementComponents(High(29.0116), Low(27.9928), Close(29.0116)),
      new LocalDate(2009, 3, 18) -> DirectionalMovementComponents(High(29.8720), Low(28.7643), Close(29.3776)),
      new LocalDate(2009, 3, 19) -> DirectionalMovementComponents(High(29.8028), Low(29.1402), Close(29.3576)),
      new LocalDate(2009, 3, 20) -> DirectionalMovementComponents(High(29.7529), Low(28.7127), Close(28.9107)),
      new LocalDate(2009, 3, 23) -> DirectionalMovementComponents(High(30.6546), Low(28.9290), Close(30.6149)),
      new LocalDate(2009, 3, 24) -> DirectionalMovementComponents(High(30.5951), Low(30.0304), Close(30.0502)),
      new LocalDate(2009, 3, 25) -> DirectionalMovementComponents(High(30.7635), Low(29.3863), Close(30.1890))
    ).toList.reverse

    val expectedResult = Map(
      new LocalDate(2009, 3, 23) -> DirectionalIndicatorResult(23.8153,	18.1258, 33.5833),
      new LocalDate(2009, 3, 24) -> DirectionalIndicatorResult(22.8135,	17.3633, 32.1535),
      new LocalDate(2009, 3, 25) -> DirectionalIndicatorResult(20.6132,	20.1994, 29.9292)
    ).toList

    it("full DI, and ADX calculation should work properly") {
      val result = directionalMovement(testValues)
      result shouldEqual expectedResult.sorted
    }
  }

  describe("Apply over sliding window") {
    it("should do its thing properly") {

      val m = Map(
        new LocalDate(2010,2, 1) -> 1,
        new LocalDate(2010,2, 2) -> 2,
        new LocalDate(2010,2, 3) -> 3,
        new LocalDate(2010,2, 4) -> 4,
        new LocalDate(2010,2, 5) -> 5,
        new LocalDate(2010,2, 6) -> 6,
        new LocalDate(2010,2, 7) -> 7
      ).toList.reverse

      val expectedResult = Map(
        new LocalDate(2010,2, 3) -> 6,
        new LocalDate(2010,2, 4) -> 9,
        new LocalDate(2010,2, 5) -> 12,
        new LocalDate(2010,2, 6) -> 15,
        new LocalDate(2010,2, 7)-> 18
      ).toList

      val result = slidingFold(3)(m)
      result.sorted shouldEqual expectedResult.sorted
    }
  }
}