package example {

  import org.jspace.{SequentialSpace, SpaceRepository}

  object lec3_chatServer{
    var port = 31145


    def main(args:Array[String]) = {
      //setup board
      val chat = new SequentialSpace()

      //Setup connection
      println("Setting up connection")
      val url = "tcp://localhost:" + port + "/?conn"
      val repository = new SpaceRepository
      repository.add("chat",chat )
      repository.addGate(url)

      import org.jspace.FormalField
      // Keep reading chat messages and printing them // Keep reading chat messages and printing them
      println("connection setup, ready to chat!")
      while (true) {
        var t = chat.get(new FormalField(classOf[String]), new FormalField(classOf[String]))
        System.out.println(t(0) + ":" + t(1))
      }



    }
  }

}
