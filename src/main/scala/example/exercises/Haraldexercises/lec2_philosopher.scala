package example.exercises.Haraldexercises

import org.jspace.{ActualField, SequentialSpace}

object lec2_philosopher { // N defines the number of philosophers.
  val N = 10

  def run(): Unit = {

    val board = new SequentialSpace
    new Thread(new waiter(board)).start()

    for (i <- 0 until N) {
      System.out.println("setting up philosopher " + i)
      new Thread(new philosopher(board, i)).start()
    }


    try board.query(new ActualField("done")) // will never succeed

    catch {
      case e: InterruptedException =>

    }
  }


  // waiter prepares the board with forks.
  class waiter(val board: SequentialSpace) extends Runnable {

    def run(): Unit = {
      System.out.println("Waiter putting forks on the table...")
      for (i <- 0 until (lec2_philosopher.N / 2 - 1)) {
        board.put("lock")
        System.out.println("Waiter put lock " + i + " on the table.")
      }
      for (i <- 0 until lec2_philosopher.N) {
        try {
          board.put("fork", i)
          System.out.println("Waiter put fork " + i + " on the table.")
        } catch {
          case e: InterruptedException =>

        }

      }

      System.out.println("Waiter done.")
    }
  }


  // philosopher defines the behaviour of a philosopher.
  class philosopher(val board: SequentialSpace, var me: Int) extends Runnable { // We define variables to identify the left and right forks.
    val left = me
    val right = (left + 1) % lec2_philosopher.N


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
