package example

import org.jspace._
import scala.jdk.CollectionConverters._

//https://github.com/pSpaces/jSpace-examples/blob/master/tutorial/fridge-0/Fridge_0.java
object lec1_fridge {
  def run = {
    val tuple: Tuple = new Tuple("milk", 1)
//    System.out.println("We just created tuple")
    System.out.println(tuple)
//
//    System.out.println("The fields of ")
//    System.out.println(tuple)
//    System.out.println(" are ")
//    System.out.println(tuple.getElementAt(0))
//    System.out.println(" and ")
//    System.out.println(tuple.getElementAt(1))

    // Creating a space.
    val fridge: Space = new SequentialSpace // or FIFOSpace, LIFOSpace

    // Adding tuples.
    fridge.put("coffee", 1)
    fridge.put("coffee", 1)
    fridge.put("clean kitchen")
    fridge.put("butter", 2)

    // Looking for a tuple.
    val obj1 = fridge.queryp(new ActualField("clean kitchen"))
    if (obj1 != null) System.out.println("We need to clean the kitchen")

    // Removing a tuple.
    val obj2 = fridge.getp(new ActualField("clean kitchen"))
    if (obj2 != null) System.out.println("Cleaning...")

    // Looking for a tuple with pattern matching.NPE here!
    var numberOfBottles = 0
    val objs3 =
      fridge.queryp(new ActualField("milk"), new FormalField(classOf[Int]))
    numberOfBottles = objs3(1).asInstanceOf[Int]

    // Updating a tuple.
    if (objs3 != null && numberOfBottles <= 10) {
      System.out.println("We plan to buy milk, but not enough...")
      val objs4 =
        fridge.getp(new ActualField("milk"), new FormalField(classOf[Integer]))
      numberOfBottles = objs4(1).asInstanceOf[Integer]
      fridge.put("milk", numberOfBottles + 1)
    }

    val groceryList = fridge.queryAll(
      new FormalField(classOf[String]),
      new FormalField(classOf[Integer])
    )

    System.out.println("Items to buy: ")

    for (obj <- groceryList.asScala) {
      println(obj)
    }
  }
}
