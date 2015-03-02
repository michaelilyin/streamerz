package com.jsuereth.ansi.ui

/** Events which the EventLoop will process. */
sealed trait UILoopItem

sealed trait Event extends UILoopItem

sealed trait Action extends UILoopItem


// Special ANSI escape sequences

/** An event when a key is pressed. */
case class KeyPress(key: Int) extends Event
case class UpKey() extends Event
case class DownKey() extends Event
case class RightKey() extends Event
case class LeftKey() extends Event
/** The cursor position being fired down. */
case class CursorPosition(row: Int, col: Int) extends Event


case class UnknownAnsiCode(code: String) extends Event

abstract class SpecialKey(key: Int) {
  def unapply(e: Event): Boolean =
    e match {
      case KeyPress(`key`) => true
      case _ => false
    }
}
// TODO - Don't special case these guys.
object Backspace extends SpecialKey(127)
object Enter extends SpecialKey(10)




/** An event that will render some text to the terminal. */
case class DisplayText(ansiString: String) extends Action


/** An event that will run a given runnable on the event thread. */
case class GenericRunnable(e: Runnable) extends Action
