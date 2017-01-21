package uk.co.elder.listener

import com.datastax.driver.core.ResultSet
import net.liftweb.json._
import uk.co.elder.app.model.{ElderError, Ticker}
import uk.co.elder.app.repository.DatapointsCassandraRepository
import uk.co.elder.app.service.RefreshDatapoints
import uk.co.elder.app.service.dataproviders.YahooStockQuoteProvider
import uk.co.elder.{QueueConsumer, YahooFinanceServiceConfig}

import scalaz.Maybe.empty
import scalaz._
import scalaz.concurrent.Task

object ProcessMessage {
  implicit val formats = DefaultFormats

  case class RefreshStockChart(name: String, symbol: String)

  def consumer(yahooConfig: YahooFinanceServiceConfig) : QueueConsumer = { m =>
    val message = new String(m.body.toArray)
    handleMessage(yahooConfig, message)
      .unsafePerformAsync {
        case -\/(e) => println(s"High level error for message $message: ${e.getMessage}")
        case \/-(r) =>
          r match {
            case -\/(myerror) => println(s"Error for $message ${myerror.message}")
            case \/-(success) => println(s"$message was successful")
          }
      }
  }

  def handleMessage(yahooConfig: YahooFinanceServiceConfig, body: String): Task[ElderError \/ List[ResultSet]] = {
    val task = parse(body).extractOpt[RefreshStockChart] match {
      case Some(m) => {
        val chain = for {
          quotes <- DisjunctionT(YahooStockQuoteProvider.request(Ticker(m.symbol), yahooConfig))
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
