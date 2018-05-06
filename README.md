# OculusGoStreamer

A desktop streaming application for the OculusGo and GearVR (although untested).

- Cross platform using Java.
- Captures desktop with VLC using VLCj library.
- Compresses video using JPEG, PNG or GIF. 
- Uses custom interlacing and interframe compression.
- Streams over websockets.
- Supports Go virtual keyboard. Input is copied to clipboard on PC and then copy-pasted, so it can handle all characters.
- Supports Go pointer for mouse.

##Todo
- Improve latency. TCP makes it so packets can get stacked. Needs to send and receive frame timestamps to be able to read the actual latency and account for it. (Done)
- Browser side pull up menu so more options can be accessible.
- Such as fullscreen support.
- Or switching displays.
- Improve input latency for mouse cursor. Don't know why or if it can be improved much, but I think it can.
- Support bluetooth keyboard and other controllers.
- Video compression needs overall improvement. Perhaps the interframe method can be removed, but I think the bandwidth usage might otherwise become too high at cost of quality. Overall interframe compression is GPU-costly for browserside.
- Sound is missing.
- Bundle everything as single executable.
- Config file support or commandline parameters if sufficient.
- Maintain aspect ratio. Will also lessen bandwidth usage a tiny bit. (Done)
