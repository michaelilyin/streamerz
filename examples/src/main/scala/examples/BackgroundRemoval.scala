package examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializerSettings
import akka.stream.scaladsl.{Sink, Source}
import com.jsuereth.video.AsciiToVideo.asciiToVideo
import com.jsuereth.video.filters.{FiltersRegistrator, BackgroundRemovalFilter}
import com.jsuereth.video.swing

object BackgroundRemoval {
  def main(args: Array[String]): Unit = {
    val url = "https://dl.dropboxusercontent.com/s/1suup2y9aej5xjw/480.mp4"
    implicit val system = ActorSystem()
    val settings = ActorMaterializerSettings.create(system)
    def video() = com.jsuereth.video.ffmpeg.readVideoURI(new java.net.URI(url), system, playAudio = false)
	
	FiltersRegistrator()

    swing.createVideoPlayer(system, video)()
  }
}