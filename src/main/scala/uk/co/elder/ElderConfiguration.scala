package uk.co.elder

import com.typesafe.config.{Config, ConfigFactory}

import scalaz.Maybe.{Just, empty}
import scalaz.NonEmptyList._
import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import scalaz.syntax.applicative._
import scalaz.syntax.validation._
import scalaz._
import scalaz.Maybe.maybeInstance
import java.net.{URL}
import uk.co.elder.app.model.Ticker

case class RabbitMqExchangeConfig(exchangeName: String, durable: Boolean, autoDelete: Boolean)
case class RabbitMqQueueConfig(queueName: String, durable: Boolean, autoDelete: Boolean)
case class RabbitMqConnectionDetails(userName: String, password: String, host: String, port: Int) {
  def connectionString(): String = s"amqp://$userName:$password@$host:$port"
}
case class ElderConfig(connectionDetails: RabbitMqConnectionDetails, exchangeConfig: RabbitMqExchangeConfig, queueConfig: RabbitMqQueueConfig, yahooConfig: YahooFinanceServiceConfig)
case class YahooFinanceServiceConfig(host: String, port: Maybe[Int]) {
  def downloadCsvUrl(ticker: Ticker): String = {
    val path = s"/table.csv?s=${ticker.value}&ignore=.csv"
    port match {
      case Just(p) => s"http://$host:$p$path"
      case empty => s"http://$host$path"
    }
  }
}

object ElderConfiguration {

  def safelyGetProperty[A](c: Config)(f: Config => A): ValidationNel[String, A] =
    Validation.fromTryCatchNonFatal(f(c)).leftMap { e => e.getMessage } .toValidationNel[String, A]

  def validateProperty[A, B](f: => B)(validation: Maybe[B => ValidationNel[String, B]]): ValidationNel[String, B] = {
    val property = Validation.fromTryCatchNonFatal(f).leftMap { e => e.getMessage }.toValidationNel[String, B]

    validation match {
      case Just(v) => property.flatMap(v)
      case empty => property
    }
  }

  def validatePropertyM[M[_], B](f: => B)(validation: Maybe[B => ValidationNel[String, B]])(implicit ev: Applicative[M]): ValidationNel[String, M[B]] = {
    val property = Validation.fromTryCatchNonFatal(f).leftMap { e => e.getMessage }.toValidationNel[String, B]

    validation match {
      case Just(v) => property.flatMap(v).map(e => ev.point(e))
      case empty => property.map(e => ev.point(e))
    }
  }

  def load(): ValidationNel[String, ElderConfig] = {
    val c = ConfigFactory.load()
    (loadRabbitConnectionDetails(c) |@| loadRabbitExchangeDetails(c) |@| loadRabbitQueueDetails(c) |@| loadYahooConfig(c)) { ElderConfig }
  }

  def loadYahooConfig(config: Config): ValidationNel[String, YahooFinanceServiceConfig] = {
    def getHost(c: Config): ValidationNel[String, String] = {
      validateProperty(c.getString("app.yahooFinance.host"))(
        Just((e: String) => if (e.isEmpty) s"A host name must be given for Yahoo Finance".failureNel else e.successNel)
      )
    }

    def parsePort(host: String, c: Config) : ValidationNel[String, Maybe[Int]] = {
      host match {
        case "localhost" => {
          validatePropertyM[Maybe, Int](c.getInt("app.yahooFinance.port"))(
            Just((port:Int) => if (port < 1) s"A port must be given".failureNel else port.successNel)
          )
        }
        case _ => empty[Int].successNel
      }
    }

    val yahooValidation = for {
      host <- getHost(config)
      port <- parsePort(host, config)
    } yield (host, port)

    yahooValidation.map(e => YahooFinanceServiceConfig(e._1, e._2))
  }

  def loadRabbitExchangeDetails(config: Config): ValidationNel[String, RabbitMqExchangeConfig] = {
    def getDurable(c: Config): ValidationNel[String, Boolean] = {
      safelyGetProperty(c)(_.getBoolean("app.rabbitmq.exchange.durable"))
    }

    def getAutoDelete(c: Config): ValidationNel[String, Boolean] = {
      safelyGetProperty(c)(_.getBoolean("app.rabbitmq.exchange.autoDelete"))
    }

    def parseExchangeName(c: Config) : ValidationNel[String, String] = {
      validateProperty(c.getString("app.rabbitmq.exchange.name"))(
        Just((e: String) => if (e.isEmpty) s"A Rabbit MQ Exchange Name must be given".failureNel else e.successNel)
      )
    }

    (parseExchangeName(config) |@| getDurable(config) |@| getAutoDelete(config)) { RabbitMqExchangeConfig }
  }

  def loadRabbitQueueDetails(config: Config): ValidationNel[String, RabbitMqQueueConfig] = {
    def getDurable(c: Config): ValidationNel[String, Boolean] = {
      safelyGetProperty(c)(_.getBoolean("app.rabbitmq.queue.durable"))
    }

    def getAutoDelete(c: Config): ValidationNel[String, Boolean] = {
      safelyGetProperty(c)(_.getBoolean("app.rabbitmq.queue.autoDelete"))
    }

    def parseQueueName(c: Config) : ValidationNel[String, String] = {
      validateProperty(c.getString("app.rabbitmq.queue.name"))(
        Just((e: String) => if (e.isEmpty) s"A Rabbit MQ Queue Name must be given".failureNel else e.successNel)
      )
    }

    (parseQueueName(config) |@| getDurable(config) |@| getAutoDelete(config)) { RabbitMqQueueConfig }
  }

  def loadRabbitConnectionDetails(config: Config) : ValidationNel[String, RabbitMqConnectionDetails] = {
    def getPort(c: Config): ValidationNel[String, Int] = {
      validateProperty(c.getInt("app.rabbitmq.connection.port"))(
        Just((e: Int) => if (e < 0) s"The port must be a number greater than 0. Actual: $e".failureNel else e.successNel)
      )
    }

    def parseUserName(c: Config): ValidationNel[String, String] = {
      validateProperty(c.getString("app.rabbitmq.connection.userName"))(
        Just((p: String) => if (p.isEmpty) s"A username must be given".failureNel else p.successNel)
      )
    }

    def parsePassword(c: Config): ValidationNel[String, String] = {
      validateProperty(c.getString("app.rabbitmq.connection.password"))(
        Just((p: String) => if (p.isEmpty) s"A password must be given".failureNel else p.successNel)
      )
    }

    def parseHost(c: Config): ValidationNel[String, String] = {
      validateProperty(c.getString("app.rabbitmq.connection.host"))(
        Just((p: String) => if (p.isEmpty) s"A host must be given".failureNel else p.successNel)
      )
    }

    (parseUserName(config) |@| parsePassword(config) |@| parseHost(config) |@| getPort(config)) {
      RabbitMqConnectionDetails
    }
  }
}

