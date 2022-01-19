package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import Common.Event._
import Common._
import example.app.CRDT.Operations._
import example.app.CRDT._
import org.jspace
import org.jspace._

import javax.swing.JOptionPane
import scala.util.Random

class Client(onUpdate: String => Unit) {
  val lobby = new RemoteSpace(spaceURL(JOIN_SPACE_ID))

  val clientID: ClientID = getID().toString
  val crdt = new CRDT(clientID)
  var room: Option[Space] = None

  /** Ask for a new collaboration session */
  def newSession(): String = {
    clear()
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

  private def process(event: Operation[String]): Unit = {
    crdt.applyOperation(event)
    onUpdate(crdt.asString)
  }

  def getID(): Int = {
    lobby.put(GENERATE_NEW_ID)
    val (_, id) = lobby.getS(NEWID, classOf[Integer])

    id.toInt
  }

  def clear(): Unit = {
    crdt.clear()
    onUpdate(crdt.asString)
  }

  /** Try to join the session */
  def joinSession(sessionID: String): Boolean = {
    clear()
    lobby.put(JOIN_SESSION, clientID, sessionID)
    val valid = verifySession(lobby, sessionID)
    if (valid) {
      println(s"Joined a session: $sessionID")

      // Create remote space
      val sessionSpace = new RemoteSpace(spaceURL(sessionID))
      room = Some(sessionSpace)

      // Recreate history
      val history = sessionSpace.queryAllS(EVENT, HISTORY, classOf[Operation[String]])
      history foreach (x => process(x._3))

      // Spawn event listener
      new Listener(sessionSpace).spawn()
    } else {
      JOptionPane.showMessageDialog(
        null,
        s"Invalid session \"$sessionID\"",
        "Info",
        JOptionPane.INFORMATION_MESSAGE
      )
    }
    valid
  }

  class Listener(space: Space) extends Runnable {
    override def run(): Unit = {
      while (true) {
        val (_, _, op) = space.getS(EVENT, clientID, classOf[Operation[String]])
        process(op)
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

        space.put(EVENT, HISTORY, event)
        clients.filter(_ != clientID).foreach(space.put(EVENT, _, event))
    }
  }

  // Ask server if Session exists.
  private def verifySession(lobby: RemoteSpace, sessionID: String): Boolean = {
    val (test, _, _) = lobby.getS(classOf[String], clientID, sessionID)
    test == SESSION
  }
}
