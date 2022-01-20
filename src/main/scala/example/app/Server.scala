package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import example.app.Common._
import example.app.Event._
import example.app.TextServer.SessArray
import org.jspace._

import java.util
import scala.io.StdIn.readLine

// Coordinates clients that work on some file
object TextServer {
  type SessArray = util.ArrayList[(String, SequentialSpace)]

  def run(): Unit = {
    // Create a repository

    val repo = new SpaceRepository
    val lobby = new SequentialSpace
    val sessionArray = new util.ArrayList[(String, SequentialSpace)]

    repo.add(LOBBY_SPACE_ID, lobby)
    repo.addGate(baseURL)

    new SessionStarter(repo, lobby, sessionArray).spawn()
    new SessionJoiner(repo, lobby).spawn()
    new SessionIdCreator(lobby).spawn()
    new SessionCleanup(repo, lobby, sessionArray).spawn()
    new SessionButler(lobby, sessionArray).spawn()

    readLine("Press ENTER to stop the server\n")
  }
}

/** for creating unique IDs * */
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
class SessionStarter(
                      repo: SpaceRepository,
                      lobby: Space,
                      sessionArray: SessArray
                    ) extends Runnable {
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
      sessionArray.add((sessionID, fileSpace))
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
class SessionCleanup(
                      repo: SpaceRepository,
                      lobby: Space,
                      sessionArray: SessArray
                    ) extends Runnable {
  override def run(): Unit = {
    println("Listening for CLEANUP requests...")
    while (!Thread.currentThread().isInterrupted) {
      val (_, sessionID) = lobby.getS(CLEANUP, classOf[String])
      repo.remove(sessionID)
      sessionArray.removeIf(tuple => tuple._1 == sessionID)
      println(s"Session $sessionID closed")
    }
  }
}

/** handling actions of cleaning up sessions after everyone leaves */
class SessionButler(lobby: Space, sessionArray: SessArray) extends Runnable {
  override def run(): Unit = {
    println("Butler listening...")
    while (!Thread.currentThread().isInterrupted) {
      sessionArray.forEach(tuple => {
        val (sessionId, session) = tuple

        val (_, clients) = session.getS(CLIENTS, classOf[Array[String]])
        var newClients = clients.clone()
        clients.foreach(client => {
          if (session.queryAllS(PING, client).length > 2) {
            newClients = newClients.filter(_ != client)
            println(s"Removed user: $client")
          } else {
            session.put(PING, client)
          }
        })

        if (newClients.isEmpty) lobby.put(CLEANUP, sessionId)
        session.put(CLIENTS, newClients)
      })

      Thread.sleep(5000)
    }
  }
}
