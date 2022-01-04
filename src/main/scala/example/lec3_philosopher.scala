package example

import org.jspace.{ActualField, RemoteSpace, SequentialSpace}


import java.io.IOException

object lec3_philosopher {
  var N = 10
  var me =0
  var port = 31145
  var host = "localhost"



  def main(args: Array[String]): Unit = {
    if (args.length < 2 || args.length > 4) {
      System.out.println("Wrong number of arguments")
      System.out.println("Usage: java -jar run main.jar <number of philosopers> <my id> [host] [port]")
      return
    }
    N = args(0).toInt

    if (N <= 1) {
      System.out.println("Wrong number of philosophers. Must be at least 2.")
      return
    }

    me = args(1).toInt
    if (me < 0 || me >= N) {
      System.out.println("Wrong philosopher id. Must be between 0 and " + (N - 1))
      return
    }

    if (args.length >= 3) host = args(2)

    if (args.length >= 4) port = args(3).toInt



    try {
      println("philosopher sitting down")
      val url = "tcp://" + host + ":" +port + "/board?conn"
      val board = new RemoteSpace(url)
      val philosopher = new philosopher(board,me,lec3_philosopher.N)
      philosopher.run
    } catch {
      case e: InterruptedException =>
    }

  }



  // philosopher defines the behaviour of a philosopher.
  class philosopher(val board: RemoteSpace, var me: Int,val numberOfPhilosopher :Int) extends Runnable { // We define variables to identify the left and right forks.
    val left = me
    val right = (left + 1) % numberOfPhilosopher


    def run(): Unit = { // The philosopher enters his endless life cycle.

      while ( {
        true
      }) try { // Wait until the left fork is ready (get the corresponding tuple).
        //System.out.println("Philosopher " + me + " waiting for a lock")
        board.get(new ActualField("lock"))
        //System.out.println("Philosopher " + me + " got a lock")

        board.get(new ActualField("fork"), new ActualField(left))
        System.out.println("Philosopher " + me + " got left fork")
        // Wait until the right fork is ready (get the corresponding tuple).
        board.get(new ActualField("fork"), new ActualField(right))
        System.out.println("Philosopher " + me + " got right fork")
        // Lunch time.
        System.out.println("Philosopher " + me + " is eating...")
        // Return the forks (put the corresponding tuples).
        board.put("fork", left)
        board.put("fork", right)
        System.out.println("Philosopher " + me + " put both forks on the table")

        //println("returning lock")
        board.put("lock")
      } catch {
        case e: InterruptedException =>

      }
    }
  }



}

