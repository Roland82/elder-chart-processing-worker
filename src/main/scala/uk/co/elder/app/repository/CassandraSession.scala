package uk.co.elder.app.repository

import com.datastax.driver.core.Cluster
import com.websudos.phantom.dsl._

trait CassandraSessionProvider {
  lazy implicit val keyspace = KeySpace.apply("elder")
  lazy implicit val cassandraSession = Cluster.builder().withoutJMXReporting().addContactPoint("192.168.99.100").build().connect()
}