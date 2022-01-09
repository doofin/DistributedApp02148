package example.app

import java.awt._
import java.awt.event._
import java.io._
import javax.swing._
import scala.io.Source
import scala.util.{Failure, Success, Try}
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import java.awt.BorderLayout

class Editor extends JFrame("Group 5 – Collaborative Text Editor") with ActionListener with AdjustmentListener {
  // region Constructor
  setSize(500, 500)
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

  val textArea = new JTextArea
  textArea.setFont(new Font("SansSerif", Font.PLAIN, 16))

  val scroll = new JScrollPane(textArea)
  scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS)
  scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)

  val panel = new JPanel
  panel.setLayout(new BorderLayout)
  panel.add(scroll, BorderLayout.CENTER)
  add(panel)

  val jMenuBar = new JMenuBar
  addToMenus(jMenuBar)
  setJMenuBar(jMenuBar)

  setVisible(true)

  // endregion
  // region Methods

  def moveCursorTo() = ???

  def insertAt() = ???

  override def adjustmentValueChanged(e: AdjustmentEvent): Unit = {}

  override def actionPerformed(ae: ActionEvent): Unit = {
    ae.getActionCommand match {
      case "New" => textArea.setText("")
      case "Copy" => textArea.copy()
      case "Cut" => textArea.cut()
      case "Paste" => textArea.paste()
      case "Open" =>
        // Create an object of JFileChooser class
        val fileOpener = new JFileChooser("f:")

        // open dialog to make user connect with the drives
        val r = fileOpener.showOpenDialog(null)
        if (r == JFileChooser.APPROVE_OPTION) {
          Try {
            val path = fileOpener.getSelectedFile.getAbsolutePath
            val source = Source.fromFile(path)
            val text = try source.getLines mkString "\n" finally source.close()
            textArea.setText(text)
          } match {
            case Success(_) => ()
            case Failure(e) =>
              JOptionPane.showMessageDialog(this, e.getMessage)
          }
        }
      case "Save" =>
        val fileSaver = new JFileChooser("f:")
        // open dialog box to select directory by the user
        val r = fileSaver.showSaveDialog(null)
        if (r == JFileChooser.APPROVE_OPTION) {
          val file = new File(fileSaver.getSelectedFile.getAbsolutePath)
          try {
            val wr = new BufferedWriter(new FileWriter(file, false))
            wr.write(textArea.getText)
            wr.close()
          } catch {
            case e: Exception => JOptionPane.showMessageDialog(this, e.getMessage)
          }
        }
      case "Close" =>
        setVisible(false)
        System.exit(0)
      case "About" =>
        JOptionPane.showMessageDialog(this, "Collaborative text editor by Group 5")
    }
  }

  private def addToMenus(jMenuBar: JMenuBar): Component = {
    // File menu
    val m1 = new JMenu("File")
    val m1a = new JMenuItem("New")
    val m1b = new JMenuItem("Open")
    val m1c = new JMenuItem("Save")
    m1a.addActionListener(this)
    m1b.addActionListener(this)
    m1c.addActionListener(this)
    m1.add(m1a)
    m1.add(m1b)
    m1.add(m1c)

    // Edit menu
    val m2 = new JMenu("Edit")
    val m2a = new JMenuItem("Cut")
    val m2b = new JMenuItem("Copy")
    val m2c = new JMenuItem("Paste")
    m2a.addActionListener(this)
    m2b.addActionListener(this)
    m2c.addActionListener(this)
    m2.add(m2a)
    m2.add(m2b)
    m2.add(m2c)

    // Help / about menu
    val m3 = new JMenu("Help")
    val m3a = new JMenuItem("About")
    m3a.addActionListener(this)
    m3.add(m3a)

    jMenuBar.add(m1)
    jMenuBar.add(m2)
    jMenuBar.add(m3)
  }
  // endregion
}
