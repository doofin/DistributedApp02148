package example.exercises.Jakub

import org.w3c.dom.Document

object TextCommon {
  val baseURL: String = spaceURL("");

  val JOIN_SPACE_ID = "joinSpace"

  def spaceURL(spaceID: String): String = s"tcp://localhost:35123/$spaceID?conn"

  object Event {
    val START_SESSION: String = "create"
    val JOIN_SESSION: String = "join"
    val SESSION: String = "session"
    val INVALID_SESSION: String = "invalid"
    val DOCUMENT: String = "document_init"
  }

  trait Document[T] {
    def getContents(t: T): String
  }

  object Documents {
    implicit object stringDocument extends Document[String] {
      override def getContents(s: String): String = s
    }
  }
}
