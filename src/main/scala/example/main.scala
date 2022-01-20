package example

import example.app.{Editor, TextServer}

object main {

  def main(args: Array[String]): Unit = {
    args.head match {
      case "client" =>
        new Editor
      case "server" =>
        TextServer.run()
    }
  }
}
