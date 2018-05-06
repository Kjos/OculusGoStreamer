

function parseError(xhr) {
	isActive = false;
	return;

	switch (xhr.status) {
		case 500: 
			$("#message").html("Invalid key. <a href=\"#\" onclick=\"pollDelay(); return false;\">Retry</a>");
			break;
		case 501: 
			$("#message").html("Server full. <a href=\"#\" onclick=\"pollDelay(); return false;\">Retry</a>");
			break;
		case 502: 
			$("#message").html("Logged out. <a href=\"#\" onclick=\"poll(); return false;\">Login</a>");
			break;
		default:
			$("#message").html("No connection with server. Status: " + xhr.status + 
				" <a href=\"#\" onclick=\"pollDelay(); return false;\">Retry</a>");
	}
}

function sendCommand(command, value) {
	var send = {};
	send[command] = value;
	if (websocket && websocket.readyState !== websocket.CLOSED) {
		websocket.send(JSON.stringify(send));
	}
}

var lastmovetime = Date.now();
function inputSetup() {
	$("#canvas").mousemove(function( event ) {
		var t = Date.now();
		if (t - lastmovetime < 30) return;

		var pos = [event.pageX, event.pageY];
		pos[0] -= (window.innerWidth - canvas.width) / 2;
		pos[1] -= (window.innerHeight - canvas.height) / 2;
		pos[0] *= 10000;
		pos[1] *= 10000;
		pos[0] /= canvas.width;
		pos[1] /= canvas.height;

		if (pos[0] < 0 || pos[1] < 0 || pos[0] > 10000 || pos[1] > 10000) return;

		lastmovetime = t;
		sendCommand("mouseMove", pos);
	});
	$("#canvas").on({ 'touchstart' : function( event ) {
			sendCommand("mousePress", 1);
		}
	});
	$("#canvas").on({ 'touchend' : function( event ) {
			sendCommand("mouseRelease", 1);
		}
	});
	checkKeyInput();
}

// OculusGo doesn't handle input listeners correctly.
// Need to check every once in a while
function checkKeyInput() {
	var str = $("#keyboardHack").val();
	if (str.length > 0) {
		sendCommand("keys", str);
		$("#keyboardHack").val('');
	}
	setTimeout(checkKeyInput, 30);
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
		if($("#debugBFrame").is(':checked')) {
			ctx.globalCompositeOperation = "copy";
			ctx.drawImage(this, 0, 0, canvas.width, canvas.height);
			return;
		}

		// key: 255, 1.0, inter: 0, 0, 0.0
		// key: 0, 0.0, inter: 255, 255, 1.0
		// key: 128, 0.5, inter: 255, 196, 0.75
		// key: 128, 0.5, inter: 0, 64, 0.25

		// darken
		// 0.0, 0.5, 0.5, 0.25

		// screen, 0.5
		// 0, 1, 1, 0.5

		// difference: 1
		// 1, 0, 0, 0.5

		// difference key
		// res: 0, 0, 0.5, 0.0

		//------------------------

		// lighten 0.5
		// 0.5, 1, 0.75, 0.5

		// difference: 1
		// 0.5, 0, 0.25, 0.5

		// screen, 0.5
		// 1, 0, 0.5, 1

		// difference: 1
		// 0, 1, 0.5, 0

		//------------------------

		// lighter
		// res: 0, 1, 1, 0

		fctx2.globalCompositeOperation = "source-over";
		fctx2.drawImage(this, 0, 0, canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "darken";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "color-dodge";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "difference"; 
		fctx2.drawImage(this.keyFrame, 0, 0, canvas.width, frameCanvas2.height);

		fctx.globalCompositeOperation = "source-over";
		fctx.drawImage(frameCanvas2, 0, 0, canvas.width, canvas.height);

		// ------
		fctx2.globalCompositeOperation = "source-over";
		fctx2.drawImage(this, 0, 0, canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "lighten";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "color-dodge";
		fctx2.fillStyle = 'rgb(128,128,128)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx2.globalCompositeOperation = "difference";
		fctx2.fillStyle = 'rgb(255,255,255)';
		fctx2.fillRect(0,0,canvas.width, frameCanvas2.height);

		fctx.globalCompositeOperation = "lighter";
		fctx.drawImage(frameCanvas2, 0, 0, canvas.width, canvas.height);

		fctx.globalCompositeOperation = "source-atop";
		fctx.drawImage(ipCanvas[this.ip1], 0, 0, canvas.width, canvas.height);

		ctx.globalCompositeOperation = "source-atop";
		ctx.drawImage(ipCanvas[this.ip2], 0, 0, canvas.width, canvas.height);

		ctx.globalCompositeOperation = "lighter"; 
		ctx.drawImage(frameCanvas, 0, 0, canvas.width, canvas.height);
	}
};

function createCanvasData(image) {
	var h = image.height * 2;
	if (!canvasImageData || canvasImageData.width != image.width || canvasImageData.height != h) {
		canvas.width = image.width;
		canvas.height = h;
		ctx = canvas.getContext('2d');
		frameCanvas.width = image.width;
		frameCanvas.height = image.height;
		fctx = frameCanvas.getContext('2d');

		canvasImageData = ctx.createImageData(image.width, h);
		var cData = canvasImageData.data;
		for (var p = 0; p < cData.length; p++) {
			cData[p] = 255;
		}

		lastKeyData = new Array();
		console.log("Clear canvas");
	}
}

function framePixelEditing() {
	createCanvasData(this);
	if (this.type == 1 || this.type == 2) {
		var kData = getImageData(this).data;
		this.imageData = kData;
		transferData(kData, this.ip1);
	} else if ((this.type == 3 || this.type == 4) && this.keyFrame && this.keyFrame.imageData) {
		var kData = this.keyFrame.imageData;
		var iData = getImageData(this).data;
		transferDataI(kData, iData, this.ip1);
	}
};

function getImageData(image) {
	fctx.drawImage(image, 0, 0, 
		image.width, image.height);
	return fctx.getImageData(0,0,image.width, 
		image.height);
}

var canvasImageData;
function transferDataI(kData, iData, ip) {
	var cData = canvasImageData.data;
	var w = canvas.width * 4;
	var w2 = w - 4;
	var p2 = ip == 0 ? 0 : w;
	for (var p = 0; p < kData.length; p+=4, p2+=4) {
		var pg = p+1;
		var pb = p+2;
		var pg2 = p2+1;
		var pb2 = p2+2;
		cData[p2] = kData[p] + (iData[p] - 127) * 2;
		cData[pg2] = kData[pg] + (iData[pg] - 127) * 2;
		cData[pb2] = kData[pb] + (iData[pb] - 127) * 2;
		if (p % w == w2) p2 += w;
	}
	ctx.putImageData(canvasImageData, 0, 0);
}
function transferData(kData, ip) {
	var cData = canvasImageData.data;
	var w = canvas.width * 4;
	var w2 = w - 4;
	var p2 = ip == 0 ? 0 : w;
	for (var p = 0; p < kData.length; p+=4, p2+=4) {
		var pg = p+1;
		var pb = p+2;
		var pg2 = p2+1;
		var pb2 = p2+2;
		cData[p2] = kData[p];
		cData[pg2] = kData[pg];
		cData[pb2] = kData[pb];
		if (p % w == w2) p2 += w;
	}
	ctx.putImageData(canvasImageData, 0, 0);
}

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

	$("#imageContainer").show();

	var source = "ws://" + window.location.host + "/websocket?t=" + new Date().getTime();
	websocket = new WebSocket(source);
	websocket.binaryType = 'arraybuffer';
	websocket.onopen = function () {
		console.log("MJPEG connected");
		lastKeyFrame = null;
		lastKeyData = new Array();
	};
	websocket.onclose = function () {
		console.log("MJPEG disconnected");
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
				lastKeyFrame = image;
			} else {
				image.keyFrame = lastKeyFrame;
			}
			image.onload = frameCompositing; // Alternative method: framePixelEditing
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

			websocket.send(">" + framestamp);
		}
		delete bytes;

	};
	websocket.onerror = function (msg) {
		console.log('error: ' + msg.data);
		websocket.close();
	};
}

var pollTimeout = null;
function pollDelay() {
	if (pollTimeout) clearTimeout(pollTimeout);
	pollTimeout = setTimeout(poll, 1000);
}


var isActive = false;
function poll() {
	$.ajax({
	    url: "/control",
	    dataType: "json",
	    data: {
		"WIDTH": window.innerWidth,
		"HEIGHT": window.innerHeight
	    },
	    success: function( data ) {
		isActive = true;

		connectWebSocket();

		//$("#message").html("<a href=\"#\" onclick=\"logOut(); return false;\">Log out</a>");

		pollDelay();
	    },
	    error: function(xhr, status, error) {
		parseError(xhr);
	    }
	});
}

$(document).ready(function(){
	canvas = document.getElementById("canvas");
	frameCanvas = document.createElement('canvas');
	frameCanvas2 = document.createElement('canvas');

	inputSetup();

	poll();

        document.getElementById("keyboardHack").focus();
});
