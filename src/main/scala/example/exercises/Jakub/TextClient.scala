package example.exercises.Jakub

import example.ScalaSpaces.SpaceOps
import example.exercises.Jakub.TextCommon.Documents._
import example.exercises.Jakub.TextCommon.Event._
import example.exercises.Jakub.TextCommon._
import net.team2xh.scurses._
import org.jspace._

import scala.io.StdIn.readLine
import scala.util.Random
import scala.util.control.Breaks._

object TextClient {
  def main(args: Array[String]): Unit = {
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
  val myID = s"client-${Random.nextInt(100)}"

  def initializeSession(): Unit = {
    // Ask for a new collaboration session
    joinSpace.put(START_SESSION, myID)

    // Receive session ID
    val (_, _, sessionID) = joinSpace.getS(SESSION, myID, classOf[String])
    println(s"Started a session: $sessionID")

    // Send initial document state
    val sessionSpace = new RemoteSpace(spaceURL(sessionID))
    var document = "An example text document."
    sessionSpace.put(DOCUMENT, document)

    // Start inline editor
    workOn(document)
  }

  def joinSession(sessionID: String): Unit = {
    // Try to join the session
    joinSpace.put(JOIN_SESSION, myID, sessionID)
    val (event, _, _) = joinSpace.getS(classOf[String], myID, sessionID)
    event match {
      case INVALID_SESSION => return
      case SESSION =>
    }

    val sessionSpace = new RemoteSpace(spaceURL(sessionID))

    // Start working on remote document
    workOn(new RemoteDocument(sessionSpace))(remoteDocument) // TODO: how do I make the implicit work here?
  }

  def workOn[T](document: T)(implicit doc: Document[T]): Unit = {
    val contents = doc.getContents(document)
    println(contents)
  }

  //region RemoteDocument

  class RemoteDocument(space: RemoteSpace) {
    val _space: RemoteSpace = space
  }

  implicit object remoteDocument extends Document[RemoteDocument] {
    override def getContents(t: RemoteDocument): String = {
      val (_, document) = t._space.queryS(DOCUMENT, classOf[String])
      document
    }
  }

  //endregion
}
