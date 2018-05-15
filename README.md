# OculusGoStreamer
MacOS Note: Problems are being reported with VLC & Java. If you run into them, let me know
how I can improve things; I'm not a Mac user. The check-handling of architecture types also doesn't
really seem to match for MacOS, as MacOS uses both architecture types per binary. People
have gotten it to work, but I'm not up to date on what they did.

A desktop streaming application for the OculusGo and GearVR (although untested).
Basically works in any browser. Reported working with Nintendo Switch as well.

------ Made by Kaj Toet --- 

- Cross platform using Java.
- Captures desktop with VLC using VLCj library.
- Compresses video using JPEG, PNG or GIF. 
- Uses custom interlacing and interframe compression.
- Streams over websockets.
- Supports Go virtual keyboard. Input is copied to clipboard on PC and then copy-pasted, so it can handle all characters.
- Supports Go pointer for mouse.

## Instructions:
- Run from commandline: java -jar OculusGoStreamer.jar
- Or any other way you run Java programs.
- The console will give details about Java and VLC architectures. On Windows, a separate copy of
 VLC that matches Java architecture will be downloaded and put in a new directory 'vlc-override'.
- Visit the address printed in the terminal in your PCs browser first
 The address will have port 7578 by default.
 Note: The address printed may not be the correct address for your network architecture. If you get
 a 404, you need to use another address.
- It will print "Polling minimum achievable latency" for a few seconds, while the browser shows
 a black screen.
- And then should begin to stream.
- Visit the address in your browser (close the other stream, only 1 stream at a time can be open
 at one time).
- Click the keyboard icon to open the virtual keyboard.

## Notes
- Requires Java 1.6 or higher. VLCj for Mac OSX can only handle JVM 1.6 (? not sure).
- The vlcj included library requires version 2.1.0+ of VLC to be present.
- VLC needs to be same architecture as JVM. The console will print your Java and VLC architectures.
- If not working from start, you can download VLC here: http://download.videolan.org/pub/videolan/vlc/
- VLC 2.2.6 confirmed working. Other versions might work fine as well however.
- You can put the contents of VLC in a directory called 'vlc-override' next to the Jar,
and it will use that instead of the default VLC.
- More information about vlcj: http://capricasoftware.co.uk/#/projects/vlcj/tutorial/prerequisites

For OculusGo specifically:
- At 10% battery the connection or browser will be limited and streaming will stutter.
- When you have the headset off, the browser will keep running in the background, so close the app when you're done.

## config.json example
```
    {
    // Port to host server on
     "WEB_PORT": 7578,
    // Capture x, in this case second display
     "SCREEN_LEFT": 1920,
    // Capture y
     "SCREEN_TOP": 0,
    // Capture width
     "SCREEN_WIDTH": 1920,
    // Capture height
     "SCREEN_HEIGHT": 1080,
    // Framerate to maintain
     "FPS": 20,
    // Jpeg quality range
     "MIN_QUALITY": 0.3,
     "MAX_QUALITY": 1.0,
    // Jpeg quality adjustment per step, up or down
     "QUALITY_ALPHA": 0.05,
    // Skip is 1: no skip. Skip is 2: every other frame.
     "MAX_FRAME_SKIP": 3,
    // Allow for 2 extra frames of latency, then lower quality. Set higher for more skiping but nicer image
     "ADD_FRAMES_LATENCY": 2,
    // The following 3 parameters can be set to gif/png/jpeg
    // When jpeg quality range maxed out, switch to png
     "HIGH_FORMAT": "png",
    // Use jpeg for low quality. If set to gif or png, jpeg quality will only work as a "timer/buffer", has no affect on png/gif quality
     "LOW_FORMAT": "jpeg",
    // Format for interframes
     "INTERFRAME_FORMAT": "jpeg",
    }
```

## Todo
- Improve latency. TCP makes it so packets can get stacked. Needs to send and receive frame timestamps to be able to read the actual latency and account for it. (Done)
- Browser side pull up menu so more options can be accessible.
- Such as fullscreen support.
- Or switching displays.
- Improve input latency for mouse cursor. Don't know why or if it can be improved much, but I think it can. (Mostly done by fixing the video latency. Don't think it can be improved anymore.)
- Support bluetooth keyboard and other controllers.
- Video compression needs overall improvement. Perhaps the interframe method can be removed, but I think the bandwidth usage might otherwise become too high at cost of quality. Overall interframe compression is GPU-costly for browserside. (Mostly done)
- Sound is missing.
- Bundle everything as single executable. (Website dir now extracts from Jar, for Windows VLC is downloaded)
- Config file support or commandline parameters if sufficient. (Done, config.json)
- Maintain aspect ratio. Will also lessen bandwidth usage a tiny bit. (Done)
