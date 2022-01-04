package example.exercises

import example.ScalaSpaces._

/** Exercise 2.6. Parallel merge sort */
object lec2_sort {

  def run = {

    mergesort(List(3, 1, 2, 4, 5))
  }

  def merge(x1: List[Int], x2: List[Int]): List[Int] = (x1, x2) match {
    case (Nil, x) => x
    case (x, Nil) => x
    case (aa@a :: as, bb@b :: bs) =>
      if (a <= b) a :: merge(as, bb) else b :: merge(aa, bs)
  }

  /** original */
  def mergesort(xs: List[Int]): List[Int] = xs match {
    case x :: Nil => List(x)
    case Nil => List()
    case _ =>
      val (half1, half2) = xs.splitAt(xs.length / 2)
      merge(mergesort(half1), mergesort(half2))
  }

}
