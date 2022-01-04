package example.exercises.Jakub

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import example.exercises.Jakub.TextCommon.Event._
import example.exercises.Jakub.TextCommon._
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

  class SessionStarter(repo: SpaceRepository, space: Space) extends Runnable {
    override def run(): Unit = {
      println("Listening for CREATE requests...")
      while (!Thread.currentThread().isInterrupted) {
        // wait for an incoming connection
        val (_, clientId) = space.getS(START_SESSION, classOf[String])

        // create a tuple space for the client
        val sessionID = s"session-$clientId"
        repo.add(sessionID, new SequentialSpace)

        // send its name back
        println(s"Client $clientId created a session: $sessionID")
        space.put(SESSION, clientId, sessionID)
      }
    }
  }

  class SessionJoiner(repo: SpaceRepository, space: Space) extends Runnable {
    override def run(): Unit = {
      println("Listening for JOIN requests...")
      while (!Thread.currentThread().isInterrupted) {
        // wait for an incoming connection
        val (_, clientId, sessionID) = space.getS(JOIN_SESSION, classOf[String], classOf[String])

        // check if session exists
        Option(repo.get(sessionID)) match {
          case None => space.put(INVALID_SESSION, clientId, sessionID)
          case Some(_) =>
            space.put(SESSION, clientId, sessionID)
            println(s"Client $clientId joined session $sessionID")
        }
      }
    }
  }
}
