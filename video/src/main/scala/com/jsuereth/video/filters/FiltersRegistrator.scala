package com.jsuereth.video.filters

import com.jsuereth.video.AsciiVideo

/**
  * Created by michael on 27.12.16.
  */
object FiltersRegistrator {
  import com.jsuereth.video.AsciiToVideo.asciiToVideo

  def apply(): Unit = {
    FiltersRegistry.register("horizontal-flip-filter", "Horizontal flip filter",
      "Flip image in horizontal axis", HorizontalFlipFilter.filter)

    FiltersRegistry.register("pixel-ascii", "Ascii pixel",
      "Represents image in ascii background colors", AsciiVideo.pixelAscii)

    FiltersRegistry.register("color-ascii", "Ascii color",
      "Represents image in ascii colored characters", AsciiVideo.colorAscii)

    FiltersRegistry.register("black-white-ascii", "Ascii black and white",
      "Represents image in ascii blue and white characters", AsciiVideo.bwAscii)

    FiltersRegistry.register("greyscale-ascii", "Ascii greyscale",
      "Represents image in ascii greyscale colors", AsciiVideo.greyscaleAscii)
	  
	FiltersRegistry.register("background-removal-filter", "Background removal filter",
      "Removes static background & leaves only moving objects", ForegroundFilters.backgroundRemoval)
	  
	FiltersRegistry.register("foreground-removal-filter", "Foreground removal filter",
      "Removes moving objects & leaves only static background", ForegroundFilters.foregroundRemoval)
	  
	FiltersRegistry.register("foreground-mask-filter", "Foreground mask filter",
      "Shows black & white foreground mask", ForegroundFilters.foregroundMask)
	  
	FiltersRegistry.register("background-modelling-long-term-filter", "Background modelling filter (long-term)",
      "Shows static background model calculated from frame history", ForegroundFilters.backgroundModellingLongTerm)
	  
	FiltersRegistry.register("background-modelling-short-term-filter", "Background modelling filter (short-term)",
      "Shows static background model calculated from frame history", ForegroundFilters.backgroundModellingShortTerm)
  }
}
