package example

import org.jspace._
import scala.jdk.CollectionConverters._
object lec1_fridge {
  def run = {
    val tuple: Tuple = new Tuple("milk", 1)
    System.out.println("We just created tuple")
    System.out.println(tuple)

    System.out.println("The fields of ")
    System.out.println(tuple)
    System.out.println(" are ")
    System.out.println(tuple.getElementAt(0))
    System.out.println(" and ")
    System.out.println(tuple.getElementAt(1))

    // Creating a space.
    val fridge: Space = new SequentialSpace // or FIFOSpace, LIFOSpace

    // Adding tuples.
    fridge.put("coffee", 1)
    fridge.put("coffee", 1)
    fridge.put("clean kitchen")
    fridge.put("butter", 2)
    fridge.put("milk",3)

    // Looking for a tuple.
    val obj1 = fridge.queryp(new ActualField("clean kitchen"))
    if (obj1 != null) System.out.println("We need to clean the kitchen")

    // Removing a tuple.
    val obj2 = fridge.getp(new ActualField("clean kitchen"))
    if (obj2 != null) System.out.println("Cleaning...")

    // Looking for a tuple with pattern matching.NPE here!
    var numberOfBottles = 0
    val objs3 =
        fridge.queryp(new ActualField("milk"), new FormalField(classOf[Integer]))
    if (objs3 != null)   numberOfBottles = objs3(1).asInstanceOf[Integer]


    // Updating a tuple.
    if (objs3 != null && numberOfBottles <= 10) {
      System.out.println("We plan to buy milk, but not enough...")
      val objs4 =
        fridge.getp(new ActualField("milk"), new FormalField(classOf[Integer]))
      numberOfBottles = objs4(1).asInstanceOf[Integer]
      fridge.put("milk", numberOfBottles + 1)
    }
   // Check if an item is in the list already and update it, else add this amount to the list.
    addItemAndAmount("bread",2)
    addItemAndAmount("coffee",1)
    def addItemAndAmount(itemName:String, amount:Int ) {
      val objs5 = fridge.queryp(new ActualField(itemName), new FormalField(classOf[Integer]))
      if (objs5 != null) {
        val objs6 = fridge.getp(new ActualField(itemName), new FormalField(classOf[Integer]))
        val numberOfBread = objs6(1).asInstanceOf[Integer]
        fridge.put(itemName, numberOfBread + amount)
      } else {
        fridge.put(itemName, amount)
      }
    }


    val groceryList = fridge.queryAll(
      new FormalField(classOf[String]),
      new FormalField(classOf[Integer])
    )

    System.out.println("Items to buy: ")

    for (obj <- groceryList.asScala) {
      println(obj(0),obj(1))
    }
  }
}
