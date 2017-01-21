package uk.co.elder.app.repository

import java.util.Date

import com.datastax.driver.core.{ResultSet, Row}
import uk.co.elder.app.model.{Date => QuoteDate, _}
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl.{BigDecimalColumn, OptionalBigDecimalColumn, StringColumn, _}
import com.websudos.phantom.keys.{ClusteringOrder, PrimaryKey}
import uk.co.elder.app.model.ElderError
import org.joda.time.LocalDate

import scala.concurrent.Future
import scalaz.\/
import scalaz.concurrent.Task

class DatapointsRecord extends CassandraTable[DatapointsRecord, Datapoints] {
  object symbol extends StringColumn(this) with PrimaryKey[String]
  object date extends DateColumn(this) with ClusteringOrder[Date]
  object open extends BigDecimalColumn(this)
  object close extends BigDecimalColumn(this)
  object high extends BigDecimalColumn(this)
  object low extends BigDecimalColumn(this)
  object volume extends IntColumn(this)
  object movingAverage20Day extends OptionalBigDecimalColumn(this)
  object movingAverage50day extends OptionalBigDecimalColumn(this)
  object movingAverage100day extends OptionalBigDecimalColumn(this)
  object movingAverage200day extends OptionalBigDecimalColumn(this)
  object averageTrueRange14Day extends OptionalBigDecimalColumn(this)
  object forceIndex extends OptionalBigDecimalColumn(this)
  object relativeStrengthIndex14Day extends OptionalBigDecimalColumn(this)
  object highOrLow52Week extends OptionalIntColumn(this)

  override def fromRow(r: Row): Datapoints = {
    Datapoints(
      symbol = Ticker(symbol(r)),
      date = QuoteDate(new LocalDate(date(r))),
      open = Open(open(r)),
      close = Close(close(r)),
      high = High(high(r)),
      low = Low(low(r)),
      volume = Volume(volume(r)),
      movingAverage20Day = movingAverage20Day(r),
      movingAverage50Day = movingAverage50day(r),
      movingAverage100Day = movingAverage100day(r),
      movingAverage200Day = movingAverage200day(r),
      averageTrueRange14Day = averageTrueRange14Day(r),
      forceIndex = forceIndex(r),
      relativeStrengthIndex14Day = relativeStrengthIndex14Day(r),
      highOrLow52Week = highOrLow52Week(r)
    )
  }
}

object DatapointsCassandraRepository extends DatapointsRecord with CassandraSessionProvider with DatapointsRepository {

  override def tableName = "datapoints"

  def insertDatapoints(dp: Datapoints): Future[ResultSet] = insertQuery(dp).future()

  def insertDatapointsList(dp: List[Datapoints]): Task[\/[ElderError, List[ResultSet]]] = {
    import uk.co.elder.FutureExtensionOps
    val task = new FutureExtensionOps(Future.sequence(dp.map(insertQuery).map(_.future()))).asTask()
    task.map { e => \/.right(e) }
  }

  def findDatapoints(symbol: String) : Future[Seq[Datapoints]] = select.where(_.symbol eqs symbol).fetch()

  private def insertQuery(dp: Datapoints) = insert
    .value(_.symbol, dp.symbol.value)
    .value(_.date, dp.date.value.toDate)
    .value(_.open, dp.open.value)
    .value(_.close, dp.close.value)
    .value(_.high, dp.high.value)
    .value(_.low, dp.low.value)
    .value(_.volume, dp.volume.value)
    .value(_.movingAverage20Day, dp.movingAverage20Day)
    .value(_.movingAverage50day, dp.movingAverage50Day)
    .value(_.movingAverage100day, dp.movingAverage100Day)
    .value(_.movingAverage200day, dp.movingAverage200Day)
    .value(_.averageTrueRange14Day, dp.averageTrueRange14Day)
    .value(_.forceIndex, dp.forceIndex)
    .value(_.relativeStrengthIndex14Day, dp.relativeStrengthIndex14Day)
    .value(_.highOrLow52Week, dp.highOrLow52Week)
}

trait DatapointsRepository {
  def insertDatapoints(dp: Datapoints) : Future[ResultSet]

  def insertDatapointsList(dp: List[Datapoints]) : Task[\/[ElderError, List[ResultSet]]]

  def findDatapoints(symbol: String) : scala.concurrent.Future[Seq[Datapoints]]
}