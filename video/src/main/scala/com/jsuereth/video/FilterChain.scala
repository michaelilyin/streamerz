package com.jsuereth.video

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Graph}
import akka.stream.actor.ActorSubscriber
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import com.jsuereth.video.FilterChain.Filter
import org.reactivestreams.{Publisher, Subscriber}

/**
  * Created by michael on 18.12.16.
  */
object FilterChain {
  type Filter = Flow[VideoFrame, VideoFrame, Unit]

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
  if (filters.isEmpty) {
    videoSource subscribe videoReceiver
  } else {
    val filter = filters.reverse reduce { (result, filter) => filter via result }
    val sink = Sink(videoReceiver)
    val source = Source(videoSource)
    implicit val factory = system
    implicit val materializer = ActorMaterializer(ActorMaterializerSettings create factory)
    filter to sink runWith source
  }
}