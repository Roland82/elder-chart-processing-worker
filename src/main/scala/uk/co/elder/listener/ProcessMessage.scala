package uk.co.elder.listener

import com.datastax.driver.core.ResultSet
import uk.co.elder.app.model.Ticker
import uk.co.elder.QueueConsumer
import uk.co.elder.app.model.ElderError
import uk.co.elder.app.service.RefreshDatapoints
import uk.co.elder.app.service.dataproviders.YahooStockQuoteProvider
import net.liftweb.json._
import uk.co.elder.app.repository.DatapointsCassandraRepository

import scalaz.Maybe.empty
import scalaz.concurrent.Task
import scalaz._

object ProcessMessage {
  case class RefreshStockChart(name: String, symbol: String)

  val consumer : QueueConsumer = { m =>
    handleMessage(new String(m.body.toArray))
      .unsafePerformAsync {
        case -\/(e) => println()
        case \/-(r) => println(r.toString)
      }
  }

  def handleMessage(body: String): Task[ElderError \/ List[ResultSet]] = {
    implicit val formats = DefaultFormats

    val task = parse(body).extractOpt[RefreshStockChart] match {
      case Some(m) => {
        val chain = for {
          quotes <- DisjunctionT(YahooStockQuoteProvider.request(Ticker(m.symbol), "ichart.finance.yahoo.com"))
          processedData <- DisjunctionT(RefreshDatapoints.refresh(quotes))
          inserted <- DisjunctionT(DatapointsCassandraRepository.insertDatapointsList(processedData))
        } yield inserted

        chain.run
      }

      case None => Task[ElderError \/ List[ResultSet]](-\/(ElderError(s"Message couldnt be parsed $body", empty)))
    }

    Task.fork(task)
  }
}
