package example

import org.jspace.RemoteSpace

import scala.io.StdIn.readLine


object lec3_chatClient {
  var port = 31145
  def main(args: Array[String]) ={

    val url ="tcp://localhost:" + port +"/chat?conn)"
    /*
    val chat = new RemoteSpace(url)
    while(true) {
        println("enter message")
        val message = readLine()
        chat.put("Harald",message)
    }
     */
    while(true) {
      val chat = new RemoteSpace(url)
      println("enter message")
      val message = readLine()
      chat.put("Harald",message)
    }
  }
}
