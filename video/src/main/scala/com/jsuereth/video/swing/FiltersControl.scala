package com.jsuereth.video.swing

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{BorderLayout, Component, Dimension, GridLayout}
import java.util
import java.util.Comparator
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

  private val selector = new DualListBox(FiltersRegistry().toList)

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
      actor ! ApplyFiltersClicked(selector.selected.map(meta => FiltersRegistry(meta.key)))
  }

  clearButton addActionListener new ActionListener() {
    override def actionPerformed(e: ActionEvent): Unit = {
      selector.clear()
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

class DualListBox(var elements: List[FilterMeta]) extends JPanel {
  private val sourceModel = new java.util.Vector[FilterMeta]()
  private val destinationModel = new java.util.Vector[FilterMeta]()

  private val sourceList: JList[FilterMeta] = new JList(sourceModel)
  private val destinationList: JList[FilterMeta] = new JList(destinationModel)

  private val addButton = new JButton(">>")
  private val removeButton = new JButton("<<")

  def selected: List[FilterMeta] = destinationModel.toList

  initScreen()
  initData()

  def clear(): Unit = {
    initData()
    sourceList.updateUI()
    destinationList.updateUI()
  }

  private def initData() = {
    sourceModel.addAll(elements.sortBy(_.name))
    destinationModel.clear()
  }

  private def initScreen() {
    setLayout(new GridLayout(0, 2))
    addButton.addActionListener(new AddListener())
    removeButton.addActionListener(new RemoveListener())

    sourceList.setCellRenderer(new CellRenderer())
    destinationList.setCellRenderer(new CellRenderer())

    val leftPanel = new JPanel(new BorderLayout())
    leftPanel.add(new JLabel("Available Elements:"), BorderLayout.NORTH)
    leftPanel.add(new JScrollPane(sourceList), BorderLayout.CENTER)
    leftPanel.add(addButton, BorderLayout.SOUTH)

    val rightPanel = new JPanel(new BorderLayout())
    rightPanel.add(new JLabel("Selected Elements:"), BorderLayout.NORTH)
    rightPanel.add(new JScrollPane(destinationList), BorderLayout.CENTER)
    rightPanel.add(removeButton, BorderLayout.SOUTH)

    add(leftPanel)
    add(rightPanel)
  }

  private class AddListener extends ActionListener {
    def actionPerformed(e: ActionEvent) {
      val selected = sourceList.getSelectedValuesList
      destinationModel addAll selected
      sourceModel removeAll selected
      sourceList.getSelectionModel.clearSelection()
      sourceList.updateUI()
      destinationList.updateUI()
    }
  }
  private class RemoveListener extends ActionListener {
    def actionPerformed(e: ActionEvent) {
      val selected = destinationList.getSelectedValuesList
      sourceModel addAll selected
      sourceModel.sort(new Comparator[FilterMeta]() {
        override def compare(o1: FilterMeta, o2: FilterMeta): Int = o1.name.compareTo(o2.name)
      })
      destinationModel removeAll selected
      destinationList.getSelectionModel.clearSelection()
      destinationList.updateUI()
      sourceList.updateUI()
    }
  }

}