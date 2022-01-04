package example

import org.jspace.RemoteSpace

object lec3_chatClient {
  var port = 31145
  def main(args: Array[String]) ={
    val server = new RemoteSpace("tcp://localhost:" + port + "/?conn)")
    while(true) {
      val message = scala.io.StdIn.readLine("Enter message> ")
      server.put("Harald",message)
    }
  }
}
