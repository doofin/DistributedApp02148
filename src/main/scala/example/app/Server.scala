package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import Common.Event._
import Common._
import org.jspace._

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
  }
}

class SessionStarter(repo: SpaceRepository, joinSpace: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for CREATE requests...")
    while (!Thread.currentThread().isInterrupted) {
      // wait for an incoming connection
      val (_, clientId) = joinSpace.getS(START_SESSION, classOf[String])

      // create a tuple space for the client
      val sessionID = s"session-$clientId"
      val fileSpace = new SequentialSpace
      repo.add(sessionID, fileSpace)

      // save info about new space
      fileSpace.put(CLIENTS, Array(clientId))

      // send its name back
      println(s"Client $clientId created a session: $sessionID")
      joinSpace.put(SESSION, clientId, sessionID)
    }
  }
}

class SessionJoiner(repo: SpaceRepository, joinSpace: Space) extends Runnable {
  override def run(): Unit = {
    println("Listening for JOIN requests...")
    while (!Thread.currentThread().isInterrupted) {
      // wait for an incoming connection
      val (_, clientId, sessionID) = joinSpace.getS(JOIN_SESSION, classOf[String], classOf[String])

      // check if session exists
      Option(repo.get(sessionID)) match {
        case None => joinSpace.put(INVALID_SESSION, clientId, sessionID)
        case Some(fileSpace) =>
          // Notify others
          val (_, oldClients) = fileSpace.getS(CLIENTS, classOf[Array[String]])
          fileSpace.put(CLIENTS, (clientId :: oldClients.toList).toArray)

          // Respond that session worked
          joinSpace.put(SESSION, clientId, sessionID)
          println(s"Client $clientId joined session $sessionID")
      }
    }
  }
}
