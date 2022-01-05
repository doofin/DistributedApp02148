package example.exercises.Jakub

import example.exercises.Jakub.RGA.Operations._
import example.exercises.Jakub.TextCommon.Document

import scala.collection.mutable

// https://bartoszsypytkowski.com/operation-based-crdts-arrays-1/
object RGA {
  type Time = Integer
  type SiteId = String
  type VPtr = (Time, SiteId)
  type Vertex[A] = (VPtr, Option[A])

  trait Operation[A]

  object Operations {
    type Position = VPtr

    case class Inserted[A](predecessor: VPtr, ptr: VPtr, value: A) extends Operation[A]

    case class Removed[A](ptr: Position) extends Operation[A]
  }

  class CRDT(site: SiteId) extends Document {
    type V = Vertex[String]
    val root: V = ((0, ""), None)
    var vertices: mutable.ArrayBuffer[V] = mutable.ArrayBuffer(root)

    var clock: Time = 0

    def shift(offset: Int, ptr: VPtr): Int = {
      if (offset >= vertices.length) offset // append at the end
      else {
        val (successor, _) = vertices(offset)
        if (successor._1 < ptr._1 || (successor._1 == ptr._1 && successor._2 < ptr._2))
          offset
        else shift(offset + 1, ptr) // move insertion point to the right
      }
    }

    def applyInserted(inserted: Inserted[String]): Unit = {
      val Inserted(predecessor, ptr, value) = inserted
      // find index where predecessor vertex can be found
      val predecessorIdx = vertices.indexWhere(x => x._1 == predecessor)
      // adjust index where new vertex is to be inserted
      val insertIdx = shift(predecessorIdx + 1, ptr)
      vertices.insert(insertIdx, (ptr, Some(value)))
      // update RGA to store the highest observed sequence number
      // (a.k.a. Lamport timestamp)
      clock = math.max(ptr._1, clock)
    }

    def applyRemoved(event: Removed[String]): Unit = {
      val Removed(ptr) = event
      // find index where removed vertex can be found and tombstone it
      val index = vertices.indexWhere(x => x._1 == ptr)
      val (at, _) = vertices(index)
      vertices.update(index, (at, None))
    }

    def writeAtEnd(string: String): Inserted[String] = {
      clock += 1
      val event = Inserted(vertices.last._1, (clock, site), string)
      applyInserted(event)
      event
    }

    def backspace(): Removed[String] = {
      clock += 1
      val event = Removed[String](vertices.last._1)
      applyRemoved(event)
      event
    }

    def asString: String = vertices.map(x => x._2.getOrElse("")).mkString("")
  }
}
