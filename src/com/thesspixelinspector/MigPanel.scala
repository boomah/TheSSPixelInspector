package com.thesspixelinspector

import net.miginfocom.swing.MigLayout
import swing.{Button, Panel, SequentialContainer, Component}
import javax.swing.ImageIcon
import java.awt.Dimension

class MigPanel(layoutConstraints:String = "", colConstraints:String = "", rowConstraints:String = "") extends Panel with SequentialContainer.Wrapper {
  override lazy val peer = new javax.swing.JPanel(new MigLayout(layoutConstraints, colConstraints, rowConstraints))
  protected def add(component:Component, constraints:String = "") {peer.add(component.peer, constraints)}
}

class SmallButton(iconURL:String) extends Button {
  icon = new ImageIcon(getClass.getResource(iconURL))
  val iconPadding = 10
  val bestSize = new Dimension(icon.getIconWidth + iconPadding, icon.getIconHeight + iconPadding)
  preferredSize = bestSize
  maximumSize = bestSize
}