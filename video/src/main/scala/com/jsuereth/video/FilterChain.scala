package com.jsuereth.video

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import org.reactivestreams.{Publisher, Subscriber}

/**
  * Created by michael on 18.12.16.
  */
object FilterChain {
  def apply(filters: Filter*): FilterChain = {
    new FilterChain(filters:_*)
  }
}

class FilterChain(filters: Filter*) {

  def run(system: ActorSystem)
         (videoSource: Publisher[VideoFrame],
          videoReceiver: Subscriber[VideoFrame]): Unit = {
    if (filters.isEmpty) {
      videoSource subscribe videoReceiver
    } else {
      implicit val factory = system
      implicit val materializer = ActorMaterializer(ActorMaterializerSettings create factory)

      val filter = filters.reverse reduce { (result, filter) => filter via result }
      val sink = Sink(videoReceiver)
      val source = Source(videoSource)
      filter to sink runWith source
    }
  }
}