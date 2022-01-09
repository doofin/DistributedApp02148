package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import example.exercises.Jakub.RGA.Operations._
import example.exercises.Jakub.RGA._
import example.exercises.Jakub.TextCommon.Event._
import example.exercises.Jakub.TextCommon._
import org.jspace._

import scala.util.Random

class Client {
  val joinSpace = new RemoteSpace(spaceURL(JOIN_SPACE_ID))
  val myID = s"client-${Random.nextInt(1000)}"
  val myCRDT = new CRDT(myID)

  def initializeSession(): String = {
    // Ask for a new collaboration session
    joinSpace.put(START_SESSION, myID)

    // Receive session ID
    val sessionID = joinSpace.getS(SESSION, myID, classOf[String])._3
    println(s"Started a session: $sessionID")

    // Create remote space
    val sessionSpace = new RemoteSpace(spaceURL(sessionID))

    // Spawn event listener
    new Listener(sessionSpace).spawn()

    // Return session ID
    sessionID
  }

  def joinSession(sessionID: String): Unit = {
    // Try to join the session
    joinSpace.put(JOIN_SESSION, myID, sessionID)
    joinSpace.getS(SESSION, myID, sessionID) // TODO: INVALID_SESSION
    println(s"Joined a session: $sessionID")

    // Create remote space
    val sessionSpace = new RemoteSpace(spaceURL(sessionID))

    // Spawn event listener
    new Listener(sessionSpace).spawn()
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
