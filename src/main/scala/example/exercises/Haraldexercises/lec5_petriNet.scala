package example.exercises.Haraldexercises


import org.jspace.{ActualField, SequentialSpace, Space}





object lec5_petriNet {
  def run() ={
   println("hello")
    val space = new SequentialSpace


    new Thread(new butler(space)).start
    new Thread(new tasks(space,List("s2","s1"),List("s3"),"B")).start()
    new Thread(new tasks(space,List("s1"),List("s1"),"A")).start()
    new Thread(new tasks(space,List("s2"),List("s2"),"C")).start()
    new Thread(new tasks(space,List("s3"),List("s1","s2"),"D")).start()


  }
  class butler(space:SequentialSpace) extends Runnable{
      def run() = {
        println("placing locks")
        space.put(new ActualField("lock"), new ActualField("s2"))
        space.put(new ActualField("lock"), new ActualField("s1"))
        println("butler done!")
      }
  }
  class tasks(space: SequentialSpace,s0:List[String],s1:List[String],me :String) extends Runnable{
     def run() = {
      while (true) {
        Thread.sleep(1000)
        println("task " + me + " ready" )
        s0.foreach(x=> {
          println(me + " is looking for " +x)
          space.getp(new ActualField("lock"),new ActualField(x))
          println(me + " Got "+ x)
        })
        println(me +" got lock. Doing task...")
        Thread.sleep(1000)
        println(me + " Task done! placing lock in next position")
        s1.foreach(x=> space.put("lock",x))


      }
     }

  }

}
