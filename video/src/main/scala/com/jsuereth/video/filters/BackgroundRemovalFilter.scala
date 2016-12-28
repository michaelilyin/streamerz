package com.jsuereth.video.filters

import java.awt.Color
import java.awt.image.BufferedImage
import akka.stream.scaladsl.Flow
import com.jsuereth.image.RGBVector
import com.jsuereth.video._

object BackgroundRemovalFilter {

  val filter: Filter = {
    val filterInstance = new BackgroundRemovalFilter
    Flow[VideoFrame].map(frame => VideoFrame(filterInstance.frameReceived(frame.image), frame.timeStamp, frame.timeUnit))
  }
  
}

class BackgroundRemovalFilter(val learningRateShort: Float = 0.4f, val thresholdBottom: Integer = 12, val hoodRadius: Integer = 2) {
  require(learningRateShort > 0.0f && learningRateShort < 1.0f, "learning rate must be between 0.0 and 1.0")
  require(thresholdBottom > 0, "bottom threshold must be positive")
  
  private val learningRateLong = learningRateShort/10
  private val thresholdTop = 2*thresholdBottom
  
  var initialized = false
  var mask: Array[Array[Boolean]] = _
  var shortTermModel: Array[Array[RGBVector]] = _
  var longTermModel: Array[Array[RGBVector]] = _
  
  private def init(width: Integer, height: Integer) = {
    mask = Array.tabulate(width, height)((x, y) => false)
    shortTermModel = Array.tabulate(width, height)((x, y) => new RGBVector(0, 0, 0))
    longTermModel = Array.tabulate(width, height)((x, y) => new RGBVector(0, 0, 0))
	initialized = true
  }
  
  private def foregroundInNeighborhood(x: Integer, y: Integer): Boolean = {
	var xOffset, yOffset = 0
	for(xOffset <- -hoodRadius to hoodRadius; yOffset <- -hoodRadius to hoodRadius) {
		val (xCurrent, yCurrent) = (x+xOffset, y+yOffset)
		if (xCurrent>0 && yCurrent>0 && (xCurrent<mask.length) && (yCurrent<mask(xCurrent).length) && mask(xCurrent)(yCurrent))
			return true
	}
    false
  }
	  
  private def shortTermCheck(x: Integer, y: Integer, color: RGBVector): Boolean = {
	  if ((color euclidianDistance shortTermModel(x)(y)) > thresholdTop)
	    true
	  else
	    false
  }
  private def longTermCheck(x: Integer, y: Integer, color: RGBVector): Boolean = {
    val value = color euclidianDistance longTermModel(x)(y)
    if (value >= thresholdTop)
	  true
    else if (value < thresholdBottom)
	  false
    else
	  foregroundInNeighborhood(x, y)
  }
	  
  private def updateModel(model: Array[Array[RGBVector]], color: RGBVector, x: Integer, y: Integer, alpha: Float) =
    model(x)(y) = color * alpha + model(x)(y) * (1.0f - alpha)
  
  def frameReceived(image: BufferedImage): BufferedImage = {
    if (!initialized)
	  init(image.getWidth, image.getHeight)

	val rows = (0 until image.getHeight).toList.par
	rows.foreach { y => {
	  var x = 0
	  for(x <- 0 until image.getWidth) {
	    val color = RGBVector.fromRGB(image.getRGB(x,y))
	    val alpha = (1 - (if (mask(x)(y)) 1 else 0)) * learningRateLong
		
	    mask(x)(y) = shortTermCheck(x, y, color) & longTermCheck(x, y, color)
	    if (!mask(x)(y))
		  image.setRGB(x,y,0)
		  
		updateModel(longTermModel, color, x, y, alpha)
		updateModel(shortTermModel, color, x, y, learningRateShort)
	  }
	}}
	image
  }

}