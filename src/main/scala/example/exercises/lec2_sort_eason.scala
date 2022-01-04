package example.exercises

import example.ScalaSpaces._
import org.jspace.{SequentialSpace, Space}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/** Exercise 2.6. Parallel merge sort
  * Use the producers/consumers coordination pattern where tasks are (ordered and unordered)
  * vectors of integers and workers are specialised in various tasks:
  * (i) splitting an unordered vector,
  * (ii) merging two ordered vectors
  * */
object lec2_sort_eason {

  def run = {

    mergesort(List(3, 1, 2, 4, 5))
  }

  def merge(x1: List[Int], x2: List[Int]): List[Int] = (x1, x2) match {
    case (Nil, x) => x
    case (x, Nil) => x
    case (aa @ a :: as, bb @ b :: bs) =>
      if (a <= b) a :: merge(as, bb) else b :: merge(aa, bs)
  }

  /** original */
  def mergesort(xs: List[Int]): List[Int] = xs match {
    case x :: Nil => List(x)
    case Nil      => List()
    case _ =>
      val (half1, half2) = xs.splitAt(xs.length / 2)
      merge(mergesort(half1), mergesort(half2))
  }

  def mergesort_tup() = {
    val (numSplitter, numMerger) = (2, 2)
    val space: Space = new SequentialSpace

    // We place the array to be sorted in the tuple space
    val arr: Array[Int] = Array[Int](7, 6, 5, 4, 3, 2, 1)
    space.put("sort", arr, arr.length)

    0 to numSplitter foreach { i =>
      Future(merger(space, i))
    }
    0 to numMerger foreach { i =>
      Future(merger(space, i))
    }

    println("result:", space.queryS("result", classOf[Object]))

  }
  def merger(spc: Space, me: Int) = {
    while (true) {
      for {
        _ <- Try(spc.getS("lock"))
        r <- Try(spc.getS("sorted", classOf[Object], classOf[Int]))
      } yield {
        r
      }

    }
  }
}
