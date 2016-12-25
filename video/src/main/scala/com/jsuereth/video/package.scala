package com.jsuereth

import akka.stream.scaladsl.Flow

/**
  * Created by Ilyina Ann on 25.12.16.
  */
package object video {
  type Filter = Flow[VideoFrame, VideoFrame, Unit]
}
