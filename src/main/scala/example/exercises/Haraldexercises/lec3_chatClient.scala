package example.exercises.Haraldexercises

import org.jspace.RemoteSpace

import scala.io.StdIn.readLine

object lec3_chatClient {
  var port = 31145

  def main(args: Array[String]) = {
    try {
      val url = "tcp://localhost:" + port + "/chat?conn"
      while (true) {
        val chat = new RemoteSpace(url)


        println("enter message")
        val message = readLine()
        println("ready")
        chat.put("Harald", message)
        println("succeed")
      }
    }
  }
}
