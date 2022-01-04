package example.exercises

import example.ScalaSpaces._
import org.jspace.{SequentialSpace, Tuple}

//https://github.com/pSpaces/jSpace-examples/blob/master/tutorial/fridge-0/Fridge_0.java
object lec1_fridge {
  def run(): Unit = {
    val tuple@SeqView(a, b) = new Tuple("milk", 1)
    println(s"The fields of $tuple are $a and $b")

    // Creating a space.
    val fridge = new SequentialSpace

    // Adding tuples.
    fridge.put("coffee", 1)
    fridge.put("clean kitchen")
    fridge.put("butter", 2)
    fridge.put("milk", 3)

    fridge.querypS("clean kitchen") foreach { _ => println("We need to clean the kitchen") }

    fridge.getpS("coffee", classOf[Integer]) foreach { case (_, x) => println(s"Coffee : $x") }

    // Looking for a tuple with pattern matching.NPE here!
    fridge.querypS("milk", classOf[Integer]).foreach { case (_, numberOfBottles) =>
      // Updating a tuple
      if (numberOfBottles <= 10) {
        println("We plan to buy milk, but not enough...")

        fridge.getpS("milk", classOf[Integer]) foreach {
          case (_, newNumber) => fridge.put("milk", newNumber + 1)
        }
      }
    }

    // Check if an item is in the list already and update it, else add this amount to the list.
    addItemAndAmount("bread", 2)
    addItemAndAmount("coffee", 1)

    def addItemAndAmount(itemName: String, amount: Int) =
      fridge.getpS(itemName, classOf[Integer]) match {
        case None => fridge.put(itemName, amount)
        case Some((_, num)) => fridge.put(itemName, num + amount)
      }

    println("Items to buy: ")
    fridge.queryAllS(classOf[String], classOf[Integer]) foreach println
  }
}
