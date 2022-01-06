package example.exercises

import example.Utils.MainRunnable
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.text.Font
import javafx.scene.{Parent, Scene}
import javafx.stage.{Stage, WindowEvent}
import lib.EditorUtils

object jfxeditor extends MainRunnable {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[jfxeditor], "")
  }

}

class jfxeditor extends Application {
  def run = Application.launch("")
  @throws[Exception]
  override def start(primaryStage: Stage): Unit = {
    Font.loadFont(getClass.getResourceAsStream("/Roboto-Regular.ttf"), 16)

//    Caused by: java.lang.IllegalStateException: Location is not set
    val r = getClass.getResource("/main.fxml")
//    println("r path : ", r)
    val fxmlLoader = new FXMLLoader(getClass.getResource("/main.fxml"))

    val root: Parent = fxmlLoader.load
    val scene = new Scene(root)
    scene.getStylesheets.add(
      getClass.getResource("/style.css").toExternalForm
    )
    primaryStage.setScene(scene)
    primaryStage.setMinHeight(640)
    primaryStage.setMinWidth(640)
    primaryStage.setTitle("untitled")
    primaryStage.setOnCloseRequest((event: WindowEvent) => {
      def foo(event: WindowEvent) = {
        EditorUtils.onCloseExitConfirmation()
        event.consume()
      }

      foo(event)
    })
    primaryStage.show()
  }
}
