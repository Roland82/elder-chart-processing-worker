package uk.co.elder.app.service.dataproviders

import java.io.StringReader
import java.net.URL

import com.github.tototoshi.csv._
import com.ning.http.client.Response
import dispatch.Defaults._
import dispatch._
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import uk.co.elder.YahooFinanceServiceConfig
import uk.co.elder.app.model.{ElderError, Quote, _}

import scalaz.Maybe.{Just, empty}
import scalaz.concurrent.Task
import scalaz.{DisjunctionT, \/}

object YahooStockQuoteProvider {
  private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val httpClient = Http.configure(_.setConnectTimeout(4000))

  def request(symbol: Ticker, yahooConfig: YahooFinanceServiceConfig): Task[ElderError \/ List[Quote]] = {
    val chain = for {
      response <- DisjunctionT(callYahoo(symbol, yahooConfig))
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



  private def callYahoo(symbol: Ticker, yahooConfig: YahooFinanceServiceConfig): Task[ElderError \/ String] = {
    import uk.co.elder.FutureExtensionOps

    val svc = url(yahooConfig.downloadCsvUrl(symbol)) > { (r: Response) => r }

    val result = httpClient(svc)(uk.co.elder.ioNonBlockingContext)
    val response = new FutureExtensionOps(result).asTask()

    response map { t => t.getStatusCode match {
        case 200 => \/.right(t.getResponseBody())
        case s => \/.left(ElderError(s"Call to yahoo failed for ${symbol.value}. Response code = ${s}", empty))
      }
    }
  }
}