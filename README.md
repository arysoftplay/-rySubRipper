# @rySubRipper v1.4 -> an OCR application for ripping hardcoded subtitles and saving them as .srt

<h2>Since version 1.3, two available OCR modes (learning/interactive and non-interactive mode) + non-OCR mode</h2>

About @rySubRipper

For several years I tried to find on the net the subtitles for some rare tv shows that came with hardcoded subtitles in their original language.

Having tried the few available applications available for ripping subs without success. I’ve finally started this project for personal use.

My objective was to quickly have a working application that produces accurate subtitles for series using the same set of characters through all the episodes. Of course, you can use @rySubRipper to rip movie subtitles. Just be aware that by starting each time with a different set of subtitle characters, the ripping process will take longer because of the OCR learning curve.

With that goal in mind, I chose a “binary comparison” technology as opposed to a standard OCR (more flexible but also more complex and unreliable).

That said, accuracy has its price and ripping a tv show episode takes a lot of user time even once the machine learned the basic characters.

The quality of the video and the contrast between the subtitles and the background images will have a huge 
impact on the OCR ripping time and the manual work. You can have scenes where the machine turns alone for 
several minutes and scenes where you have to interact or fix every subtitle. Ripping a 20-minute episode can 
take a few working hours. 

Since new version 1.3 has two OCR running modes + a non OCR mode: 
- Interactive (learning mode): the original working mode. It’s the mode that you need to use to teach the 
machine the characters. It’s also possible to work all the time in this mode so you make only one pass to 
get a readable subtitle. However, it’s a more consuming user time.
- Non interactive: this new mode can be used once the machine has learned enough characters to go on 
alone. This mode will treat the full movie all alone and flag every unknown character with a @.
You will then do a manual pass to fix the errors.
- Non OCR mode: you can always go fully manual by moving to each subtitle with the player controls and typing the subtitles into the edition text box.

As the first user, I tried to simplify the user tasks as much as possible by adding new features I identified while 
using the app. I think this new version will reduce a lot the user time as you can let the machine run a first pass 
without intervention. If you have used the previous versions, you’ll see the difference.

Please refer to the @rySubRipper_Doc.pdf available on the repository.

