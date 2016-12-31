package uk.co.elder.app.charting.indicators

import uk.co.elder.app.model.{Close, Volume}
import uk.co.elder.{DataSeries, dataSeriesChronoligicalOrdering}
import uk.co.elder.app.charting.slidingApplyList

trait ForceIndex {
  def forceIndex(
      data: DataSeries[ForceIndexComponents]): DataSeries[BigDecimal] = {
    slidingApplyList(data)(e =>
      (e(1)._1, (e(1)._2.close.value - e(0)._2.close.value) * e(1)._2.volume.value))(2)
  }

  case class ForceIndexComponents(close: Close, volume: Volume)
}
