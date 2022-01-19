package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import Common.Event._
import Common._
import org.jspace._

import scala.io.StdIn.readLine


// Coordinates clients that work on some file
object TextServer {
  def main(args: Array[String]): Unit = {
    // Create a repository

    val repo = new SpaceRepository
    val join = new SequentialSpace

    repo.add(JOIN_SPACE_ID, join)
    repo.addGate(baseURL)

    new SessionStarter(repo, join).spawn()
    new SessionJoiner(repo, join).spawn()
    new SessionIdCreator(join).spawn()
    new SessionCleanup(repo, join).spawn()

    readLine("Press ENTER to stop the server\n")
  }
}

/** for creating unique IDs **/
class SessionIdCreator(lobby: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for ID requests...")
    var id = 1 //initial id value.

    while (!Thread.currentThread().isInterrupted) {
      // wait for an incoming connection
      lobby.getS(GENERATE_NEW_ID)
      //generate new id
      lobby.put(NEWID, id)
      id += 1

    }
  }
}

/** for creating new session */
class SessionStarter(repo: SpaceRepository, lobby: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for CREATE requests...")
    var ServerNumber = 1
    while (!Thread.currentThread().isInterrupted) {
      // wait for an incoming connection
      val (_, clientId) = lobby.getS(START_SESSION, classOf[String])

      // create a tuple space for the client
      val sessionID = s"session-$clientId-$ServerNumber"
      val fileSpace = new SequentialSpace

      repo.add(sessionID, fileSpace)

      // save info about new space
      fileSpace.put(CLIENTS, Array(clientId))

      // send its name back
      println(s"Client $clientId created a session: $sessionID")
      lobby.put(SESSION, clientId, sessionID)
      ServerNumber += 1
    }
  }
}

/** handling actions of joining a existing session */
class SessionJoiner(repo: SpaceRepository, lobby: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for JOIN requests...")
    while (!Thread.currentThread().isInterrupted) {
      // wait for an incoming connection
      val (_, clientId, sessionID) =
        lobby.getS(JOIN_SESSION, classOf[String], classOf[String])

      // check if session exists
      Option(repo.get(sessionID)) match {
        case None =>
          lobby.put(INVALID_SESSION, clientId, sessionID)
          println(s"Session : $sessionID does not exist")
        case Some(fileSpace) =>
          // Notify others
          val (_, oldClients) = fileSpace.getS(CLIENTS, classOf[Array[String]])
          fileSpace.put(CLIENTS, (clientId :: oldClients.toList).toArray)

          // Respond that session worked
          lobby.put(SESSION, clientId, sessionID)
          println(s"Client $clientId joined session $sessionID")

      }
    }
  }
}

/** handling actions of cleaning up sessions after everyone leaves */
class SessionCleanup(repo: SpaceRepository, lobby: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for CLEANUP requests...")
    while (!Thread.currentThread().isInterrupted) {
      val (_, sessionID) = lobby.getS(CLEANUP, classOf[String])
      repo.remove(sessionID)
      println(s"Session $sessionID closed")
    }
  }
}
