
function sendCommand(command, value) {
	var send = {};
	send[command] = value;
	if (websocket && websocket.readyState !== websocket.CLOSED && 
		websocket.readyState !== websocket.CONNECTING) {
		websocket.send(JSON.stringify(send));
	}
}

function getCursor(e) {
	var pos = [0,0];
	if(e.type == 'touchstart' || e.type == 'touchmove' || e.type == 'touchend' || e.type == 'touchcancel'){
		var touch = e.originalEvent.touches[0] || e.originalEvent.changedTouches[0];
		pos[0] = touch.pageX;
		pos[1] = touch.pageY;
	} else if (e.type == 'mousedown' || e.type == 'mouseup' || e.type == 'mousemove' || e.type == 'mouseover'|| e.type=='mouseout' || e.type=='mouseenter' || e.type=='mouseleave') {
		pos[0] = e.pageX;
		pos[1] = e.pageY;
	}
	pos[0] -= (window.innerWidth - canvas.width) / 2;
	pos[1] -= (window.innerHeight - canvas.height) / 2;
	pos[0] *= 10000;
	pos[1] *= 10000;
	pos[0] /= canvas.width;
	pos[1] /= canvas.height;

	if (pos[0] < 0 || pos[1] < 0 || pos[0] > 10000 || pos[1] > 10000) {
		return false;
	} else {
		return pos;
	}
}

var lastmovetime = Date.now();
function inputSetup() {
	var keysDown = new Array();
	$(window).on({ 'keydown' : function( e ) {
			if (!keysDown[e.keyCode]) {
				keysDown[e.keyCode] = true;
				sendCommand("keyDown", e.keyCode);
				console.log("keyDown: " + e.keyCode);
			}
		}
	});
	$(window).on({ 'keyup' : function( e ) {
			sendCommand("keyUp", e.keyCode);
			keysDown[e.keyCode] = false;
		}
	});
	$("#canvas").on({ 'mousemove touchmove' : function( e ) {
			e.preventDefault();

			var t = Date.now();
			if (t - lastmovetime < 30) return;

			var pos = getCursor(e);
			if (!pos) return;

			lastmovetime = t;
			sendCommand("mouseMove", pos);
		}
	});
	$("#canvas").on({ 'mousedown touchstart' : function( e ) {
			e.preventDefault();

			var pos = getCursor(e);
			if (!pos) return;

			$("#keyboardHack").blur();
			console.log("touch start");
			sendCommand("mousePress", pos);
		}
	});
	$("#canvas").on({ 'mouseup touchend touchcancel' : function( e ) {
			e.preventDefault();
			
			var pos = getCursor(e);
			if (!pos) return;

			console.log("touch end");
			sendCommand("mouseRelease", pos);
		}
	});

	setInterval(keyboardCheck, 100);
}

function keyboardCheck() {
// OculusGo doesn't handle input listeners correctly.
// Need to check every once in a while
	var str = $("#keyboardHack").val();
	if (str.length > 1) {
		sendCommand("keys", str.substring(1));
		$("#keyboardHack").val(' ');
	} else if (str.length == 0) {
		sendCommand("backspace", true);
		$("#keyboardHack").val(' ');
	}
}

var ipCanvas;
var ipCtx;
function canvasResize(image, force) {
	var height = image.height * 2;

	var createIpCanvas = !ipCanvas || ipCanvas[0].height != height;

	if (force || createIpCanvas || canvas.width != image.width || canvas.height != height) {	
		canvas.width = image.width;
		canvas.height = height;
        	canvas.style.position = "absolute";
		canvas.style.left = ((window.innerWidth - image.width) / 2) + "px";
		canvas.style.top = ((window.innerHeight - height) / 2) + "px";
		
		frameCanvas.width = image.width;
		frameCanvas.height = height;
		frameCanvas2.width = image.width;
		frameCanvas2.height = image.height;
		ctx = canvas.getContext("2d");
		fctx = frameCanvas.getContext('2d');
		fctx2 = frameCanvas2.getContext('2d');
		ctx.imageSmoothingEnabled = false;
		fctx.imageSmoothingEnabled = false;
		fctx2.imageSmoothingEnabled = false;
	}

	if (createIpCanvas) {
		ipCanvas = new Array();
		ipCtx = new Array();

		for (var i = 0; i < 2; i++) {
			ipCanvas[i] = document.createElement('canvas');
			ipCanvas[i].width = 1;
			ipCanvas[i].height = height;
			ipCtx[i] = ipCanvas[i].getContext('2d');
			var imgData = ipCtx[i].getImageData(0, 0, 1, height);
			var bytes = imgData.data;
			var val1 = i == 0 ? 255 : 0;
			var val2 = i == 1 ? 255 : 0;
			for (var k = 0; k < bytes.length;) {
				bytes[k] = val1;
				k++;
				bytes[k] = val1;
				k++;
				bytes[k] = val1;
				k++;
				bytes[k] = val2;
				k++;
				
				if (k >= bytes.length) break;

				bytes[k] = val2;
				k++;
				bytes[k] = val2;
				k++;
				bytes[k] = val2;
				k++;
				bytes[k] = val1;
				k++;
			}
			ipCtx[i].putImageData(imgData,0,0);
		}
	}
}

function frameCompositing() {
	if (this.type == 1 || this.type == 2) {
		canvasResize(this, false);
		fctx.globalCompositeOperation = "source-over";
		fctx.drawImage(this, 0, 0, canvas.width, canvas.height);

		fctx.globalCompositeOperation = "source-atop";
		fctx.drawImage(ipCanvas[this.ip1], 0, 0, canvas.width, canvas.height);

		ctx.globalCompositeOperation = "source-atop";
		ctx.drawImage(ipCanvas[this.ip2], 0, 0, canvas.width, canvas.height);

		ctx.globalCompositeOperation = "lighter";
		ctx.drawImage(frameCanvas, 0, 0, canvas.width, canvas.height);
	} else if ((this.type == 3 || this.type == 4) && this.keyFrame) {
// Copy image
		fctx2.globalCompositeOperation = "source-over";
		fctx2.drawImage(this, 0, 0, canvas.width, frameCanvas2.height);

// Take lower half
		fctx2.globalCompositeOperation = "darken";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Multiply by 2
		fctx2.globalCompositeOperation = "color-dodge";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Invert
		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Subtract with keyframe
		fctx2.globalCompositeOperation = "difference"; 
		fctx2.drawImage(this.keyFrame, 0, 0, canvas.width, frameCanvas2.height);

		fctx.globalCompositeOperation = "source-over";
		fctx.drawImage(frameCanvas2, 0, 0, canvas.width, canvas.height);

		// ------
// Copy
		fctx2.globalCompositeOperation = "source-over";
		fctx2.drawImage(this, 0, 0, canvas.width, frameCanvas2.height);

// Take upper half
		fctx2.globalCompositeOperation = "lighten";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Invert
		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Multiply
		fctx2.globalCompositeOperation = "color-dodge";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Invert
		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

// Add to stored fb
		fctx.globalCompositeOperation = "lighter";
		fctx.drawImage(frameCanvas2, 0, 0, canvas.width, canvas.height);

// Interlace
		fctx.globalCompositeOperation = "source-atop";
		fctx.drawImage(ipCanvas[this.ip1], 0, 0, canvas.width, canvas.height);

// Interlace 2
		ctx.globalCompositeOperation = "source-atop";
		ctx.drawImage(ipCanvas[this.ip2], 0, 0, canvas.width, canvas.height);

// Merge both
		ctx.globalCompositeOperation = "lighter"; 
		ctx.drawImage(frameCanvas, 0, 0, canvas.width, canvas.height);
	}
};

var canvas;
var ctx;
var frameCanvas;
var fctx;
var frameCanvas2;
var fctx2;
var rCanvas;
var rctx;
var websocket;
var lastKeyFrame;
var lastKeyData;
var urlCreator = window.URL || window.webkitURL;

var frameTime = Date.now();
var frameCnt = 0;
function connectWebSocket() {
	if (websocket && websocket.readyState !== websocket.CLOSED) return;

	var source = "ws://" + window.location.host + "/websocket?t=" + new Date().getTime();
	websocket = new WebSocket(source);
	websocket.binaryType = 'arraybuffer';
	websocket.onopen = function () {
		console.log("connected");
		lastKeyFrame = new Array();
		lastKeyData = new Array();
		sendCommand("window", [window.innerWidth, window.innerHeight]);
	};
	websocket.onclose = function () {
		console.log("disconnected");
	};
	websocket.onmessage = function (msg) {
		var bytes = new Uint8Array(msg.data);
		var type = bytes[0];
		
		if (type == 0) {
			// Workaround for Chrome bug?
			// In case this is missing, frames stutter
			if (ctx) {
				ctx.globalCompositeOperation = "source-over";
				ctx.drawImage(canvas, 0, 0, canvas.width, canvas.height);
			}
			var framestamp = bytes[4];
			framestamp <<= 8;
			framestamp |= bytes[3];
			framestamp <<= 8;
			framestamp |= bytes[2];
			framestamp <<= 8;
			framestamp |= bytes[1];

			websocket.send(">" + framestamp);
		} else {
			frameCnt++;
			if (frameCnt > 10) {
				var nft = Date.now();
				var fps = Math.floor(1000 * 10 / (nft - frameTime));
				$("#fps").html("FPS: " + fps);
				frameTime = nft;
				frameCnt = 0;
			}

			var compression = bytes[1];
			var image = new Image();
			image.ip1 = type % 2 == 0 ? 0 : 1;
			image.ip2 = 1 - image.ip1;
			image.type = type;
			var framestamp = bytes[5];
			framestamp <<= 8;
			framestamp |= bytes[4];
			framestamp <<= 8;
			framestamp |= bytes[3];
			framestamp <<= 8;
			framestamp |= bytes[2];
			console.log(framestamp);

			if (type == 1 || type == 2) {
				lastKeyFrame[type-1] = image;
			} else {
				image.keyFrame = lastKeyFrame[type-3];
			}
			image.frameLoad = frameCompositing;
			image.onload = function() {
				image.frameLoad(); 
				websocket.send(">" + framestamp);
			};
			var imageFormat;
			switch(compression) {
				case 0: imageFormat = "jpeg";
					break;
				case 1: imageFormat = "gif";
					break;
				case 2: imageFormat = "png";
					break;
			}
			var blob = new Blob( [ bytes.subarray(6) ], { type: "image/" + imageFormat } );
			image.src = urlCreator.createObjectURL( blob );
			delete blob;
		}
		delete bytes;

	};
	websocket.onerror = function (msg) {
		console.log('error: ' + msg.data);
		websocket.close();
	};
}

function toggleFullScreen() {
  if ((document.fullScreenElement && document.fullScreenElement !== null) ||    // alternative standard method
      (!document.mozFullScreen && !document.webkitIsFullScreen)) {               // current working methods
    if (document.documentElement.requestFullScreen) {
      document.documentElement.requestFullScreen();
    } else if (document.documentElement.mozRequestFullScreen) {
      document.documentElement.mozRequestFullScreen();
    } else if (document.documentElement.webkitRequestFullScreen) {
      document.documentElement.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
    }
  } else {
    if (document.cancelFullScreen) {
      document.cancelFullScreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.webkitCancelFullScreen) {
      document.webkitCancelFullScreen();
    }
  }
}

var isActive = false;
var pollTimeout = null;
function poll() {
	if (pollTimeout) clearTimeout(pollTimeout);
	pollTimeout = setTimeout(function() {
		sendCommand("window", [window.innerWidth, window.innerHeight]);
	}, 500);
}

function menuInit() {
	$(".menu-nextscreen").click(function() {
		sendCommand("screenSwitch", true);
	});
	$(".menu-fullscreen").click(toggleFullScreen);
	$(".menu-open").click(function() {
		if ($(".menu-contents").css("visibility") == "hidden") {
			$(".menu-contents").css("visibility", "visible");
		} else {
			$(".menu-contents").css("visibility", "hidden");
		}
	});
}

$(document).ready(function(){
	canvas = document.getElementById("canvas");
	var ctx = canvas.getContext("2d");
	ctx.font = "30px Arial";
	ctx.fillStyle = 'rgb(255,255,255)';
	ctx.fillText("OculusGoStreamer - Kaj Toet", 10, 50);

	frameCanvas = document.createElement('canvas');
	frameCanvas2 = document.createElement('canvas');

	inputSetup();
	connectWebSocket();

	window.onresize = poll;

	menuInit();
});
