package example.exercises.Jakub

object TextCommon {
  val baseURL: String = spaceURL("");

  val JOIN_SPACE_ID = "joinSpace"

  def spaceURL(spaceID: String): String = s"tcp://localhost:35123/$spaceID?conn"

  object Event {
    val START_SESSION: String = "create"
    val JOIN_SESSION: String = "join"
    val SESSION: String = "session"
    val INVALID_SESSION: String = "invalid"
    val EVENT: String = "event"
    val CLIENTS: String = "clients"
  }

  trait Document {
    def asString: String
  }
}
