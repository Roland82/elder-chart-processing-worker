package uk.co.elder.app.charting.indicators

import uk.co.elder.DataSeries
import uk.co.elder.app.charting.Smoothing
import uk.co.elder.dataSeriesChronoligicalOrdering
import scalaz.std.AllInstances.bigDecimalInstance
import uk.co.elder.app.charting.DivideBigDecimal

trait SimpleMovingAverage {
  private implicit def ordering = dataSeriesChronoligicalOrdering[BigDecimal]

  def simpleMovingAverage(d: DataSeries[BigDecimal], period: Int) : DataSeries[BigDecimal] = Smoothing.simpleMovingAverage(d, period)
}
