package uk.co.elder.app.service.dataproviders

import java.io.StringReader

import uk.co.elder.app.model._
import com.github.tototoshi.csv._
import uk.co.elder.app.model.{ElderError, Quote}
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

import scala.util.Try
import scalaj.http.Http
import scalaz.{-\/, DisjunctionT, \/, \/-}
import scalaz.Maybe.Just
import scalaz.concurrent.Task
import scalaz.Maybe.empty

object YahooStockQuoteProvider {
  private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

  def request(symbol: Ticker, host: String): Task[ElderError \/ List[Quote]] = {
    val chain = for {
      response <- DisjunctionT(callYahoo(symbol, host))
      quotes <- DisjunctionT(parseCsv(symbol)(response))
    } yield quotes

    chain.run
  }

  private def parseCsv(symbol: Ticker)(body: String) : Task[ElderError \/ List[Quote]] = {
    Task {
      try {
        val result = CSVReader.open(new StringReader(body)).all().tail.map {
          case List(date, open, high, low, close, volume, adjClose) => {
            Quote(symbol,
              Date(LocalDate.parse(date, formatter)),
              Open(BigDecimal(open)),
              Close(BigDecimal(close)),
              High(BigDecimal(high)),
              Low(BigDecimal(low)),
              Volume(volume.toInt))
          }
        }
        \/.right(result)
      } catch {
        case e: Exception => \/.left(ElderError(s"Parsing CSV for stock ${symbol.value} returned from server failed.", Just(e)))
      }
    }
  }

  private def callYahoo(symbol: Ticker, host: String): Task[ElderError \/ String] = {
    Task {
      Try {
        val res = Http(s"http://$host/table.csv?s=${symbol.value}&ignore=.csv").timeout(2000, 2000).asString
        res.code match {
          case 200 => \/-(res.body)
          case r => -\/(ElderError(s"Call to yahoo failed for ${symbol.value}. Response code = $r", empty))
        }
      }.recover {
        case e: Exception => -\/(ElderError(s"Something went wrong with the HTTP request for ${symbol.value}.", Just(e)))
      }.get
    }
  }
}