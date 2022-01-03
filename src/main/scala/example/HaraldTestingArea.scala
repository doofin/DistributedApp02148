package example

import org.jspace.{ActualField, FormalField, SequentialSpace, Space, Template}

class HaraldTestingArea {
  def runAll(): Unit = {
    hello()
    mathTesting()
    val time = Timer
    //time.main(4)
    tublesTesting()
   }
  def hello():Unit = {
      val inputString = "Hello World"
      System.out.println(inputString)
  }
  def mathTesting():Unit ={
      val one = 1
      val result = addOne(one)
      System.out.println(result)
      val result2 = square(2)
      System.out.println(result2)

  }
 def addOne(x: Int):Int = x+1
 def square(x:Int):Int = x*x

  object Timer {
    def oncePerSecond(callback: () => Unit,number:Int): Unit = {
      var count=number
      while (count>0) { callback();count-=1; Thread sleep 1000 }
    }
    def timeFlies(): Unit = {
      println("time flies like an arrow... ")
    }
    def main(x:Int): Unit = {
      oncePerSecond(timeFlies,x)
    }
  }
 def tublesTesting() :Unit = {
   val queue = new SequentialSpace()
   queue.put("Hello world !!!",1)
   queue.put("Hello world !!!",2)

   var x = true
   while (x)
   {
     val result = queue.getp(new FormalField(classOf[String]), new FormalField(classOf[Integer]))

     if (result != null) {
       println(result(0), result(1))

     } else {
       x = false
     }
   }
   print("no more items found")

 }
}
