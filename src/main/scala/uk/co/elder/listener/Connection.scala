package uk.co.elder.listener

import io.relayr.amqp.{ChannelOwner, _}
import uk.co.elder.QueueConsumer

import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import scalaz._
import uk.co.elder.listener.ConnectionBuilder._

case class ExchangeConfig(exchangeName: String, durable: Boolean, autoDelete: Boolean)
case class QueueConfig(queueName: String, durable: Boolean, autoDelete: Boolean)
case class RabbitMQConfig(connString: String, exchangeConfig: ExchangeConfig, queueConfig: QueueConfig)

object Connection {
  def connectAndConsume(q: QueueConsumer) = {
    for {
      connection <- createConnection
      queueDeclare <- getQueueDetails
      channel <- newChannel(connection)
      _ <- declareExchange(channel)
      _ <- setupQueue(channel)(queueDeclare)
      closeable <- addConsumer(channel)(queueDeclare)(q)
    } yield closeable
  }
}

object ConnectionBuilder {
  def createConnection =
      Reader[RabbitMQConfig, ConnectionHolder] { e => {
        ConnectionHolder
          .builder(e.connString)
          .reconnectionStrategy(ReconnectionStrategy.JavaClientFixedReconnectDelay(Duration(1, SECONDS)))
          .build()
      }
    }

  def getQueueDetails = Reader[RabbitMQConfig, QueueDeclare] { e =>
    QueueDeclare(Some(e.queueConfig.queueName), durable = e.queueConfig.durable, exclusive = false, autoDelete = e.queueConfig.autoDelete)
  }

  def newChannel(c: ConnectionHolder) =
    Reader[RabbitMQConfig, ChannelOwner] { _ => c.newChannel() }

  def declareExchange(c: ChannelOwner) = Reader[RabbitMQConfig, Exchange] { e =>
      c.declareExchange(
        e.exchangeConfig.exchangeName,
        ExchangeType.direct,
        durable = e.exchangeConfig.durable,
        autoDelete = e.exchangeConfig.autoDelete)
  }

  def setupQueue(c: ChannelOwner)(queueDeclare: QueueDeclare) =
    Reader[RabbitMQConfig, String] { e => c.declareQueue(queueDeclare) }

  def addConsumer(c: ChannelOwner)(queueDeclare: QueueDeclare)(consumer: QueueConsumer) = Reader { (e: RabbitMQConfig) => {
      c.addConsumer(queueDeclare, consumer)
    }
  }
}