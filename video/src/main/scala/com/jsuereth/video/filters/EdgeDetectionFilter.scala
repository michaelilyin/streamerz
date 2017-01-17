package com.jsuereth.video.filters

import java.awt.image.{DataBufferByte, BufferedImage}
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

import akka.stream.scaladsl.Flow
import com.jsuereth.video.{Filter, VideoFrame}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable

object Test extends App {
  val image = ImageIO.read(Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.png").toFile)
  var result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.0, 0.0).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.1.png").toFile)
  result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.0, 0.25).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.2.png").toFile)
  result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.0, 0.5).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.3.png").toFile)
  result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.0, 0.75).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.4.png").toFile)
  result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.0, 1.0).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.5.png").toFile)
  result = EdgeDetectionFilter.map(VideoFrame(image, 0, TimeUnit.SECONDS), 0.45, 0.9).image
  ImageIO.write(result, "png", Paths.get("/Volumes/Data/University/Magistracy/2_course/FP/lena.out.png").toFile)
}

object EdgeDetectionFilter {

  private val logger = Logger(LoggerFactory.getLogger("EdgeDetectionFilter"))

  val filter: Filter = Flow[VideoFrame].map(vf => map(vf, 0.45, 0.9))

  private class PixelSource(val image: BufferedImage) {

    val pixels = image.getRaster.getDataBuffer.asInstanceOf[DataBufferByte].getData()
    val width = image.getWidth
    val height = image.getHeight
    val imageType = image.getType
    type Extractor = (Int, Int) => Int

    val intExtractor: Extractor = (x, y) => {
      val idx = y * width + x
      val pixel = pixels(idx)
      val r = (pixel >> 16).toByte
      val g = (pixel >> 8).toByte
      val b = pixel.toByte
      (r + g + b) / 3
    }

    val threeBytesExtractor: Extractor = (x, y) => {
      val idx = y * 3 * width + x * 3
      val r = pixels(idx + 2)
      val g = pixels(idx + 1)
      val b = pixels(idx)
      (r + g + b) / 3
    }

    val fourBytesExtractor: Extractor = (x, y) => {
      val idx = y * 4 * width + x * 4
      val r = pixels(idx + 3)
      val g = pixels(idx + 2)
      val b = pixels(idx + 1)
      (r + g + b) / 3
    }

    val extractor: Extractor = imageType match {
      case BufferedImage.TYPE_CUSTOM => {
        val len = width * height
        val threeLen = len  * 3
        val fourLen = len * 4
        pixels.length match {
          case `len` => intExtractor
          case `threeLen`  => threeBytesExtractor
          case `fourLen` => fourBytesExtractor
        }
      }
      case BufferedImage.TYPE_3BYTE_BGR => threeBytesExtractor
      case BufferedImage.TYPE_4BYTE_ABGR => fourBytesExtractor
      case BufferedImage.TYPE_INT_ARGB | BufferedImage.TYPE_INT_RGB => intExtractor
      case _ =>
        logger.info(s"Unsupported image type: $imageType")
        throw new UnsupportedOperationException(s"$imageType")
    }


    def pixel(x: Int, y: Int): Int = extractor(x, y)
  }

  private val sobelX = Array[Array[Int]](
    Array(-1, 0, 1),
    Array(-2, 0, 2),
    Array(-1, 0, 1)
  )

  private val sobelY = Array[Array[Int]](
    Array(-1, -2, -1),
    Array(0, 0, 0),
    Array(1, 2, 1)
  )

  private val bufferCache = new mutable.HashMap[Int, Array[Int]]()
  private val imageCache = new mutable.HashMap[(Int, Int), BufferedImage]()

  def map(frame: VideoFrame, lowerThreshold: Double, higerThreshold: Double): VideoFrame = {
    try {
      val lowerThresholdValue = (255 * lowerThreshold).toInt
      val higerThresholdValue = (255 * higerThreshold).toInt
      val image = frame.image
      val source = new PixelSource(image)
      val sizes = (image.getWidth, image.getHeight)

      val size = sizes._1 * sizes._2
      if (!bufferCache.contains(size))
        bufferCache += ((size, new Array[Int](size)))
      val result = bufferCache(size)

      for (x <- 1 to sizes._1 - 2) {
        for (y <- 1 to sizes._2 - 2) {
          val pixelX = (sobelX(0)(0) * source.pixel(x - 1, y - 1)) + (sobelX(0)(1) * source.pixel(x, y - 1)) + (sobelX(0)(2) * source.pixel(x + 1, y - 1)) +
            (sobelX(1)(0) * source.pixel(x - 1, y)) + (sobelX(1)(1) * source.pixel(x, y)) + (sobelX(1)(2) * source.pixel(x + 1, y)) +
            (sobelX(2)(0) * source.pixel(x - 1, y + 1)) + (sobelX(2)(1) * source.pixel(x, y + 1)) + (sobelX(2)(2) * source.pixel(x + 1, y + 1))

          val pixelY = (sobelY(0)(0) * source.pixel(x - 1, y - 1)) + (sobelY(0)(1) * source.pixel(x, y - 1)) + (sobelY(0)(2) * source.pixel(x + 1, y - 1)) +
            (sobelY(1)(0) * source.pixel(x - 1, y)) + (sobelY(1)(1) * source.pixel(x, y)) + (sobelY(1)(2) * source.pixel(x + 1, y)) +
            (sobelY(2)(0) * source.pixel(x - 1, y + 1)) + (sobelY(2)(1) * source.pixel(x, y + 1)) + (sobelY(2)(2) * source.pixel(x + 1, y + 1))

          val level = Math.ceil(Math.sqrt((pixelX * pixelX) + (pixelY * pixelY))).toInt
          val adjustedByLow = if (level < lowerThresholdValue) 0 else level
          val adjustedByHigh = if (level > higerThresholdValue) 0 else adjustedByLow
          result(y * image.getWidth + x) = adjustedByHigh
        }
      }

      if (!imageCache.contains(sizes))
        imageCache += ((sizes, new BufferedImage(sizes._1, sizes._2, BufferedImage.TYPE_BYTE_GRAY)))
      val edges = imageCache(sizes)
      edges.getRaster.setPixels(0, 0, sizes._1, sizes._2, result)
      VideoFrame(edges, frame.timeStamp, frame.timeUnit)
    } catch {
      case e: Throwable =>
        logger.info("Error:", e)
        throw e
    }
  }
}
