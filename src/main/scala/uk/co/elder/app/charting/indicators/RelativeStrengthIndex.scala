package uk.co.elder.app.charting.indicators

import uk.co.elder.{Dividable, DataSeries, DividableSyntax}
import uk.co.elder.app.charting.Smoothing.simpleMovingAverage
import uk.co.elder.app.charting._
import org.joda.time.LocalDate

import scalaz.Monoid
import scalaz.syntax.monoid._

private[charting] case class DailyChange(positive: BigDecimal, negative: BigDecimal) {
  def * (i: Int) = DailyChange(positive * i, negative * i)
}

private[charting] object DailyChange {
  def create(change: BigDecimal)(implicit n: Numeric[BigDecimal]): DailyChange =
    DailyChange(zeroifyWhen(change)(_ < 0), n.abs(zeroifyWhen(change)(_ > 0)))

  implicit def add = new Monoid[DailyChange] {
    def zero: DailyChange = DailyChange(0, 0)

    def append(f1: DailyChange, f2: => DailyChange): DailyChange =
      DailyChange(f1.positive + f2.positive, f1.negative + f2.negative)
  }

  implicit def divide = new Dividable[DailyChange] {
    def divide(a: DailyChange, divider: Int): DailyChange = DailyChange(a.positive / divider, a.negative / divider)
  }
}

trait RelativeStrengthIndex {
  import uk.co.elder.dataSeriesChronoligicalOrdering

  def relativeStrengthIndex(d: DataSeries[BigDecimal], period: Int) : DataSeries[BigDecimal] = {
    def calculateRs(averages: DataSeries[DailyChange], changes: DataSeries[DailyChange]) : DataSeries[BigDecimal] = {
      val (_, lastAverage) = averages.last
      changes match {
        case (date, change)::xs => calculateRs(averages :+ (date, ((lastAverage * (period - 1)) |+| change) / period), xs)
        case Nil => averages.map { case (date, change) => date -> (change.positive / change.negative).round(rounding) }
      }
    }

    def rsi(e: (LocalDate, BigDecimal)) = e._1 -> (100 - (100 / (1 + e._2))).round(rounding)

    if (d.size < period) return List.empty
    val changes = slidingApply(2)(d)(l => l(1) - l(0))
    val dailyChanges = (for ((date, change) <- changes) yield (date, DailyChange.create(change))).sorted
    val nextChanges = dailyChanges.drop(period)
    val firstAverage = simpleMovingAverage(dailyChanges.take(period), period).take(1)
    calculateRs(firstAverage, nextChanges) map rsi
  }
}




