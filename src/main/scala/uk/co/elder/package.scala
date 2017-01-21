package uk.co

import io.relayr.amqp.Message
import org.joda.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future, Promise}
import scalaz.Monoid
import scalaz.concurrent.Task
import scala.util.{Failure, Success}

package object elder {
  type QueueConsumer = Message => Unit
  type DataSeries[T] = List[(LocalDate, T)]

  implicit def dataSeriesChronoligicalOrdering[T]: Ordering[(LocalDate, T)] = Ordering.fromLessThan((e, f) => e._1 isBefore f._1)
  implicit def dataSeriesReverseChronoligicalOrdering[T]: Ordering[(LocalDate, T)] = Ordering.fromLessThan((e, f) => e._1 isAfter f._1)

  implicit class ExtendedBigDecimal(val value: BigDecimal) extends AnyVal {

    def difference(other: BigDecimal) = value.max(other) - value.min(other)
    def max(other: BigDecimal) = if (other > value) other else value
    def min(other: BigDecimal) = if (other > value) value else other
  }

  implicit def numericMonoid[A](n: Numeric[A]) = new Monoid[A] {
    def zero: A = n.zero

    def append(f1: A, f2: => A): A = n.plus(f1, f2)
  }

  trait Dividable[A] {
    def divide(a: A, divider: Int): A
  }

  implicit class DividableSyntax[A](a: A)(implicit ev: Dividable[A]) {
    def /(by: Int) : A = {
      ev.divide(a, by)
    }
  }

  trait Minus[A] {
    def minus(a: A, other: A): A
  }

  implicit class MinusSyntax[A](a: A)(implicit ev: Minus[A]) {
    def - (other: A) : A = {
      ev.minus(a, other)
    }
  }



  final class FutureExtensionOps[A](x: => Future[A]) {

    import scalaz.Scalaz._

    def asTask(): Task[A] = {
      Task.async {
        register =>
          x.onComplete {
            case Success(v) => register(v.right)
            case Failure(ex) => register(ex.left)
          }
      }
    }
  }

  final class TaskExtensionOps[A](x: => Task[A]) {
    import scalaz.{-\/, \/-}
    val p: Promise[A] = Promise()
    def runFuture(): Future[A] = {
      x.unsafePerformAsync {
        case -\/(ex) =>
          p.failure(ex); ()
        case \/-(r) => p.success(r); ()
      }
      p.future
    }
  }

  implicit val ioNonBlockingContext = new ExecutionContext {
    val threadPool = Executors.newSingleThreadExecutor()

    def execute(runnable: Runnable) {
      threadPool.submit(runnable)
    }

    def reportFailure(t: Throwable): Unit = {
      println("Execution failed " + t.getMessage)
    }
  }
}
