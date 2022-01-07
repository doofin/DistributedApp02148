package example.app

import com.formdev.flatlaf.FlatLightLaf
import example.app.editorView.addToMenus

import java.io._
import java.awt._
import java.awt.event._
import javax.swing._
import scala.util.{Failure, Success, Try}

//Set Ocean theme (default for cross platform metal theme...)
//MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
class editorView() extends JFrame with ActionListener with AdjustmentListener {
//  this: editorView =>
  var jTextArea = new JTextArea
  var jFrame = new JFrame("Group5 collabrative Text Editor")
  var scroll = new JScrollPane(jTextArea)

  def moveCursorTo = {}
  def insertAt = {}

  //    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
  Try(
    UIManager.setLookAndFeel(new FlatLightLaf())
  ) match {
    case Failure(exception) =>
    case Success(value)     =>
  }

  //Creating menu bar
  val jMenuBar: JMenuBar = new JMenuBar
  addToMenus(jMenuBar, this)
  jFrame.setJMenuBar(jMenuBar)
  jFrame.add(jTextArea)
  jFrame.setVisible(true)

  scroll.setVerticalScrollBarPolicy(
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
  )
  scroll.setHorizontalScrollBarPolicy(
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
  )
  jFrame.add(scroll)
  scroll.setSize(20, 500)
  jFrame.setSize(500, 500)
  jTextArea.setSize(400, 500)
  val fontSz = 25
  val fo = new Font("SansSerif", Font.PLAIN, fontSz)
  jTextArea.setFont(fo)
  jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

  override def adjustmentValueChanged(e: AdjustmentEvent): Unit = {}

  //If a button is pressed
  override def actionPerformed(ae: ActionEvent): Unit = { //Getting to know which button is pressed
    val s = ae.getActionCommand
    //If copy is pressed
    s match {
      case "Copy"  => jTextArea.copy()
      case "Cut"   => jTextArea.cut()
      case "Paste" => Try(jTextArea.paste())
      case "Open"  =>
        // Create an object of JFileChooser class
        val fileopener = new JFileChooser("f:")
        //open dialog to make user connect with the drives
        val r = fileopener.showOpenDialog(null)
        if (r == JFileChooser.APPROVE_OPTION) { //to get the option constant from filechooser
          val fileo = new File(fileopener.getSelectedFile.getAbsolutePath)
          Try { //string
            var s1 = ""
            var s2 = ""
            //creating the File reader object
            val fr = new FileReader(fileo)
            val br = new BufferedReader(fr)
            s2 = br.readLine
            //it reads line by line from the file and appends  the s2 string one line by line
            s1 = br.readLine
            while (s1 != null) s2 = s2 + "\n" + s1
            // Set the text
            jTextArea.setText(s2)
          } match {
            case Success(value) => ???
            case Failure(exception) =>
              JOptionPane.showMessageDialog(jFrame, exception.getMessage)
          }
        }
      case "Save" =>
        //Add Content to Save a File!!!!!!!!!!!
        val filesaver = new JFileChooser("f:")
        //opened dialog box to select directory by the user
        val r = filesaver.showSaveDialog(null) //getting thee option value in methods
        if (r == JFileChooser.APPROVE_OPTION) { // Set the label to the path of the selected directory
          val files = new File(
            filesaver.getSelectedFile.getAbsolutePath
          )

          try { // Create a file writer
            val wr = new FileWriter(files, false)
            // Create buffered writer to write
            val w = new BufferedWriter(wr)
            w.write(jTextArea.getText)
            w.flush() //to clear buffer in stdin
            w.close() //closing the buffered writerr class
          } catch {
            case evt: Exception =>
              JOptionPane.showMessageDialog(jFrame, evt.getMessage)
          }
        }
      case _ =>
        val fontSz = 25
        if (s == "New") jTextArea.setText("")
        else if (s == "Close") {
          jFrame.setVisible(false)
          System.exit(0)
        } else if (s == "About") {
          val newLine = System.getProperty("line.separator") //This will retrieve line separator dependent on OS.
          //The following message would be displayed when "About" button is clicked
          JOptionPane.showMessageDialog(
            jFrame,
            "Srivatsan : Worked on database using JFileChooser plus minimal changes in Nikhil's code " + newLine + " Nikhil : Added File,Edit,Close button,Frame,Panel " + newLine + " Sahil : Added themes, fonts,scroll bar and the box in which you're reading this message...Thank You !"
          )
        }
    }

  }
}

object editorView {
  def main(args: Array[String]): Unit = {
    val o = new editorView()
  }

  def addToMenus(jMenuBar: JMenuBar, thisE: editorView) = {
    val m1 = new JMenu("File")
    //Adding menu items
    val m1a = new JMenuItem("New")
    val m1b = new JMenuItem("Open")
    val m1c = new JMenuItem("Save")
    //Add Action Listener to each menu items
    m1a.addActionListener(thisE)
    m1b.addActionListener(thisE)
    m1c.addActionListener(thisE)
    m1.add(m1a)
    m1.add(m1b)
    m1.add(m1c)
    //Creating edit menu
    val m2 = new JMenu("Edit")
    val m2a = new JMenuItem("Cut")
    val m2b = new JMenuItem("Copy")
    val m2c = new JMenuItem("Paste")
    m2a.addActionListener(thisE)
    m2b.addActionListener(thisE)
    m2c.addActionListener(thisE)
    m2.add(m2a)
    m2.add(m2b)
    m2.add(m2c)
    //About
    val m4 = new JMenu("Help")
    //Adding about
    val m4a = new JMenuItem("About")
    m4a.addActionListener(thisE)
    m4.add(m4a)
    val mc = new JMenuItem("Close")
    //Adding Action Listener
    mc.addActionListener(thisE)
    //Adding menu items to menu bar
    jMenuBar.add(m1)
    jMenuBar.add(m2)
    jMenuBar.add(m3)
    jMenuBar.add(m4)
    jMenuBar.add(mc)
    //Setting menubar on the frame

  }
}
