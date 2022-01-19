package example.app

import example.ScalaSpaces.{RunnableOps, SpaceOps}
import Common.Event._
import Common._
import org.jspace._

import java.util
import scala.io.StdIn.readLine


// Coordinates clients that work on some file
object TextServer {
  def main(args: Array[String]): Unit = {
    // Create a repository

    val repo = new SpaceRepository
    val join = new SequentialSpace
    val sessionArray = new util.ArrayList[SequentialSpace]

    repo.add(JOIN_SPACE_ID, join)
    repo.addGate(baseURL)

    new SessionStarter(repo, join, sessionArray).spawn()
    new SessionJoiner(repo, join).spawn()
    new SessionIdCreator(join).spawn()
    new SessionCleanup(repo, join).spawn()
    new SessionButler(repo, sessionArray).spawn()

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
class SessionStarter(repo: SpaceRepository, lobby: Space, sessionArray: util.ArrayList[SequentialSpace]) extends Runnable {
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
      sessionArray.add(fileSpace)
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

/** handling actions of cleaning up sessions after everyone leaves */
class SessionButler(repo: SpaceRepository, sessionArray: util.ArrayList[SequentialSpace]) extends Runnable {
  override def run(): Unit = {
    println("Butler")
    while (!Thread.currentThread().isInterrupted) {
      sessionArray.forEach(session => {
        val (_, clientlistx) = session.getS(CLIENTS, classOf[Array[String]])
        var newList = clientlistx
        clientlistx.foreach(client => {
          if (session.queryAllS(PING, client).length > 2) {

            newList = newList.filter(_ != client)
            println(s"Removed user: $client")

          } else {

            session.put(PING, client)
          }
        }
        )
        //if (newList.isEmpty){
        //  lobby.put(CLEANUP,session)
        //
        //}
        //else {
        //  session.put(CLIENTS, newList)
        //}
      })

      Thread.sleep(1000)
    }
  }
}