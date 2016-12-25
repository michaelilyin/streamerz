package com.jsuereth.video

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import org.reactivestreams.{Publisher, Subscriber}

/**
  * Created by michael on 18.12.16.
  */
object FilterChain {
  def apply(system: ActorSystem,
            videoSource: Publisher[VideoFrame],
            videoReceiver: Subscriber[VideoFrame])
           (implicit filters: Filter*): FilterChain = {
    new FilterChain(system, videoSource, videoReceiver, filters:_*)
  }
}

class FilterChain(system: ActorSystem,
                  videoSource: Publisher[VideoFrame],
                  videoReceiver: Subscriber[VideoFrame],
                  filters: Filter*) {

  private implicit val factory = system
  private implicit val materializer = ActorMaterializer(ActorMaterializerSettings create factory)

  def run(): Unit = {
    if (filters.isEmpty) {
      videoSource subscribe videoReceiver
    } else {
      val filter = filters.reverse reduce { (result, filter) => filter via result }
      val sink = Sink(videoReceiver)
      val source = Source(videoSource)
      filter to sink runWith source
    }
  }
}