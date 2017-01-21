package uk.co.elder

import uk.co.elder.listener._

object Main extends App
{
  ElderConfiguration.load().fold(
    (errors) => errors.foreach(println),
    (configuration) => Connection.connectAndConsume(ProcessMessage.consumer(configuration.yahooConfig)).run(configuration)
  )
}
