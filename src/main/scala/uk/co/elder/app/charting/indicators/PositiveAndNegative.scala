package uk.co.elder.app.charting.indicators

import uk.co.elder.app.model.{Close, High, Low}
import uk.co.elder.app.charting.Smoothing._
import uk.co.elder.app.charting.calculators.movingTrueRange
import uk.co.elder.app.charting.{DivideBigDecimal, MinusBigDecimal, rounding, slidingApplyList, zeroify}
import uk.co.elder.{DataSeries, Dividable, Minus, dataSeriesChronoligicalOrdering}
import org.joda.time.LocalDate

import scalaz.Monoid
import scalaz.std.AllInstances.bigDecimalInstance

object PositiveAndNegative {
  implicit def monoid = new Monoid[PositiveAndNegative] {
    def append(f1: PositiveAndNegative,
               f2: => PositiveAndNegative): PositiveAndNegative = {
      PositiveAndNegative(f1.positiveDm + f2.positiveDm,
                          f1.negativeDm + f2.negativeDm)
    }

    def zero: PositiveAndNegative = PositiveAndNegative(0, 0)
  }

  implicit def minus = new Minus[PositiveAndNegative] {
    def minus(a: PositiveAndNegative,
              other: PositiveAndNegative): PositiveAndNegative =
      PositiveAndNegative(a.positiveDm - other.positiveDm,
                          a.negativeDm - other.negativeDm)
  }

  implicit def divide = new Dividable[PositiveAndNegative] {
    def divide(a: PositiveAndNegative, divider: Int): PositiveAndNegative =
      PositiveAndNegative(a.positiveDm / divider, a.negativeDm / divider)
  }
}

trait DirectionalMovment {
  def directionalMovement(
      components: DataSeries[DirectionalMovementComponents])
    : DataSeries[DirectionalIndicatorResult] = {
    val trueRangeComponents = components map {
      case (date, e) =>
        date -> TrueRangeComponents.fromDirectionalMovementComponents(e)
    }

    val movingDailyDirectionalMovement = dailyDirectionalMovement(components)
    val movingDm14Day = wilderMovingAverage(movingDailyDirectionalMovement, 14)
    val trueRange14Day = wilderMovingAverage(movingTrueRange(trueRangeComponents), 14)

    val trueRangeWithDm14 = merge(trueRange14Day, movingDm14Day)
    val di14 = trueRangeWithDm14 map {
      case (date, tr, dm) =>
        (date,
         PositiveAndNegative(100 * (dm.positiveDm / tr),
                             100 * (dm.negativeDm / tr)))
    }
    val dx = di14 map (e => (e._1, e._2.dx))
    val adx = adxSmoothing(dx, 14)
    val diAndAdx = merge(di14, adx)
    // TODO: Need to fix the rounding logic across all the calculators
    diAndAdx map {
      case (date, di14Val, adxVal) => {
        (date,
         DirectionalIndicatorResult(
           di14Val.positiveDm.setScale(4, BigDecimal.RoundingMode.HALF_UP),
           di14Val.negativeDm.setScale(4, BigDecimal.RoundingMode.HALF_UP),
           adxVal.setScale(4, BigDecimal.RoundingMode.HALF_UP)))
      }
    }
  }

  def dailyDirectionalMovement(data: DataSeries[DirectionalMovementComponents])
    : DataSeries[PositiveAndNegative] = {
    implicit def ordering = dataSeriesChronoligicalOrdering[DirectionalMovment]

    def calculateDirectionalMovement(
        d: DataSeries[DirectionalMovementComponents])
      : (LocalDate, PositiveAndNegative) = {
      val posDi = d(1)._2.high.value - d(0)._2.high.value
      val negDi = d(0)._2.low.value - d(1)._2.low.value
      (
        d(1)._1,
        if (posDi > negDi) PositiveAndNegative(zeroify(posDi), 0)
        else PositiveAndNegative(0, zeroify(negDi))
      )
    }

    slidingApplyList(data)(calculateDirectionalMovement)(2)
  }

  private def adxSmoothing(input: DataSeries[BigDecimal],
                           period: Int): DataSeries[BigDecimal] = {
    def calculate(windows: DataSeries[BigDecimal],
                  results: DataSeries[BigDecimal]): DataSeries[BigDecimal] = {
      windows match {
        case x :: xs => {
          val (date, lastResult) = results.last
          val (_, currentDx) = windows.last
          val nextResult = ((lastResult * (period - 1)) + currentDx) / period
          calculate(xs, results :+ (date -> nextResult.round(rounding)))
        }

        case Nil => results
      }
    }

    val sortedInput = input.sorted
    val firstResult = (input.drop(period - 1).head._1,
                       input.take(period).map(_._2).sum / period)
    val remainingToProcess = sortedInput.drop(period - 1)
    remainingToProcess match {
      case x :: xs => calculate(xs, List(firstResult))
      case _ => List.empty
    }
  }

  // TODO: Make this more generic and move to utility library
  private def merge[A, B](a: DataSeries[A], b: DataSeries[B]) = {
    for (f <- a;
         g <- b.find(e => e._1 == f._1)) yield (f._1, f._2, g._2)
  }
}

case class PositiveAndNegative(positiveDm: BigDecimal, negativeDm: BigDecimal) {
  private def difference = (positiveDm - negativeDm).abs
  private def sum = positiveDm + negativeDm
  def dx = 100 * (difference / sum)
}

case class DirectionalMovementComponents(high: High,
                                         low: Low,
                                         close: Close)
case class DirectionalIndicatorResult(positiveDi: BigDecimal,
                                      negativeDi: BigDecimal,
                                      adx: BigDecimal)
