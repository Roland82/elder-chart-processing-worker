package uk.co.elder.app.charting.indicators

import uk.co.elder.{dataSeriesChronoligicalOrdering, DataSeries, ExtendedBigDecimal }
import uk.co.elder.app.charting.slidingApply

trait TrueRange {
  def movingTrueRange(input: DataSeries[TrueRangeComponents]) : DataSeries[BigDecimal] =
    slidingApply(2)(input)(e => trueRange(e(0), e(1))).toList

  private def trueRange(lastQuote: TrueRangeComponents, currentQuote: TrueRangeComponents) : BigDecimal = {
    val options = Seq(
      (currentQuote.high.value - lastQuote.close.value).abs,
      (lastQuote.close.value - currentQuote.low.value).abs,
      currentQuote.high.value.difference(currentQuote.low.value)
    )

    options.max
  }
}
