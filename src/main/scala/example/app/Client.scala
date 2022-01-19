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
  var room: Option[RemoteSpace] = None
  var listener: Option[Thread] = None
  var listenerPing: Option[Thread] = None
  var sessionID: String = ""

  /** Ask for a new collaboration session */
  def newSession(): String = {
    clear()
    lobby.put(START_SESSION, clientID)

    // Receive session ID
    sessionID = lobby.getS(SESSION, clientID, classOf[String])._3
    println(s"Started a session: $sessionID")

    // Create remote space
    val space = new RemoteSpace(spaceURL(sessionID))
    room = Some(space)

    // Spawn event listener
    listener = Some(new Listener(space).spawn())
    listenerPing = Some(new ListenerPing(space).spawn())


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
    listener match {
      case None =>
      case Some(thread) => thread.interrupt()
    }
    listenerPing match {
      case None =>
      case Some(thread) => thread.interrupt()
    }
    listener = None
    listenerPing = None

    room match {
      case None =>
      case Some(space) =>
        val (_, clients) = space.getS(CLIENTS, classOf[Array[String]])
        val updated = clients.filter(_ != clientID)
        space.put(CLIENTS, updated)

        // if last client, remove the tuple space
        if (updated.isEmpty) lobby.put(CLEANUP, sessionID)
    }
    room = None

    crdt.clear()
    onUpdate(crdt.asString)
  }

  /** Try to join the session */
  def joinSession(clientSessionID: String): Boolean = {
    lobby.put(JOIN_SESSION, clientID, clientSessionID)
    val valid = verifySession(lobby, clientSessionID)
    if (valid) {
      sessionID = clientSessionID
      clear()
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
  class ListenerPing(space: Space) extends Runnable {
    override def run(): Unit = {
      while (true) {
        space.getS(PING,clientID)
        println("got a ping!")
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
