package uk.co.elder.app

import java.math.{MathContext, RoundingMode}

import uk.co.elder.{DataSeries, Dividable, Minus, dataSeriesChronoligicalOrdering}
import uk.co.elder.app.charting.indicators._

import scalaz.Monoid
import scalaz.syntax.monoid._

package object charting {
  val rounding = new MathContext(4, RoundingMode.HALF_UP)

  def slidingFold[F, K](period: Int)(l: DataSeries[F])(implicit m: Ordering[(K, F)], ev: Monoid[F]): DataSeries[F] = {
    def go(): DataSeries[F] = {
      val x = for (e <- l.sorted.sliding(period)) yield e.last._1 -> e.map(e => e._2).fold[F](ev.zero)(_ |+| _)
      x.toList
    }

    if (l.size < period) List.empty else go()
  }

  def slidingApply[K, F, RF](period: Int)(l: DataSeries[F])(f: List[F] => RF) (implicit m: Ordering[(K, F)]): DataSeries[RF] = {
    val returnList = for { group <- l.sorted.sliding(period) } yield group.last._1 -> f(group.map(_._2))
    returnList.toList
  }

  def slidingApplyList[K, F, RF](l: List[F])(f: List[F] => RF)(period: Int) (implicit m: Ordering[F]): List[RF] = {
    (for { group <- l.sorted.sliding(period) } yield f(group)).toList
  }

  def zeroifyWhen(d: BigDecimal)(when: BigDecimal => Boolean) : BigDecimal = if (when(d)) 0 else d

  def zeroify(d: BigDecimal) : BigDecimal = if (d < 0) 0 else d

  object calculators extends RelativeStrengthIndex with SimpleMovingAverage with DirectionalMovment with TrueRange with AverageTrueRange with ForceIndex with PeriodPriceHighLow

  implicit def DivideBigDecimal = new Dividable[BigDecimal] {
    def divide(a: BigDecimal, divider: Int): BigDecimal = a / divider
  }

  implicit def MinusBigDecimal = new Minus[BigDecimal] {
    def minus(a: BigDecimal, other: BigDecimal): BigDecimal = a - other
  }
}
