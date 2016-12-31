package uk.co.elder.app.model

import org.joda.time.LocalDate

case class Quote(symbol: Ticker, date: Date, open: Open, close: Close, high: High, low: Low, volume: Volume){
  def toDatapoints():  Datapoints = {
    Datapoints(symbol = symbol, date = date,
      open = open, close = close,
      high = high, low = low, volume = volume,
      None, None, None, None, None)
  }
}

case class Ticker(value: String) extends AnyVal
case class Date(value: LocalDate) extends AnyVal
case class Open(value: BigDecimal) extends AnyVal
case class Close(value: BigDecimal) extends AnyVal
case class High(value: BigDecimal) extends AnyVal
case class Low(value: BigDecimal) extends AnyVal
case class Volume(value: Int) extends AnyVal

case class Datapoints(symbol: Ticker,
                      date: Date,
                      open: Open,
                      close: Close,
                      high: High,
                      low: Low,
                      volume: Volume,
                      movingAverage20Day: Option[BigDecimal] = None,
                      movingAverage50Day: Option[BigDecimal] = None,
                      movingAverage100Day: Option[BigDecimal] = None,
                      movingAverage200Day: Option[BigDecimal] = None,
                      averageTrueRange14Day: Option[BigDecimal] = None,
                      forceIndex: Option[BigDecimal] = None,
                      relativeStrengthIndex14Day: Option[BigDecimal] = None,
                      highOrLow52Week: Option[Int] = None
                    )

object Datapoints {
  def fromQuote(q: Quote): Datapoints = {
    Datapoints(symbol = q.symbol, date = q.date,
      open = q.open, close = q.close,
      high = q.high, low = q.low, volume = q.volume,
      None, None, None, None, None, None, None)
  }
}


