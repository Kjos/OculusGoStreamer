# OculusGoStreamer

A desktop streaming application for the OculusGo and GearVR (although untested).

------ Made by Kaj Toet --- 

- Cross platform using Java.
- Captures desktop with VLC using VLCj library.
- Compresses video using JPEG, PNG or GIF. 
- Uses custom interlacing and interframe compression.
- Streams over websockets.
- Supports Go virtual keyboard. Input is copied to clipboard on PC and then copy-pasted, so it can handle all characters.
- Supports Go pointer for mouse.

## Instructions:
- Install VLC. It's required. (vlcj requires version 2.1.0+ of VLC)
- Run from commandline: java -jar OculusGoStreamer.jar
- Or any other way you run Java programs.
- Visit the address printed in the terminal in your PCs browser, it will have port 7578.
- It will print "Polling minimum achievable latency" for a few seconds.
- And then should begin to stream.
- Visit the address in your Oculus browser.
- Click the keyboard icon to open the keyboard.

## config.json
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

## Notes
- Requires Java 1.6 or higher. VLCj for Mac OSX can only handle JVM 1.6.
- The vlcj included library requires version 2.1.0+ of VLC installed.
- VLC needs to be same architecture as JVM. Both 32 or both 64. You can check java version with "java -version".
- More information about vlcj: http://capricasoftware.co.uk/#/projects/vlcj/tutorial/prerequisites

- At 10% battery the connection or browser will be limited and streaming will stutter.
- When you have the headset off, the browser will keep running in the background, so close the app when you're done.

## Todo
- Improve latency. TCP makes it so packets can get stacked. Needs to send and receive frame timestamps to be able to read the actual latency and account for it. (Done)
- Browser side pull up menu so more options can be accessible.
- Such as fullscreen support.
- Or switching displays.
- Improve input latency for mouse cursor. Don't know why or if it can be improved much, but I think it can. (Mostly done by fixing the video latency. Don't think it can be improved anymore.)
- Support bluetooth keyboard and other controllers.
- Video compression needs overall improvement. Perhaps the interframe method can be removed, but I think the bandwidth usage might otherwise become too high at cost of quality. Overall interframe compression is GPU-costly for browserside. (Mostly done)
- Sound is missing.
- Bundle everything as single executable.
- Config file support or commandline parameters if sufficient. (Done, config.json)
- Maintain aspect ratio. Will also lessen bandwidth usage a tiny bit. (Done)
