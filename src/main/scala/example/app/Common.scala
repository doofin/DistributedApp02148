package example.app

object Common {
  val baseURL: String = spaceURL("");

  val LOBBY_SPACE_ID = "joinSpace"

      def spaceURL(spaceID: String): String = s"tcp://25.52.107.249:35123/$spaceID?conn"
//  def spaceURL(spaceID: String): String = s"tcp://0.0.0.0:35123/$spaceID?conn"
}

object Event {
  val START_SESSION: String = "create"
  val JOIN_SESSION: String = "join"
  val SESSION: String = "session"
  val INVALID_SESSION: String = "invalid"
  val EVENT: String = "event"
  val CLIENTS: String = "clients"
  val GENERATE_NEW_ID = "generateId"
  val NEWID = "newId"
  val HISTORY = "history"
  val CLEANUP = "cleanup"
  val PING = "ping"
}
