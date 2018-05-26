# OculusGoStreamer
A desktop streaming application for the OculusGo and GearVR (although untested).
Basically works in any browser. Tested on Android phone. Reported working with Nintendo Switch as well.

------ Made by Kaj Toet --- 

- Cross platform using Java.
- Captures desktop with VLC using VLCj library.
- Compresses video using JPEG, PNG or GIF. 
- Uses custom interlacing and interframe compression.
- Streams over websockets.
- Supports virtual keyboard. Input is copied to clipboard on PC and then copy-pasted, so it can handle all characters.
- Supports touch for mouse.
- Supports native keyboard events.
- Test results: ~22mbit/s at 60fps for a Call of Duty Youtube video @ 1920x1080.
 Will be lower if the client browser has a lower resolution as the video is downscaled before compression.
 The OculusGo browser has a resolution of 800x480 when not in fullscreen.

 - Stereo sound support implemented! Still some more latency inconsistencies, but works pretty well overall.

## Instructions:
- Download the Jar: https://github.com/Kjos/OculusGoStreamer/raw/master/out/artifacts/OculusGoStreamer_jar/OculusGoStreamer.jar
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
- Reloading the browser page will also reload config.json from file.
- Change raise resolution by lowering 'initial-scale=1.0' in index.html. 0.5 is resolution multiplier of two.
 Same for lowering resolution, but vice versa. The console output will print your resolution, for example:
 'Recorder set up, width: 1096, height: 617'.

## Notes
- Requires Java 1.6 or higher. VLCj for Mac OSX can only handle JVM 1.6 (? not sure).
- The vlcj included library requires version 2.1.0+ of VLC to be present.
- VLC 3 reported not working for Mac. Possibly Windows and Linux too (unverified), 
however Windows downloads VLC 2.2.6 from start.
- VLC 2.2.6 confirmed working. Any 2.x should probably work fine.
- VLC needs to be same architecture as JVM. The console will print your Java and VLC architectures.
- If not working from start, you can download VLC here: http://download.videolan.org/pub/videolan/vlc/
- You can put the contents of VLC in a directory called 'vlc-override' next to the Jar,
and it will use that instead of the default VLC.
- More information about vlcj: http://capricasoftware.co.uk/#/projects/vlcj/tutorial/prerequisites

For OculusGo specifically:
- At 10% battery the connection or browser will be limited and streaming will stutter.
- When you have the headset off, the browser will keep running in the background, so close the app when you're done.
- If you're experiencing issues connecting to the webserver, try rebooting the Oculus Go.

## config.json example
See:
https://github.com/Kjos/OculusGoStreamer/blob/master/config.json

## Todo
- Check SBS 3D support.
- Blit cursor without VLC for cross platform and dynamically toggle on/off
on movement pointer.
- Test sound different browsers.
- Improve latency. TCP makes it so packets can get stacked. Needs to send and receive frame timestamps to be able to read the actual latency and account for it. (Done)
- Browser side pull up menu so more options can be accessible. (Done)
- Such as fullscreen support. (Done)
- Or switching displays. (Done)
- Improve input latency for mouse cursor. Don't know why or if it can be improved much, but I think it can. (Mostly done by fixing the video latency. Don't think it can be improved anymore.)
- Support bluetooth keyboard and other controllers.
- Video compression needs overall improvement. Perhaps the interframe method can be removed, but I think the bandwidth usage might otherwise become too high at cost of quality. Overall interframe compression is GPU-costly for browserside. (Mostly done)
- Sound is missing. (Done, needs some improvement though)
- Bundle everything as single executable. (Website dir now extracts from Jar, for Windows VLC is downloaded)
- Config file support or commandline parameters if sufficient. (Done, config.json)
- Maintain aspect ratio. Will also lessen bandwidth usage a tiny bit. (Done)
