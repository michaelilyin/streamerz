package com.jsuereth.video.filters

import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

import akka.stream.scaladsl.Flow
import com.jsuereth.video._

/**
  * Created by michael on 18.12.16.
  */
object HorizontalFlipFilter {
  val filter: Filter = {
    Flow[VideoFrame].map(frame => VideoFrame(createFlipped(frame.image), frame.timeStamp, frame.timeUnit))
  }

  FiltersRegistry.register("horizontal-flip-filter", "Horizontal flip filter",
    "Flip image in horizontal axis", filter)

  private def createFlipped(image: BufferedImage): BufferedImage = {
    val at = new AffineTransform()
    at.concatenate(AffineTransform.getScaleInstance(-1, 1))
    at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth, 0))
    createTransformed(image, at)
  }

  private def createTransformed(image: BufferedImage, at: AffineTransform): BufferedImage = {
    val newImage = new BufferedImage(
      image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB)
    val g = newImage.createGraphics()
    g.transform(at)
    g.drawImage(image, 0, 0, null)
    g.dispose()
    newImage
  }
}
