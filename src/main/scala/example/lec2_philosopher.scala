package example

import example.ScalaSpaces._
import org.jspace.SequentialSpace

object lec2_philosopher { // N defines the number of philosophers.
  val N = 10

  def run(): Unit = {
    val board = new SequentialSpace
    new Thread(new waiter(board)).start()

    for (i <- 0 until N) {
      new Thread(new philosopher(board, i)).start()
    }

    try board.queryS("done") // will never succeed
    catch {
      case _: InterruptedException =>
    }
  }
}

// waiter prepares the board with forks.
class waiter(val board: SequentialSpace) extends Runnable {
  override def run(): Unit = {
    println("Waiter putting forks on the table...")
    for (i <- 0 until lec2_philosopher.N) {
      try {
        board.put("fork", i)
        println(s"Waiter put fork $i on the table.")
      } catch {
        case _: InterruptedException =>
      }
    }
    println("Waiter done.")
  }
}


// philosopher defines the behaviour of a philosopher.
class philosopher(val board: SequentialSpace, var me: Int) extends Runnable {
  // We define variables to identify the left and right forks.
  val left: Int = me
  val right: Int = (left + 1) % lec2_philosopher.N

  override def run(): Unit = { // The philosopher enters his endless life cycle.
    while (true) {
      try {
        // Wait until the left fork is ready (get the corresponding tuple).
        board.getS("fork", left)
        println(s"Philosopher $me got left fork")

        // Wait until the right fork is ready (get the corresponding tuple).
        board.getS("fork", right)
        println(s"Philosopher $me got right fork")

        // Lunch time.
        println(s"Philosopher $me is eating...")

        // Return the forks (put the corresponding tuples).
        board.put("fork", left)
        board.put("fork", right)
        println(s"Philosopher $me put both forks on the table")
      } catch {
        case _: InterruptedException =>
      }
    }
  }
}


