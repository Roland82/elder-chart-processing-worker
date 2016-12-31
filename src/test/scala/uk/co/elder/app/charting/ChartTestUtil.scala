package uk.co.elder.app.charting

import uk.co.elder.DataSeries
import org.joda.time.LocalDate

object ChartTestUtil {
  def dateVsPriceGraph(startDate: LocalDate, prices: Seq[BigDecimal]): DataSeries[BigDecimal] = {
    def generate(dateSeed: Int, prices: Seq[BigDecimal], graph: DataSeries[BigDecimal]): DataSeries[BigDecimal]  =
      prices match {
        case x :: xs => generate(dateSeed + 1, xs, graph :+ ((startDate.plusDays(dateSeed), x)))
        case _ => graph
      }

    generate(0, prices, List.empty).reverse
  }
}
