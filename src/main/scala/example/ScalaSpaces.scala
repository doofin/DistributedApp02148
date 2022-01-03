package example

import org.jspace._

import scala.jdk.CollectionConverters._
import scala.language.implicitConversions

object ScalaSpaces {
  object SeqView {
    def unapplySeq(tuple: Tuple): Option[Seq[Any]] = Some(Seq.from(tuple.asScala))
  }

  implicit class SpaceOps(space: Space) {
    //region getS

    def getS[A](q: Query[A]): A = {
      val Array(a) = space.get(q.convert)
      a.asInstanceOf[A]
    }

    def getS[A, B](q1: Query[A], q2: Query[B]): (A, B) = {
      val Array(a, b) = space.get(q1.convert, q2.convert)
      (a.asInstanceOf[A], b.asInstanceOf[B])
    }

    def getS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): (A, B, C) = {
      val Array(a, b, c) = space.get(q1.convert, q2.convert, q3.convert)
      (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
    }

    //endregion
    //region getpS

    def getpS[A](q: Query[A]): Option[A] =
      Option(space.getp(q.convert)).map {
        case Array(a) => a.asInstanceOf[A]
      }

    def getpS[A, B](q1: Query[A], q2: Query[B]): Option[(A, B)] =
      Option(space.getp(q1.convert, q2.convert)).map {
        case Array(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
      }

    def getpS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): Option[(A, B, C)] =
      Option(space.getp(q1.convert, q2.convert, q3.convert)).map {
        case Array(a, b, c) => (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
      }

    //endregion
    //region queryS

    def queryS[A](q: Query[A]): A = {
      val Array(a) = space.query(q.convert)
      a.asInstanceOf[A]
    }

    def queryS[A, B](q1: Query[A], q2: Query[B]): (A, B) = {
      val Array(a, b) = space.query(q1.convert, q2.convert)
      (a.asInstanceOf[A], b.asInstanceOf[B])
    }

    def queryS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): (A, B, C) = {
      val Array(a, b, c) = space.query(q1.convert, q2.convert, q3.convert)
      (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
    }

    //endregion
    //region querypS

    def querypS[A](q: Query[A]): Option[A] =
      Option(space.queryp(q.convert)).map {
        case Array(a) => a.asInstanceOf[A]
      }

    def querypS[A, B](q1: Query[A], q2: Query[B]): Option[(A, B)] =
      Option(space.queryp(q1.convert, q2.convert)).map {
        case Array(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
      }

    def querypS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): Option[(A, B, C)] =
      Option(space.queryp(q1.convert, q2.convert, q3.convert)).map {
        case Array(a, b, c) => (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
      }

    //endregion
    //region getAllS

    def getAllS[A](q: Query[A]): List[A] =
      space.getAll(q.convert).asScala.toList.map {
        case Array(a) => a.asInstanceOf[A]
      }

    def getAllS[A, B](q1: Query[A], q2: Query[B]): List[(A, B)] =
      space.getAll(q1.convert, q2.convert).asScala.toList.map {
        case Array(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
      }

    def getAllS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): List[(A, B, C)] =
      space.getAll(q1.convert, q2.convert, q3.convert).asScala.toList.map {
        case Array(a, b, c) => (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
      }

    //endregion
    //region queryAllS

    def queryAllS[A](q: Query[A]): List[A] =
      space.queryAll(q.convert).asScala.toList.map {
        case Array(a) => a.asInstanceOf[A]
      }

    def queryAllS[A, B](q1: Query[A], q2: Query[B]): List[(A, B)] =
      space.queryAll(q1.convert, q2.convert).asScala.toList.map {
        case Array(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
      }

    def queryAllS[A, B, C](q1: Query[A], q2: Query[B], q3: Query[C]): List[(A, B, C)] =
      space.queryAll(q1.convert, q2.convert, q3.convert).asScala.toList.map {
        case Array(a, b, c) => (a.asInstanceOf[A], b.asInstanceOf[B], c.asInstanceOf[C])
      }

    //endregion
  }
}

trait Query[A] {
  def convert: TemplateField
}

case class TypeQuery[A](clazz: Class[A]) extends Query[A] {
  override def convert: TemplateField = new FormalField(clazz)
}

case class ValueQuery[A](value: A) extends Query[A] {
  override def convert: TemplateField = new ActualField(value)
}

object Query {
  implicit def convType[A](clazz: Class[A]): TypeQuery[A] = TypeQuery(clazz)

  implicit def conv[A](a: A): ValueQuery[A] = ValueQuery(a)
}
