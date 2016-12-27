package com.jsuereth.video.swing

import java.awt.event.{ActionEvent, ActionListener}
import java.awt.{Dimension, GridLayout}
import javax.swing.{JButton, JComponent, JPanel}

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.stream.actor.ActorPublisher
import com.jsuereth.video.FilterChain
import com.jsuereth.video.filters.HorizontalFlipFilter
import org.reactivestreams.Publisher

private [swing] case object ApplyFiltersClicked
private [swing] case object ClearFiltersClicked

/**
  * Created by michael on 26.12.16.
  */
private[swing] class FiltersControls(actor: ActorRef) extends JPanel {
  private val applyButton = new JButton("Apply")
  private val clearButton = new JButton("Clear")

  this setPreferredSize new Dimension(640, 100)
  this setMinimumSize new Dimension(640, 100)
  this setLayout new GridLayout(1, 2)
  applyButton setMinimumSize new Dimension(200, 100)
  clearButton setMinimumSize new Dimension(200, 100)

  this add applyButton
  this add clearButton

  applyButton addActionListener new ActionListener() {
    override def actionPerformed(e: ActionEvent): Unit =
      actor ! ApplyFiltersClicked
  }

  clearButton addActionListener new ActionListener() {
    override def actionPerformed(e: ActionEvent): Unit =
      actor ! ClearFiltersClicked
  }

}

private[swing] class FiltersControlsActor extends ActorPublisher[UIControl] {
  sealed trait State
  case object Applied extends State
  case object Cleared extends State

  private var state: State = Cleared

  override def receive: Receive = {
    case ApplyFiltersClicked =>
      state match {
        case _ =>
          state = Applied
          import com.jsuereth.video.AsciiToVideo.asciiToVideo
          this onNext ApplyFilter(FilterChain(
            HorizontalFlipFilter.filter,
            com.jsuereth.video.AsciiVideo.colorAscii))
      }
    case ClearFiltersClicked =>
      state match {
        case Cleared =>  // ignore, already in correct state.
        case _ =>
          state = Cleared
          this onNext ClearFilter
      }
  }
}

object FiltersControls {

  def apply(factory: ActorRefFactory): (Publisher[UIControl], JComponent) = {
    val props = Props[FiltersControlsActor] withDispatcher "swing-dispatcher"
    val actorRef = factory actorOf (props, "filter-controls")
    val component = new FiltersControls(actorRef)
    ActorPublisher[UIControl](actorRef) -> component
  }

}
