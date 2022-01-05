package example.exercises.Haraldexercises


import org.jspace.{ActualField, SequentialSpace}





object lec5_petriNet {
  def run():Unit ={
   println("hello")
    val space = new SequentialSpace()


    new butler(space).run()

    new Thread(new tasks(space,List(1,2),List(3),"B")).start()
    new Thread(new tasks(space,List(1),List(1),"A")).start()
    new Thread(new tasks(space,List(2),List(2),"C")).start()
    new Thread(new tasks(space,List(3),List(1,2),"D")).start()



  }
  class butler(space:SequentialSpace) extends Runnable{
      def run():Unit = {
        println("placing locks")
        space.put(1)
        space.put(2)
        println("butler done!")
      }
  }
  class tasks(space: SequentialSpace,s0:List[Int],s1:List[Int],me:String) extends Runnable{
     def run():Unit = {
      while (true) {
        println(me + " Ready to work. waiting for input ...")
        s0.foreach(x=>{
           println(me + " waiting for " + x)
           space.get(new ActualField(x))
          println(me + " got " + x)

        })
        s1.foreach({
          x=> space.put(x:Int)
            println(me + " placing back "+x)
        })


      }
     }

  }

}
