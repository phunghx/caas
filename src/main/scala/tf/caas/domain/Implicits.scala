package tf.caas.domain

import java.util

import com.twitter.util.FuturePool

import scala.collection.JavaConversions._


object Implicits {
  implicit val futurePool = FuturePool.unboundedPool

  implicit def toIntegerList(lst: List[Int]): util.List[Integer] = seqAsJavaList(lst.map(i => i: java.lang.Integer))

  implicit def toIntegerSet(set: Set[Int]): util.Set[Integer] = setAsJavaSet(set.map(i => i: java.lang.Integer))
}
