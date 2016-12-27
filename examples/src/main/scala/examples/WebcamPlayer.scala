package examples

import akka.actor.ActorSystem
import akka.stream.ActorMaterializerSettings
import com.jsuereth.video.filters.{FiltersRegistrator, HorizontalFlipFilter}
import com.jsuereth.video.swing


object WebcamPlayer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    val settings = ActorMaterializerSettings.create(system)
    def video() = com.jsuereth.video.WebCam.default(system)

    FiltersRegistrator()

    swing.createVideoPlayer(system, video)()
  }
}
