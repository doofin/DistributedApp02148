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
    // Alice adds '!!!' after 'world'
    val i21 = alice.writeAtEnd(" !!!")
    printAlice()

    // Bob sees that alice wrote 'Hello'
    bob.applyOperation(i1)
    printBob()
    // Bob adds 'there' after 'Hello'
    val i3 = bob.writeAtEnd(" there")
    printBob()
    // Bob sees that Alice wrote 'world'
    bob.applyOperation(i2)
    printBob()
    // Bob sees that Alice wrote '!!!!'
    bob.applyOperation(i21)
    printBob()

    // Alice sees that Bob wrote 'there'
    alice.applyOperation(i3)
    printAlice()

    // Bob writes his name
    val i5 = bob.writeAtEnd(" I am Bob")
    printBob()
    // but then deletes it
    val i6 = bob.backspace()
    printBob()

    // Alice sees that Bob wrote his name
    alice.applyOperation(i5)
    printAlice()
    // and decides to add her name
    val i7 = alice.writeAtEnd(" I am Alice")
    printAlice()
    // and only after that she sees that Bob removed his name
    alice.applyOperation(i6)
    printAlice()

    // Bob sees that Alice replied to his name before it was removed
    bob.applyOperation(i7)
    printBob()

    println("END")
    printAlice()
    printBob()
  }
}
