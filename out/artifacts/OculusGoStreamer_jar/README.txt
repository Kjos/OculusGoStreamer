OculusGo DesktopStreamer v3 
----------------------------------- by Kaj Toet

Instructions:
- Install VLC. It's required.
- Run from commandline: java -jar OculusGoStreamer.jar
- Or any other way you run Java programs.
- Visit the address printed in the terminal, it will have port 7578.
- Visit the address in your Oculus browser.
- Click the keyboard icon to open the keyboard.

Notes:
- Works on all platforms with Java and VLC support.
- VLC version might matter, I guess the latest is best.
- There is quite some latency when using the Oculus controller.
- Default setting are primary window on a 1920x1080 display. If your monitor isn't 1920x1080, things might not work.
- Interlacing isn't very nice, but it keeps the decompression of the images lightweight. 
But it might be changed in the future.
- After removing your headset, refresh the page possible if it stopped.

TBC

Changes from v3:
- Fixed issue with interlacing artifacts that were due to mismatching resolution. Looks better and uses less bandwidth.

Changes from v2:
- Keyboard uses clipboard now. Now supports all characters on Oculus onscreen keyboard.

Changes from v1:
- Added keyboard. Not complete yet.
