package uk.co.elder.app.service

import uk.co.elder.app.model.{Datapoints, Quote}
import uk.co.elder.app.charting.calculators._
import uk.co.elder.app.model.ElderError
import org.joda.time.LocalDate
import uk.co.elder.DataSeries

import scalaz.concurrent.Task
import scalaz.{-\/, \/, \/-}
import scalaz.Maybe.Just
object RefreshDatapoints {

  private implicit def dateTimeOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)
  private implicit def quoteOrdering: Ordering[Quote] = Ordering.fromLessThan(_.date.value isBefore _.date.value)

  private val processFromDate = LocalDate.now().minusYears(2)

  def refresh(quotes: List[Quote]): Task[ElderError \/ List[Datapoints]] = Task { prefresh(quotes) }

  private def prefresh(quotes: List[Quote]): \/[ElderError, List[Datapoints]] = {
    try {
      val yahooData = quotes.filter(q => q.date.value.isAfter(processFromDate)).sorted
      val closes = yahooData map { e => (e.date.value, e.close.value) }

      val ma200Day = simpleMovingAverage(closes, 200)
      val ma100Day = simpleMovingAverage(closes, 100)
      val ma50Day = simpleMovingAverage(closes, 50)
      val ma20Day = simpleMovingAverage(closes, 20)
      val fi = forceIndex(yahooData.map(d => d.date.value -> ForceIndexComponents(d.close, d.volume)))
      val rsi = relativeStrengthIndex(closes, 14)
      val highVsLow52Week = highLow(closes, 365)

      val result = yahooData.map(Datapoints.fromQuote).map { e => e.copy(
        movingAverage50Day = tryGetVal(ma50Day, e.date.value),
        movingAverage100Day = tryGetVal(ma100Day, e.date.value),
        movingAverage200Day = tryGetVal(ma200Day, e.date.value),
        movingAverage20Day = tryGetVal(ma20Day, e.date.value),
        forceIndex = tryGetVal(fi, e.date.value))
      }

      \/-(result)
    } catch {
      case e: Exception => -\/(ElderError(s"Processing datapoints for '${quotes.head.symbol.value}' failed.", Just(e)))
    }
  }

  private def tryGetVal(vals : DataSeries[BigDecimal], dateToGet: LocalDate) : Option[BigDecimal] = {
    for ((_, value) <- vals.find(_._1 == dateToGet)) yield value
  }
}
