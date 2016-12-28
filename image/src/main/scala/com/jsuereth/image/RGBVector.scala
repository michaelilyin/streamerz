package com.jsuereth.image

import scala.math._

object RGBVector {
  def fromRGB(rgb: Integer): RGBVector = 
    new RGBVector((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF)
	
  def black: RGBVector = new RGBVector(0, 0, 0)
  
  def white: RGBVector = new RGBVector(255, 255, 255)
}

class RGBVector(val red: Integer, val green: Integer, val blue: Integer) {
  
  def intensity = (red + green + blue)/3
  
  def +(that: RGBVector): RGBVector =
    new RGBVector(this.red + that.red, this.green + that.green, this.blue + that.blue)
	
  def euclidianDistance(that: RGBVector): Integer =
	sqrt((this.red - that.red)*(this.red - that.red) + 
	  (this.green - that.green)*(this.green - that.green) + 
	  (this.blue - that.blue)*(this.blue - that.blue)).toInt
	
  def *(that: Float): RGBVector =
    new RGBVector((that * this.red).toInt, round(that * this.green).toInt, round(that * this.blue).toInt)
	
  def toRGB: Integer =
    red << 16 | green << 8 | blue << 0
}