package com.jsuereth.video.swing

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{BorderLayout, Dimension, GridLayout}
import java.util
import javax.swing._

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import com.jsuereth.video.{Filter, FilterChain}
import com.jsuereth.video.filters.FiltersRegistry.FilterMeta
import com.jsuereth.video.filters.{FiltersRegistry, HorizontalFlipFilter}
import org.reactivestreams.Publisher

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private[swing] case class ApplyFiltersClicked(filters: Seq[Filter])

private[swing] case object ClearFiltersClicked

/**
  * Created by michael on 26.12.16.
  */
private[swing] class FiltersControls(actor: ActorRef) extends JPanel {
  private val applyButton = new JButton("Apply")
  private val clearButton = new JButton("Clear")

  private val selector = new DualListBox(ListBuffer(FiltersRegistry():_*))

  this setPreferredSize new Dimension(640, 100)
  this setMinimumSize new Dimension(640, 100)
  this setLayout new GridLayout(3, 1)
  applyButton setMaximumSize new Dimension(100, 50)
  clearButton setMaximumSize new Dimension(100, 50)
  selector setMinimumSize new Dimension(640, 320)

  this add selector
  this add applyButton
  this add clearButton

  applyButton addActionListener new ActionListener() {
    override def actionPerformed(e: ActionEvent): Unit =
      actor ! ApplyFiltersClicked(selector.destListModel.map(meta => FiltersRegistry(meta.key)))
  }

  clearButton addActionListener new ActionListener() {
    override def actionPerformed(e: ActionEvent): Unit = {
      selector.sourceListModel ++= selector.destListModel
      selector.destListModel.clear()
      actor ! ClearFiltersClicked
    }
  }

}

private[swing] class FiltersControlsActor extends ActorPublisher[UIControl] {

  sealed trait State

  case object Applied extends State

  case object Cleared extends State

  private var state: State = Cleared

  override def receive: Receive = {
    case data: ApplyFiltersClicked =>
      state match {
        case _ =>
          state = Applied
          this onNext ApplyFilter(FilterChain(data.filters:_*))
      }
    case ClearFiltersClicked =>
      state match {
        case Cleared => // ignore, already in correct state.
        case _ =>
          state = Cleared
          this onNext ClearFilter
      }
  }
}

object FiltersControls {

  def apply(factory: ActorRefFactory): (Publisher[UIControl], JComponent) = {
    val props = Props[FiltersControlsActor] withDispatcher "swing-dispatcher"
    val actorRef = factory actorOf(props, "filter-controls")
    val component = new FiltersControls(actorRef)
    ActorPublisher[UIControl](actorRef) -> component
  }

}

import scala.collection.JavaConversions._

class DualListBox(var sourceListModel: mutable.ListBuffer[FilterMeta],
                  var destListModel: mutable.ListBuffer[FilterMeta] = new ListBuffer[FilterMeta])
  extends JPanel {

  private var sourceList: JList[FilterMeta] = _

  private var destList: JList[FilterMeta] = _

  private var addButton: JButton = _

  private var removeButton: JButton = _

  initScreen()

  def clearSourceListModel() {
    sourceListModel.clear()
  }

  def clearDestinationListModel() {
    destListModel.clear()
  }

  def addSourceElements(newValue: ListModel[FilterMeta]) {
    fillListModel(sourceListModel, newValue)
  }

  def setSourceElements(newValue: ListModel[FilterMeta]) {
    clearSourceListModel()
    addSourceElements(newValue)
  }

  def addDestinationElements(newValue: ListModel[FilterMeta]) {
    fillListModel(destListModel, newValue)
  }

  private def fillListModel(model: ListBuffer[FilterMeta], newValues: ListModel[FilterMeta]) {
    val size = newValues.getSize
    for (i <- 0 until size) {
      model.add(newValues.getElementAt(i))
    }
  }

  def addSourceElements(newValue: Array[FilterMeta]) {
    fillListModel(sourceListModel, newValue)
  }

  def setSourceElements(newValue: Array[FilterMeta]) {
    clearSourceListModel()
    addSourceElements(newValue)
  }

  def addDestinationElements(newValue: Array[FilterMeta]) {
    fillListModel(destListModel, newValue)
  }

  private def fillListModel(model: ListBuffer[FilterMeta], newValues: Array[FilterMeta]) {
    model ++= newValues
  }

  private def clearSourceSelected() {
    val selected = sourceList.getSelectedValuesList
    sourceListModel = sourceListModel --= selected
    sourceList.getSelectionModel.clearSelection()
  }

  private def clearDestinationSelected() {
    val selected = destList.getSelectedValuesList
    destListModel = destListModel --= selected
    destList.getSelectionModel.clearSelection()
  }

  private def initScreen() {
    setLayout(new GridLayout(0, 2))
    sourceList = new JList(sourceListModel.toArray)
    addButton = new JButton(">>")
    addButton.addActionListener(new AddListener())
    removeButton = new JButton("<<")
    removeButton.addActionListener(new RemoveListener())
    destList = new JList(destListModel.toArray)
    val leftPanel = new JPanel(new BorderLayout())
    leftPanel.add(new JLabel("Available Elements:"), BorderLayout.NORTH)
    leftPanel.add(new JScrollPane(sourceList), BorderLayout.CENTER)
    leftPanel.add(addButton, BorderLayout.SOUTH)
    val rightPanel = new JPanel(new BorderLayout())
    rightPanel.add(new JLabel("Selected Elements:"), BorderLayout.NORTH)
    rightPanel.add(new JScrollPane(destList), BorderLayout.CENTER)
    rightPanel.add(removeButton, BorderLayout.SOUTH)
    add(leftPanel)
    add(rightPanel)
  }

  private class AddListener extends ActionListener {

    def actionPerformed(e: ActionEvent) {
      val selected = sourceList.getSelectedValuesList
      addDestinationElements(selected.toList.toArray)
      clearSourceSelected()
    }
  }

  private class RemoveListener extends ActionListener {

    def actionPerformed(e: ActionEvent) {
      val selected = destList.getSelectedValuesList
      addSourceElements(selected.toList.toArray)
      clearDestinationSelected()
    }
  }

}
