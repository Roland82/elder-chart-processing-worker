package uk.co.elder

import uk.co.elder.listener._

object Main extends App
{
  // TODO: Pull in from config file
  val config = RabbitMQConfig(
    "amqp://user:password@192.168.99.100:5672",
    ExchangeConfig("stock-data", durable = true, autoDelete = false),
    QueueConfig("datapoints-process", durable = true, autoDelete = false)
  )

  Connection.connectAndConsume(ProcessMessage.consumer).run(config)
}
