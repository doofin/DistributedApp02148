package example

import example.app.Editor
import scala.io.StdIn.readLine
import example.app.TextServer

object main {

  def main(args: Array[String]): Unit = {
    args.head match {
      case "0" =>
        new Editor
        readLine("Press ENTER to stop the client\n")
      case "1" =>
        TextServer.run()
    }
  }
}
