package uk.co.elder.app.charting.indicators

import uk.co.elder.DataSeries
import uk.co.elder.app.charting.slidingApply
import uk.co.elder.dataSeriesChronoligicalOrdering


trait PeriodPriceHighLow {
  def highLow(data: DataSeries[BigDecimal], period: Int) : DataSeries[Int] = slidingApply(period)(data)(calculatePriceStatus).toList

  private def calculatePriceStatus(d: List[BigDecimal]): Int = {
    if (d.take(d.size - 1).max < d.last) return 1
    if (d.take(d.size - 1).min > d.last) return -1
    0
  }
}
