package uk.co.elder.app.charting

import java.math.{MathContext, RoundingMode}
import uk.co.elder.{DataSeries, DividableSyntax, dataSeriesChronoligicalOrdering, Dividable, Minus, MinusSyntax}
import org.joda.time.LocalDate
import scalaz.Monoid
import scalaz.syntax.monoid._


private object Smoothing {

  def average[A](as: Seq[A])(implicit n: Numeric[A]) : BigDecimal = BigDecimal(n.toDouble(as.sum) / as.size)

  def simpleMovingAverage[A](as: DataSeries[A], period: Int)(implicit m: Monoid[A], d: Dividable[A], o: Ordering[(LocalDate, A)]) : DataSeries[A] = {
    for ((date, value) <- slidingFold(period)(as)) yield date -> value / period
  }

  def wilderMovingAverage[A](input: DataSeries[A], period: Int)(implicit m: Minus[A], d: Dividable[A], p: Monoid[A]): DataSeries[A] = {
    if (input.size < period) return List.empty

    def go(previous: A, series: DataSeries[A], output: DataSeries[A]): DataSeries[A] = {
      series match {
        case (date, value)::xs => {
          val currentMa = previous - (previous / period) |+| value
          go(currentMa, xs, output :+ ((date, currentMa)))
        }

        case _ => output
      }
    }

    val sortedData = input.sorted
    val firstVal = sortedData.take(period).last._1 -> sortedData.take(period).foldRight(p.zero)((e, f) => e._2 |+| f)
    go(firstVal._2, sortedData.drop(period), List(firstVal))
  }

  def exponentialMovingAverage(input: DataSeries[BigDecimal], period: Int) : DataSeries[BigDecimal] = {
    val sorted = input.sorted
    val multiplier = 2.toFloat / (period + 1)
    val slidingPeriods = sorted.sliding(period).toSeq
    val emaStart = List(
                          (slidingPeriods(0).last._1,
                           average(slidingPeriods(0).map(_._2)).round(new MathContext(4, RoundingMode.HALF_UP))
                          )
                       )

    def calculate(windows: Seq[Seq[(LocalDate, BigDecimal)]], movingEma: DataSeries[BigDecimal]) : DataSeries[BigDecimal] =
      windows match {
        case x #:: xs => {
          val newEma = (x.last._2 - movingEma.last._2) * multiplier + movingEma.last._2
          calculate(xs, movingEma :+ (x.last._1 -> newEma.round(new MathContext(4, RoundingMode.HALF_UP))))
        }

        case _ => movingEma
      }

    calculate(slidingPeriods drop 1, emaStart)
  }


}
