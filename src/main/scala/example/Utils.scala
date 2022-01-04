package example

import System._

object Utils {
  def benchmark(f: => Unit): Double = {
    val t1 = nanoTime
    f
    (nanoTime - t1) / 1e9d
  }

  trait MainRunnable {
    def main(args: Array[String]): Unit
  }

  trait JustRunnable {
    def run(): Unit
  }
}
