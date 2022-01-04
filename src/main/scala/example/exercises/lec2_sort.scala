package example.exercises

import example.ScalaSpaces._
import example.Utils._
import org.jspace._

import scala.util.control.Breaks._

/** Exercise 2.6. Parallel merge sort */
object lec2_sort {
  def run: Unit = {
    val bigList: List[Int] = (10000 until 0 by (-1)).toList

    val elapsedSeq = benchmark {
      mergesort(bigList)
    }

    println(s"Sequential done in ${elapsedSeq}s")

    val elapsedPar = benchmark {
      par_mergesort(bigList)
    }

    println(s"Parallel done in ${elapsedPar}s")
  }

  def merge[T](x1: List[T], x2: List[T])(implicit order: Ordering[T]): List[T] = (x1, x2) match {
    case (Nil, x) => x
    case (x, Nil) => x
    case (aa@a :: as, bb@b :: bs) =>
      if (order.lteq(a, b)) a :: merge(as, bb) else b :: merge(aa, bs)
  }

  /** original */
  def mergesort[T](xs: List[T])(implicit order: Ordering[T]): List[T] = xs match {
    case Nil => Nil
    case x@_ :: Nil => x
    case _ =>
      val (half1, half2) = xs.splitAt(xs.length / 2)
      merge(mergesort(half1), mergesort(half2))
  }

  /** parallel */
  class Splitter[T](space: Space) extends Runnable {
    override def run(): Unit = {
      while (!Thread.currentThread().isInterrupted) {
        breakable {
          val lst = space.getS("sort", classOf[List[T]])._2

          lst match {
            case Nil => break // continue
            case x :: Nil => space.put("sorted", List(x))
            case lst =>
              val (a, b) = lst.splitAt(lst.length / 2)
              space.put("sort", a)
              space.put("sort", b)
          }
        }
      }
    }
  }

  class Merger[T](space: Space, resultLen: Int)(implicit order: Ordering[T]) extends Runnable {
    override def run(): Unit = {
      while (!Thread.currentThread().isInterrupted) {
        breakable {
          space.getS("lock")
          space.getpS("sorted", classOf[List[T]]) match {
            case None =>
              space.put("lock")
              break
            case Some((_, l1)) =>
              space.getpS("sorted", classOf[List[T]]) match {
                case None =>
                  space.put("lock")
                  space.put("sorted", l1)
                  break
                case Some((_, l2)) =>
                  space.put("lock")
                  val merged = merge(l1, l2)
                  if (merged.length == resultLen) {
                    space.put("result", merged)
                  } else {
                    space.put("sorted", merged)
                  }
              }
          }
        }
      }
    }
  }

  def par_mergesort[T](xs: List[T])(implicit order: Ordering[T]): List[T] = {
    val space = new SequentialSpace()

    space.put("sort", xs)
    space.put("lock")

    val splitters = for (_ <- 1 to 4) yield new Splitter(space).spawn()
    val mergers = for (_ <- 1 to 4) yield new Merger(space, xs.length).spawn()

    val result = space.getS("result", classOf[List[T]])._2

    for (s <- splitters ++ mergers) {
      s.interrupt()
    }

    result
  }

}
