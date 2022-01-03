package example

import org.jspace.FormalField
import org.jspace.SequentialSpace
import org.jspace.Space

object Main {
  def main(args: Array[String]) = {
    import org.jspace.FormalField
    import org.jspace.SequentialSpace
    import org.jspace.Space
    val inbox = new SequentialSpace

    inbox.put("Hello World!")
    val tuple = inbox.get(new FormalField(classOf[String]))
    System.out.println(tuple(0))
    val harald = new HaraldTestingArea
    harald.runAll()
  }

}
