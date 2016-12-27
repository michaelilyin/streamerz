package com.jsuereth.video.filters

import com.jsuereth.video.AsciiVideo

/**
  * Created by michael on 27.12.16.
  */
object AsciiFiltersRegistrator {
  import com.jsuereth.video.AsciiToVideo.asciiToVideo

  FiltersRegistry.register("pixel-ascii", "Ascii pixel",
    "Represents image in ascii background colors", AsciiVideo.pixelAscii)

  FiltersRegistry.register("color-ascii", "Ascii color",
    "Represents image in ascii colored characters", AsciiVideo.colorAscii)

  FiltersRegistry.register("black-white-ascii", "Ascii black and white",
    "Represents image in ascii blue and white characters", AsciiVideo.bwAscii)

  FiltersRegistry.register("greyscale-ascii", "Ascii greyscale",
    "Represents image in ascii greyscale colors", AsciiVideo.greyscaleAscii)
}
