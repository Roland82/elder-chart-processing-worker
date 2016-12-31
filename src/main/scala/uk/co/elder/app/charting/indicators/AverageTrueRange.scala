package uk.co.elder.app.charting.indicators

import uk.co.elder.app.model.{Close, High, Low}
import uk.co.elder._
import uk.co.elder.app.charting.Smoothing._
import uk.co.elder.app.charting.calculators.movingTrueRange
import uk.co.elder.dataSeriesChronoligicalOrdering
import uk.co.elder.app.charting.DivideBigDecimal

import scalaz.std.AllInstances.bigDecimalInstance

case class TrueRangeComponents(close: Close, high: High, low: Low)

object TrueRangeComponents {
  def fromDirectionalMovementComponents(e: DirectionalMovementComponents): TrueRangeComponents = TrueRangeComponents(e.close, e.high, e.low)
}

trait AverageTrueRange {
  private implicit def ordering = dataSeriesChronoligicalOrdering[BigDecimal]

  private def averageTrueRange(input: DataSeries[TrueRangeComponents], window: Int) : DataSeries[BigDecimal] =
    simpleMovingAverage(movingTrueRange(input), window)
}
