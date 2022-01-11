package example.app

import example.app.CRDT.Operations._
import example.app.CRDT._

import scala.collection.mutable

// https://bartoszsypytkowski.com/operation-based-crdts-arrays-1/
object CRDT {
  type Time = Integer
  type ClientID = String
  type VPtr = (Time, ClientID)
  type Vertex[A] = (VPtr, Option[A])

  trait Operation[A]

  object Operations {
    case class Inserted[A](predecessor: VPtr, ptr: VPtr, value: A) extends Operation[A]

    case class Removed[A](ptr: VPtr) extends Operation[A]
  }
}

// TODO: Guard against concurrent access, otherwise this happens with `vertices`:
// Exception in thread "Thread-2" java.util.ConcurrentModificationException: mutation occurred during iteration
class CRDT(site: ClientID) {
  type V = Vertex[String]
  val root: V = ((0, ""), None)
  var vertices: mutable.ArrayBuffer[V] = mutable.ArrayBuffer(root)

  var clock: Time = 0

  private[this] def ptrLt(p1: VPtr, p2: VPtr): Boolean = p1._1 < p2._1 || p1._2 < p2._2

  private def shift(offset: Int, ptr: VPtr): Int = {
    if (offset >= vertices.length) offset // append at the end
    else {
      val (successor, _) = vertices(offset)
      if (ptrLt(successor, ptr))
        offset
      else shift(offset + 1, ptr) // move insertion point to the right
    }
  }

  def applyOperation(op: Operation[String]): Unit = {
    op match {
      case i: Inserted[String] => applyInserted(i)
      case r: Removed[String] => applyRemoved(r)
    }
  }

  private def applyInserted(inserted: Inserted[String]): Unit = {
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

  private def applyRemoved(event: Removed[String]): Unit = {
    val Removed(ptr) = event
    // find index where removed vertex can be found and tombstone it
    val index = vertices.indexWhere(x => x._1 == ptr)
    val (at, _) = vertices(index)
    vertices.update(index, (at, None))
  }

  def ptrToCharAt(index: Int): Option[VPtr] = {
    var counter = -1
    for ((ptr, c) <- vertices.tail) {
      c match {
        case None =>
        case Some(_) => counter += 1
      }
      if (index == counter) return Some(ptr)
    }
    None
  }

  def writeAfter(offset: Int, string: String): Inserted[String] = {
    val pred = offset match {
      case 0 => Some(root._1)
      case _ => ptrToCharAt(offset - 1)
    }

    val ptr = pred.getOrElse(vertices.last._1)

    val event = Inserted(ptr, (clock + 1, site), string)
    applyInserted(event)
    event
  }

  def deleteAt(offset: Int): Removed[String] = {
    val ptr = ptrToCharAt(offset - 1).getOrElse(vertices.last._1)
    val event = Removed[String](ptr)
    applyRemoved(event)
    event
  }

  def writeAtEnd(string: String): Inserted[String] = writeAfter(Integer.MAX_VALUE, string)

  def deleteLast(): Removed[String] = deleteAt(Integer.MAX_VALUE)

  def asString: String = vertices.map(x => x._2.getOrElse("")).mkString("")
}