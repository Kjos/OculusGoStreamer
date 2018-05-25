function PCMPlayer(option) {
	this.init(option);
}

PCMPlayer.prototype.init = function(option) {
	var defaults = {
		encoding: '16bitInt',
		channels: 1,
		sampleRate: 8000,
		flushingTime: 1000
	};
	this.option = Object.assign({}, defaults, option);
	this.samples = new Float32Array();
	this.flush = this.flush.bind(this);
	//this.interval = setInterval(this.flush, this.option.flushingTime);
	this.maxValue = this.getMaxValue();
	this.typedArray = this.getTypedArray();
	this.audioBufferPool = new Array();
	this.createContext();

	this.maxBufferSize = 1000;
	this.refresh();
	this.flush();
};

PCMPlayer.prototype.getMaxValue = function () {
	var encodings = {
		'8bitInt': 128,
		'16bitInt': 32768,
		'32bitInt': 2147483648,
		'32bitFloat': 1
	}

	return encodings[this.option.encoding] ? encodings[this.option.encoding] : encodings['16bitInt'];
};

PCMPlayer.prototype.getTypedArray = function () {
	var typedArrays = {
		'8bitInt': Int8Array,
		'16bitInt': Int16Array,
		'32bitInt': Int32Array,
		'32bitFloat': Float32Array
	}

	return typedArrays[this.option.encoding] ? typedArrays[this.option.encoding] : typedArrays['16bitInt'];
};

PCMPlayer.prototype.createContext = function() {
	this.audioCtx = new (window.AudioContext || window.webkitAudioContext)();
	this.gainNode = this.audioCtx.createGain();
	this.gainNode.gain.value = 1;
	this.gainNode.connect(this.audioCtx.destination);
};

PCMPlayer.prototype.isTypedArray = function(data) {
	return (data.byteLength && data.buffer && data.buffer.constructor == ArrayBuffer);
};

PCMPlayer.prototype.feed = function(data) {
	if (!this.isTypedArray(data)) return;
	data = this.getFormatedValue(data);
	this.feedFormatted(data);
};

PCMPlayer.prototype.feedFormatted = function(data) {
	var k = 0;
	for (i = this.audioCount; i < this.maxBufferSize && k < data.length; i++, k++) {
		this.audioData[i] = data[k];
		this.audioCount++;
		if (this.audioCount >= this.maxBufferSize) {
			this.flush();
			this.feedFormatted(data.slice(k+1));
			return;
		}
	}
};

var buffer = new Array();
var playing = null;

PCMPlayer.prototype.getFormatedValue = function(data) {
	var data = new this.typedArray(data.buffer),
		float32 = new Float32Array(data.length),
		i;

	for (i = 0; i < data.length; i++) {
		float32[i] = data[i] / this.maxValue;
	}
	return float32;
};

PCMPlayer.prototype.volume = function(volume) {
	this.gainNode.gain.value = volume;
};

PCMPlayer.prototype.destroy = function() {
	if (this.interval) {
		clearInterval(this.interval);
	}
	this.samples = null;
	this.audioCtx.close();
	this.audioCtx = null;
};

PCMPlayer.prototype.refresh = function() {
	this.samples = new Float32Array();
	var length = this.maxBufferSize / this.option.channels;
	this.bufferSource = this.audioCtx.createBufferSource();
	this.audioBuffer = this.audioCtx.createBuffer(this.option.channels, length, this.option.sampleRate);
	this.audioData = this.audioBuffer.getChannelData(0);
	this.audioCount = 0;
};

PCMPlayer.prototype.flush = function() {
	if (this.audioCount != this.maxBufferSize) {
		//setTimeout(this.flush, 100);
		return;
	}

	this.bufferSource.buffer = this.audioBuffer;
	this.bufferSource.connect(this.gainNode);
	this.bufferSource.start(0);
	this.refresh();
};
