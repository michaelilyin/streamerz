package com.jsuereth.video.swing

import com.jsuereth.video.FilterChain


sealed trait UIControl
case object Play extends UIControl
case object Pause extends UIControl
case object Stop extends UIControl

case class ApplyFilter(filterChain: FilterChain) extends UIControl
case object ClearFilter extends UIControl