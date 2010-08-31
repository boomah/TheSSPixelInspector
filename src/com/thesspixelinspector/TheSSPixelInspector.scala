package com.thesspixelinspector

import _root_.java.awt.{Dimension, Toolkit, Robot, MouseInfo, Rectangle, Color, Graphics2D}
import scala.swing.Swing._
import swing.event.{ButtonClicked, Key, WindowClosing}
import scala.math._
import java.awt.image.BufferedImage
import java.awt.geom.AffineTransform
import java.awt.event.{InputEvent, KeyEvent, ActionEvent, ActionListener}
import swing._
import javax.swing.{Timer, KeyStroke, JComponent}
import java.text.DecimalFormat

class TheSSPixelInspector extends Frame {
  private val imagePanel = new ImagePanel {
    preferredSize = new Dimension()
  }
  imagePanel.preferredSize = new Dimension(400, 300)
  contents = new MigPanel("") {
    title = "The SS Pixel Inspector"
    val checkBox  = new CheckBox {
      text = "Enable"
      tooltip = "Enable or disable the screen capture"
      mnemonic = Key.E
      selected = false
      reactions += {case ButtonClicked(e) => enableOrDisable}
    }

    def enableOrDisable {
      if (checkBox.selected) {
        imagePanel.resetOffSets
        enableInspector
      }
      else {
        disableInspector
      }
    }

    def zoomIn {imagePanel.increaseZoom;updateState}
    def zoomOut {imagePanel.decreaseZoom;updateState}
    def updateState {
      zoomInButton.enabled = imagePanel.canIncreaseZoom
      zoomInAction.enabled = imagePanel.canIncreaseZoom
      zoomOutButton.enabled = imagePanel.canDecreaseZoom
      zoomOutAction.enabled = imagePanel.canDecreaseZoom
      zoomLevelLabel.text = zoomLevelText
    }

    val zoomInButton = new SmallButton("/com/thesspixelinspector/icons/16x16_ZoomIn.png") {
      tooltip = "Click to zoom in (=)"
      enabled = imagePanel.canIncreaseZoom
      reactions += {
        case ButtonClicked(b) => zoomIn
      }
    }

    val zoomOutButton = new SmallButton("/com/thesspixelinspector/icons/16x16_ZoomOut.png") {
      tooltip = "Click to zoom out (-)"
      enabled = imagePanel.canDecreaseZoom
      reactions += {
        case ButtonClicked(b) => zoomOut
      }
    }

    val textFormat = new DecimalFormat("000")
    def zoomLevelText = {
      textFormat.format(imagePanel.currentZoomLevel * 100) + "%"
    }

    val zoomLevelLabel = new Label(zoomLevelText)

    add(imagePanel, "push, grow, wrap")
    add(checkBox, "split")
    add(zoomInButton, "gapafter 3lp")
    add(zoomOutButton)
    add(zoomLevelLabel)

    val zoomInAction = Action("zoomIn") {zoomIn}
    val zoomOutAction = Action("zoomOut") {zoomOut}
    zoomInAction.enabled = imagePanel.canIncreaseZoom
    zoomOutAction.enabled = imagePanel.canDecreaseZoom

    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomIn")
    peer.getActionMap.put("zoomIn", zoomInAction.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut")
    peer.getActionMap.put("zoomOut", zoomOutAction.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "enableDisable")
    peer.getActionMap.put("enableDisable", Action("enableDisable") {
      checkBox.selected = !checkBox.selected
      enableOrDisable
    }.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "viewLeft")
    peer.getActionMap.put("viewLeft", Action("viewLeft") {if (!checkBox.selected) imagePanel.decreaseXOffSet}.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "viewRight")
    peer.getActionMap.put("viewRight", Action("viewRight") {if (!checkBox.selected) imagePanel.increaseXOffSet}.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "viewUp")
    peer.getActionMap.put("viewUp", Action("viewUp") {if (!checkBox.selected) imagePanel.decreaseYOffSet}.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "viewDown")
    peer.getActionMap.put("viewDown", Action("viewDown") {if (!checkBox.selected) imagePanel.increaseYOffSet}.peer)
    peer.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "resetOffSets")
    peer.getActionMap.put("resetOffSets", Action("resetOffSets") {if (!checkBox.selected) imagePanel.resetOffSets}.peer)
  }
  reactions += {case WindowClosing(e) => quit}
  peer.getRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), "quit")
  peer.getRootPane.getActionMap.put("quit", Action("quit") {quit}.peer)

  private val screenSize = Toolkit.getDefaultToolkit.getScreenSize

  private var timer:Timer = null
  private val robot = new Robot

  private def enableInspector {
    timer = new Timer(0, new ActionListener{
      def actionPerformed(e:ActionEvent) = {
        try {
          val location = MouseInfo.getPointerInfo.getLocation
          // Don't draw if we are inside the frame.
          if (!bounds.contains(location)) {
            val w = imagePanel.size.width
            val h = imagePanel.size.height
            val captureArea = new Rectangle(location.x-w/2,location.y-h/2,w,h)
            val image = robot.createScreenCapture(captureArea)
            // If part of the image is from off screen, paint it black.
            val g = image.getGraphics
            g.setColor(Color.BLACK)
            if (captureArea.x < 0) {
              g.fillRect(0,0,abs(captureArea.x),h)
            }
            if (captureArea.x + w > screenSize.width) {
              val diff = captureArea.x + w - screenSize.width
              g.fillRect(w-diff,0,diff,h)
            }
            if (captureArea.y < 0) {
              g.fillRect(0,0,w,abs(captureArea.y))
            }
            if (captureArea.y + h > screenSize.height) {
              val diff = captureArea.y + h - screenSize.height
              g.fillRect(0,h-diff,w,diff)
            }
            imagePanel.image = image
          }
        } catch {
          case np:NullPointerException =>
        }
      }
    }) {
      setDelay(100)
      start
    }
  }
  private def disableInspector {timer.stop}

  private def quit {
    if (timer != null) timer.stop
    System.exit(0)
  }

  pack
  centerOnScreen
  visible = true
}

object TheSSPixelInspectorRunner {
  def main(args:Array[String]) {onEDT(new TheSSPixelInspector)}
}

class ImagePanel extends Panel {
  private val zoomLevels = Array(1.0, 2.0, 4.0, 8.0, 16.0)
  private var _image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
  border = LineBorder(Color.GRAY)
  def image = _image
  def image_=(image:BufferedImage) {_image = image;repaint}
  private var zoomLevel = 0
  def increaseZoom {zoomLevel += 1;repaint}
  def decreaseZoom {zoomLevel -= 1;repaint}
  def canIncreaseZoom = {zoomLevel < (zoomLevels.length - 1)}
  def canDecreaseZoom = {zoomLevel > 0}
  def currentZoomLevel = zoomLevels(zoomLevel)
  private var _xOffSet = 0
  private var _yOffSet = 0
  def decreaseXOffSet {_xOffSet += 1;repaint}
  def increaseXOffSet {_xOffSet -= 1;repaint}
  def decreaseYOffSet {_yOffSet += 1;repaint}
  def increaseYOffSet {_yOffSet -= 1;repaint}
  def resetOffSets {_xOffSet = 0; _yOffSet = 0;repaint}

  override protected def paintComponent(g:Graphics2D) = {
    g.setColor(Color.LIGHT_GRAY)
    g.fillRect(0,0,size.width,size.height)
    val origTransform = g.getTransform
    val transform = new AffineTransform
    val level = zoomLevels(zoomLevel)
    val currentZoomLevels = zoomLevels.take(zoomLevel + 1)
    val halfWidth = size.width / 2.0
    val offSetFactor = 16 / level
    val xPos = (_xOffSet * offSetFactor + halfWidth / level - halfWidth).round.toInt
    val halfHeight = size.height / 2.0
    val yPos = (_yOffSet * offSetFactor + halfHeight / level - halfHeight).round.toInt
    transform.scale(level, level)
    g.setTransform(transform)
    g.drawImage(image, xPos, yPos, null)
    g.setTransform(origTransform)

    g.setColor(Color.RED)
    g.drawRect(halfWidth.toInt - 3, size.height / 2 - 3, 6, 6)
  }
}