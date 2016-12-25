package com.jsuereth.video

import java.awt.Font
import java.awt.image.BufferedImage

import akka.stream.scaladsl.Flow
import com.jsuereth.ansi.AnsiColors

object AsciiToVideo {

  implicit def asciiToVideo(asciiFlow: Flow[VideoFrame, AsciiVideoFrame, Unit]): Flow[VideoFrame, VideoFrame, Unit] = {
    asciiFlow map {
      avf => convert(avf)
    }
  }

  private def convert(avf: AsciiVideoFrame) = {
    try {
      VideoFrame(renderAsciiToImage(avf.image), avf.timeStamp, avf.timeUnit)
    } catch {
      case e: Exception =>
        println(e.getMessage)
        VideoFrame(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB), avf.timeStamp, avf.timeUnit)
    }
  }

  private val letterWidth = 10
  private val letterHeight = 10

  private val font = new Font("monospaced", Font.PLAIN, (letterHeight * 1.6).toInt)
  private val colorGroupStartPattern = 27.toChar + "\\["

  def renderAsciiToImage(image: String): BufferedImage = {
    val lines = image split "\n"
    val (height, width) = getDimensions(lines)

    val picture = new BufferedImage(
      width * letterWidth,
      height * letterHeight,
      BufferedImage.TYPE_INT_RGB
    )
    val graphics = picture.getGraphics
    graphics setFont font

    (lines foldLeft 0) {
      (offsetY, line) => {
        val colorGroups = line split colorGroupStartPattern
        (colorGroups foldLeft 0) {
          (offsetX, colorGroup) => {
            if (!colorGroup.isEmpty) {
              val (fbFlag, ansiColor, text) = parseColorGroup(colorGroup)
              graphics setColor AnsiColors(ansiColor)
              graphics drawString(text, offsetX, offsetY)
              offsetX +letterWidth * text.length
            } else {
              offsetX
            }
          }
        }
        offsetY + letterHeight
      }
    }

    picture
  }

  private def getDimensions(lines: Array[String]): (Int, Int) = {
    val firstLine = lines(0) split colorGroupStartPattern
    val firstLineChars = firstLine map {
      group => {
        group dropWhile (_ != 'm') drop 1
      }
    } flatMap (_.toCharArray)

    (lines.length, firstLineChars.length)
  }

  private def parseColorGroup(colorGroup: String): (String, Int, String) = {
    (
      colorGroup take 2,
      Integer parseInt (colorGroup drop (colorGroup lastIndexOf ";") takeWhile (_ != 'm') drop 1),
      colorGroup dropWhile (_ != 'm') drop 1
    )
  }

}