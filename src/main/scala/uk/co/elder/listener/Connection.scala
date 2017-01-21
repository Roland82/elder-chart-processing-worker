package uk.co.elder.listener

import io.relayr.amqp.{ChannelOwner, _}
import uk.co.elder.{QueueConsumer, ElderConfig}

import scala.concurrent.duration.Duration
import scala.concurrent.duration.SECONDS
import scalaz._
import uk.co.elder.listener.ConnectionBuilder._


object Connection {
  def connectAndConsume(q: QueueConsumer) = {
    for {
      connection <- createConnection
      queueDeclare <- getQueueDetails
      channel <- newChannel(connection)
      exchange <- declareExchange(channel)
      queueName <- setupQueue(channel, queueDeclare)
      _ <- bindQueue(channel, queueName, exchange)
      closeable <- addConsumer(channel)(queueDeclare)(q)
    } yield closeable
  }
}

object ConnectionBuilder {
  def createConnection =
      Reader[ElderConfig, ConnectionHolder] { e => {
        ConnectionHolder
          .builder(e.connectionDetails.connectionString())
          .reconnectionStrategy(ReconnectionStrategy.JavaClientFixedReconnectDelay(Duration(1, SECONDS)))
          .build()
      }
    }

  def getQueueDetails = Reader[ElderConfig, QueueDeclare] { e =>
    QueueDeclare(Some(e.queueConfig.queueName), durable = e.queueConfig.durable, exclusive = false, autoDelete = e.queueConfig.autoDelete)
  }

  def newChannel(c: ConnectionHolder) =
    Reader[ElderConfig, ChannelOwner] { _ => c.newChannel() }

  def declareExchange(c: ChannelOwner) = Reader[ElderConfig, Exchange] { e =>
      c.declareExchange(
        e.exchangeConfig.exchangeName,
        ExchangeType.direct,
        durable = e.exchangeConfig.durable,
        autoDelete = e.exchangeConfig.autoDelete)
  }

  def setupQueue(c: ChannelOwner, queueDeclare: QueueDeclare) =
    Reader[ElderConfig, String] { e =>
      {
        c.declareQueue(queueDeclare)
      }
    }

  def bindQueue(c: ChannelOwner, queueName: String, exchange: Exchange) =
    Reader[ElderConfig, Unit] { e => {
        c.queueBind(QueuePassive(queueName), exchange, "stock-data")
      }
    }


  def addConsumer(c: ChannelOwner)(queueDeclare: QueueDeclare)(consumer: QueueConsumer) = Reader { (e: ElderConfig) => {
      c.addConsumer(queueDeclare, consumer)
    }
  }
}