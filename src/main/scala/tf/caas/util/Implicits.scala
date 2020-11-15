package tf.caas.util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, GregorianCalendar, TimeZone}

import com.twitter.util.{Await, Future, FuturePool, Promise => TwitterPromise}

import scala.concurrent.{Future => ScalaFuture}
import scala.io.Source


object Implicits {


  implicit val futurePool = FuturePool.unboundedPool

  implicit def async[A](f: => A): Future[A] = futurePool { f }

  def tryWith[A <: AutoCloseable, B](a: A)(fn: A => B): B = {
    try {
      fn(a)
    } finally {
      if (a != null) {
        a.close()
      }
    }
  }

  implicit class FutureEnhance[T](f: Future[T]) {
    def sync(): T = Await.result(f)
  }


  implicit class ImplicitList[T](value: Seq[T]) {

    def itemIsDuplicated: Boolean = value.distinct.size != value.size

    def itemDuplicated: Seq[T] = {
      value.groupBy(f => f).collect {
        case (x, ys) if ys.size > 1 => x
      }.toSeq
    }

    def opt: Option[Seq[T]] = Option(value) match {
      case Some(x) if x.isEmpty => None
      case x => x
    }

  }

  implicit class ImplicitCollection[A](value: Stream[A]) {
    def convertToSeq: Seq[A] = {
      val seq = scala.collection.mutable.ListBuffer.empty[A]
      val it = value.iterator
      while (it.hasNext) seq += it.next()
      seq
    }
  }

  implicit class ImplicitMilliseconds(value: Long) {
    def asDayOfWeek: Int = {
      val calendar = new GregorianCalendar()
      calendar.setTime(new Date(value))
      calendar.get(Calendar.DAY_OF_WEEK)
    }

    def asDayOfMonth: Int = {
      val calendar = new GregorianCalendar()
      calendar.setTime(new Date(value))
      calendar.get(Calendar.DAY_OF_MONTH)
    }

    def asDate: Date = {
      new Date(value)
    }

    def format(formatted: String = "yyyy-MM-dd"): String = {
      new SimpleDateFormat(formatted).format(value)
    }

    def asTimeFormula(): String = {
      value match {
        case x if x > 0 => {
          val calendar = new GregorianCalendar()
          calendar.setTime(new Date(value))
          calendar.setTimeZone(TimeZone.getDefault)
          s"TIME(${calendar.get(Calendar.HOUR_OF_DAY)}, ${calendar.get(Calendar.MINUTE)}, ${calendar.get(Calendar.SECOND)})"
        }
        case _ => {
          s"TIME(0,0,0)"
        }
      }
    }

    def asTimeFormulaWithoutTimeZone(): String = {
      val hours = value / 3600000
      var tmp = (value % 3600000)
      val minutes = tmp / 60000
      tmp = tmp % 60000
      val seconds = tmp / 1000
      s"TIME(${hours}, ${minutes}, ${seconds})"
    }
  }

  implicit class ImplicitDayOfWeek(value: Int) {
    def asString: String = value match {
      case Calendar.MONDAY => "Mon"
      case Calendar.TUESDAY => "Tue"
      case Calendar.WEDNESDAY => "Wed"
      case Calendar.THURSDAY => "Thu"
      case Calendar.FRIDAY => "Fri"
      case Calendar.SATURDAY => "Sat"
      case Calendar.SUNDAY => "Sun"
      case _ => s"unknown"
    }
  }

  import language.implicitConversions
  import scala.concurrent.ExecutionContext
  import scala.util.{Failure, Success}

  implicit class ScalaFutureLike[A](val sf: ScalaFuture[A]) extends AnyVal {
    def asTwitter(implicit e: ExecutionContext): Future[A] = {
      val promise: TwitterPromise[A] = new TwitterPromise[A]()
      sf.onComplete {
        case Success(value)     => promise.setValue(value)
        case Failure(exception) => promise.setException(exception)
      }
      promise
    }
  }


}
