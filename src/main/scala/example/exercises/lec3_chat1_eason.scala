package example.exercises

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import example.ScalaSpaces._

import scala.concurrent.duration.Duration

/**chat room with lobby and dual printing*/
object lec3_chat1_eason {
  def main(args: Array[String]): Unit = {
    val spac = chat1.Client.main(args)

    val f1 = Future(while (true) {
      val message = readLine("input msg\n")
      spac.put("hi ", message)
    })

    val f2 = Future(while (true) {
      println(spac.queryS(classOf[String], classOf[String]))
    })

    scala.concurrent.Await.result(f2, Duration.Inf)
  }
}
