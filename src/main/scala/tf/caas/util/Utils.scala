package tf.caas.util

import scala.util.Random


object Utils {
  val random = Random

  def randomInt(from: Int = Integer.MIN_VALUE, to: Int = Integer.MAX_VALUE): Int = {
    val randomVal = random.nextInt(to)
    if (randomVal < from) randomInt(from, to) else randomVal
  }
}
