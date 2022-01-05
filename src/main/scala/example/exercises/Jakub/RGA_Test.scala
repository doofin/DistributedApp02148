package example.exercises.Jakub

import example.exercises.Jakub.RGA._

object RGA_Test {
  def main(args: Array[String]): Unit = {
    val alice = new CRDT("Alice")
    val bob = new CRDT("Bob")

    val printAlice = () => println(s"Alice at ${alice.clock}: '${alice.asString}'")
    val printBob = () => println(s"Bob at ${bob.clock}: '${bob.asString}'")

    // Alice writes 'Hello'
    val i1 = alice.writeAtEnd("Hello")
    printAlice()
    // Alice adds 'world' after 'Hello'
    val i2 = alice.writeAtEnd(" world")
    printAlice()

    // Bob sees that alice wrote 'Hello'
    bob.applyOperation(i1)
    printBob()
    // Bob adds 'you' after 'Hello'
    val i3 = bob.writeAtEnd(" you")
    printBob()
    // Bob sees that Alice wrote 'world'
    bob.applyOperation(i2)
    printBob()

    // Alice sees that Bob wrote 'you'
    alice.applyOperation(i3)
    printAlice()

    // Bob writes 'bbb'
    val i5 = bob.writeAtEnd(" bbb")
    printBob()
    // but then deletes it
    val i6 = bob.backspace()
    printBob()

    // Alice sees that Bob wrote 'bbb'
    alice.applyOperation(i5)
    printAlice()
    // and decides to add 'aaa'
    val i7 = alice.writeAtEnd(" aaa")
    printAlice()
    // and only after that she sees that Bob removed 'bbb'
    alice.applyOperation(i6)
    printAlice()

    // Bob sees that Alice replied to 'bbb' before it was removed
    bob.applyOperation(i7)
    printAlice()

    println("END")
  }
}
