package example.exercises.Haraldexercises
import org.jspace._
object lec3_butler {
  val N = 10
  val port = 31145

  def main(args: Array[String]): Unit = {
    val board = new SequentialSpace

    // Setup global board
    val url = "tcp://localhost:" + port + "/?conn"
    val repository = new SpaceRepository
    repository.addGate(url)
    repository.add("board", board)

    //Waiter prepare board
    new Thread(new waiter(board, N)).start()

    try board.query(new ActualField("done")) // will never succeed

    catch {
      case e: InterruptedException =>

    }

  }


  // waiter prepares the board with forks.
  class waiter(val board: SequentialSpace, val amount: Int) extends Runnable {

    def run(): Unit = {
      System.out.println("Waiter putting forks on the table...")
      for (i <- 0 until (amount / 2)) {
        board.put("lock")
        System.out.println("Waiter put lock " + i + " on the table.")
      }
      for (i <- 0 until amount) {
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
}
