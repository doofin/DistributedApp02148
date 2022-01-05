package example.exercises.Jakub

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import example.exercises.Jakub.RGA.Operations._
import example.exercises.Jakub.RGA._
import example.exercises.Jakub.TextCommon.Event._
import example.exercises.Jakub.TextCommon._
import org.jspace._

import scala.io.StdIn.readLine
import scala.util.Random

object TextClient {
  def main(args: Array[String]): Unit = {
    println(s"You are $myID")
    println("COMMANDS: Either 'init' or 'join'")
    print(">> ")
    val command = readLine()
    command match {
      case "init" => initializeSession()
      case "join" =>
        print("Enter session ID: ")
        val sessionID = readLine()
        joinSession(sessionID)
      case _ => println(s"Invalid command '$command")
    }
  }

  val joinSpace = new RemoteSpace(spaceURL(JOIN_SPACE_ID))
  val myID = s"client-${Random.nextInt(1000)}"
  val myCRDT = new CRDT(myID)

  def initializeSession(): Unit = {
    // Ask for a new collaboration session
    joinSpace.put(START_SESSION, myID)

    // Receive session ID
    val (_, _, sessionID) = joinSpace.getS(SESSION, myID, classOf[String])
    println(s"Started a session: $sessionID")

    // Send initial document state
    val sessionSpace = new RemoteSpace(spaceURL(sessionID))

    println("Waiting for input...")
    readLine() // TODO

    write("000", sessionSpace)

    // Start inline editor
    workOn(sessionSpace)
  }

  def joinSession(sessionID: String): Unit = {
    // Try to join the session
    joinSpace.put(JOIN_SESSION, myID, sessionID)
    joinSpace.getS(SESSION, myID, sessionID) // TODO: INVALID_SESSION

    // Start working on remote document
    val sessionSpace = new RemoteSpace(spaceURL(sessionID))

    println("Waiting for input...")
    readLine() // TODO

    workOn(sessionSpace)
  }

  def workOn(space: Space): Unit = {
    new Listener(space).spawn()
    println(myCRDT.asString)

    val rand = new Random()

    for (i <- 1 to 20) {
      Thread.sleep(500 + rand.nextInt(500))
      if (rand.nextBoolean()) {
        write(s"$i", space)
        println(s"[A ] ${myCRDT.asString}")
      } else {
        if (myCRDT.vertices.nonEmpty) {
          backspace(space)
          println(s"[B ] ${myCRDT.asString}")
        }
      }
    }

    //    while (true) {
    //      print(">> ")
    //      val input = readLine()
    //      write(input, space)
    //      println(myCRDT.asString)
    //    }
  }

  class Listener(space: Space) extends Runnable {
    override def run(): Unit = {
      while (true) {
        val (_, _, op) = space.getS(EVENT, myID, classOf[Operation[String]])
        myCRDT.applyOperation(op)
        val t = op match {
          case _: Inserted[_] => "A"
          case _: Removed[_] => "B"
        }
        println(s"[R$t] ${myCRDT.asString}")
      }
    }
  }

  def write(str: String, space: Space): Unit = str.foreach(writeChar(_, space))

  def writeChar(str: Char, space: Space): Unit = {
    val event: Operation[String] = myCRDT.writeAtEnd(str.toString)

    // Notify others
    val (_, clients) = space.queryS(CLIENTS, classOf[Array[String]])

    // TODO: Save every event with some global tag, so that everyone who joins late
    // can replay the previous, unseen, messages
    //    space.put(EVENT, "history", event)

    clients.foreach(x => if (x != myID)
      space.put(EVENT, x, event)
    )
  }

  def backspace(space: Space): Unit = {
    val event: Operation[String] = myCRDT.backspace()

    // Notify others
    val (_, clients) = space.queryS(CLIENTS, classOf[Array[String]])

    clients.foreach(x => if (x != myID)
      space.put(EVENT, x, event)
    )
  }

}
