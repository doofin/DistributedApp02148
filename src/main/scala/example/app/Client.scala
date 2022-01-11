package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import Common.Event._
import Common._
import example.app.CRDT.Operations._
import example.app.CRDT._
import org.jspace._

import scala.util.Random

class Client(onUpdate: String => Unit) {
  val lobby = new RemoteSpace(spaceURL(JOIN_SPACE_ID))
  val clientID = Random.nextInt(1000).toString
  val crdt = new CRDT(clientID)
  var room: Option[Space] = None

  /**Ask for a new collaboration session*/
  def newSession(): String = {
    lobby.put(START_SESSION, clientID)

    // Receive session ID
    val sessionID = lobby.getS(SESSION, clientID, classOf[String])._3
    println(s"Started a session: $sessionID")

    // Create remote space
    val space = new RemoteSpace(spaceURL(sessionID))
    room = Some(space)

    // Spawn event listener
    new Listener(space).spawn()

    // Return session ID
    sessionID
  }

  /**Try to join the session*/
  def joinSession(sessionID: String): Unit = {
    lobby.put(JOIN_SESSION, clientID, sessionID)
    if (verifySession(lobby, sessionID)) {

      println(s"Joined a session: $sessionID")

      // Create remote space

      val sessionSpace = new RemoteSpace(spaceURL(sessionID))
      room = Some(sessionSpace)

      // Spawn event listener
      new Listener(sessionSpace).spawn()
    } else println(s"invalid  session")

  }

  class Listener(space: Space) extends Runnable {
    override def run(): Unit = {
      while (true) {
        val (_, _, op) = space.getS(EVENT, clientID, classOf[Operation[String]])
        crdt.applyOperation(op)
        onUpdate(crdt.asString)
      }
    }
  }

  def writeChar(current: Int, str: Char): Unit =
    notify(crdt.writeAfter(current, str.toString))

  def deleteAt(at: Int): Unit = notify(crdt.deleteAt(at))

  private def notify(event: Operation[String]): Unit = {
    room match {
      case None =>
      case Some(space) =>
        val (_, clients) = space.queryS(CLIENTS, classOf[Array[String]])

        // TODO: Save every event with some global tag, so that everyone who joins late
        // can replay the previous, unseen, messages
        // space.put(EVENT, "history", event)

        clients.foreach(
          x =>
            if (x != clientID)
              space.put(EVENT, x, event)
        )
    }
  }

  // TODO: INVALID_SESSION
  private def verifySession(lobby: RemoteSpace, sessionID: String) = {
//    lobby.getS(SESSION, clientID, sessionID)
    lobby.querypS(SESSION, clientID, sessionID).nonEmpty
  }
}
