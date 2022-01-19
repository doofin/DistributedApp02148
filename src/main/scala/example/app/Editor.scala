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
import javax.swing.event.{DocumentEvent, DocumentListener}
import javax.swing.text.{AbstractDocument, AttributeSet, DocumentFilter}
import scala.io.StdIn.readLine

object Editor {
  def main(args: Array[String]): Unit = {
    new Editor
    readLine("running client,press enter to stop\n")
  }
}

class Editor
  extends JFrame("Group 5 – Collaborative Text Editor")
    with ActionListener
    with AdjustmentListener {
  // region Constructor

  import com.formdev.flatlaf.FlatLightLaf
  import javax.swing.UIManager

  UIManager.setLookAndFeel(new FlatLightLaf)

  setSize(500, 500)
  setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  setLayout(new BorderLayout)

  val textArea = new JTextArea()
  textArea.setFont(new Font("SansSerif", Font.PLAIN, 30))

  val client = new Client(updateText)

  val scroll = new JScrollPane(textArea)
  scroll.setVerticalScrollBarPolicy(
    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
  )
  scroll.setHorizontalScrollBarPolicy(
    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
  )

  val panel = new JPanel
  panel.setLayout(new BorderLayout)
  panel.add(scroll, BorderLayout.CENTER)
  add(panel, BorderLayout.CENTER)

  val jMenuBar = new JMenuBar
  addToMenus(jMenuBar)
  setJMenuBar(jMenuBar)

  val statusBar = new JLabel(" Disconnected")
  statusBar.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 15))
  add(statusBar, BorderLayout.SOUTH)

  setVisible(true)
  // endregion

  // region handlers

  private val doc = textArea.getDocument.asInstanceOf[AbstractDocument]
  doc.setDocumentFilter(new DocumentInsertionFilter(client))

  textArea.getDocument.addDocumentListener(new DocumentChangeListener(client))

  // endregion
  // region Methods

  private def updateText(txt: String): Unit = {
    textArea.setText(s"[CRDT]$txt")
  }

  override def adjustmentValueChanged(e: AdjustmentEvent): Unit = {}

  override def actionPerformed(ae: ActionEvent): Unit = {
    ae.getActionCommand match {
      case "New session" =>
        val sessionID = client.newSession()
        JOptionPane.showMessageDialog(
          null,
          s"Session ID: $sessionID",
          "Info",
          JOptionPane.INFORMATION_MESSAGE
        )
        statusBar.setText(s" Connected: $sessionID") // TODO: Handle failure
      case "Join session" =>
        val sessionID = JOptionPane.showInputDialog("Session ID:")
        if (sessionID != null) { // check if cancel was pressed
          if (client.joinSession(sessionID))
            statusBar.setText(s" Connected: $sessionID") // TODO: Handle failure
        }
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
            val text = try source.getLines mkString "\n"
            finally source.close()
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
          Try {
            val wr = new BufferedWriter(new FileWriter(file, false))
            wr.write(textArea.getText)
            wr.close()
          } match {
            case Failure(e) =>
              JOptionPane.showMessageDialog(this, e.getMessage)
            case Success(value) =>
          }
        }
      case "Close" =>
        setVisible(false)
        System.exit(0)
      case "About" =>
        JOptionPane.showMessageDialog(
          this,
          "Collaborative text editor by Group 5"
        )
    }
  }

  private def addToMenus(jMenuBar: JMenuBar): Unit = {
    // File menu
    val m1 = new JMenu("File")
    val m1Items = Seq(
      new JMenuItem("New session"),
      new JMenuItem("Join session"),
      new JMenuItem("Open"),
      new JMenuItem("Save")
    )
    m1Items foreach { x =>
      x.addActionListener(this)
      m1.add(x)
    }
    // Edit menu
    val m2 = new JMenu("Edit")
    val m2Items = Seq(
      new JMenuItem("Cut"),
      new JMenuItem("Copy"),
      new JMenuItem("Paste")
    )
    m2Items foreach { x =>
      x.addActionListener(this)
      m2.add(x)
    }

    // Help / about menu
    val m3 = new JMenu("Help")
    val m3a = new JMenuItem("About")
    m3a.addActionListener(this)
    m3.add(m3a)

    Seq(m1, m2, m3) foreach (jMenuBar add _)
  }

  // endregion
}

class DocumentChangeListener(client: Client) extends DocumentListener {
  def insertUpdate(e: DocumentEvent): Unit = {
    logChange(e, "inserted into")
  }

  def removeUpdate(e: DocumentEvent): Unit = {
    logChange(e, "removed from")
  }

  def changedUpdate(e: DocumentEvent): Unit = {
    // Plain text components do not fire these events
  }

  def logChange(e: DocumentEvent, action: String): Unit = {
    val changeLength = e.getLength
    println(
      s"$changeLength character${if (changeLength == 1) " " else "s "}$action document at (${e.getOffset})"
    )
  }
}

class DocumentInsertionFilter(client: Client) extends DocumentFilter {
  override def replace(
                        fb: DocumentFilter.FilterBypass,
                        offset: Int,
                        length: Int,
                        text: String,
                        attrs: AttributeSet
                      ): Unit = {
    if (text.startsWith("[CRDT]")) {
      super.replace(fb, offset, length, text.drop(6), attrs)
    } else {
      super.replace(fb, offset, length, text, attrs)
      for (c <- text.reverse) client.writeChar(offset, c)
    }
    println(s"CRDT: ${client.crdt.asString}")
  }

  override def remove(
                       fb: DocumentFilter.FilterBypass,
                       offset: Int,
                       length: Int
                     ): Unit = {
    val indices = (offset until offset + length).reverse
    for (ix <- indices) client.deleteAt(ix + 1)
    super.remove(fb, offset, length)
    println(s"CRDT: ${client.crdt.asString}")
  }
}
