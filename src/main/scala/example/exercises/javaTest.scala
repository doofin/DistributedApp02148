package example.exercises

object javaTest {

  def main(args: Array[String]) = {
    val message = scala.io.StdIn.readLine("Enter 0 for server,1 for client ")

    message match {
      case "0" => chat1.Server.main(args)//chat0.Server.main(args)
      case _   => //chat1.Client.main(args) //chat0.Client.main(args)
    }

//
  }
}
