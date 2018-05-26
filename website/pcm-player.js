function PCMPlayer(option) {
	this.init(option);
}

PCMPlayer.prototype.init = function(option) {
	var defaults = {
		encoding: '16bitInt',
		channels: 1,
		sampleRate: 8000
	};
	this.option = Object.assign({}, defaults, option);
	this.samples = new Float32Array();
	this.flush = this.flush.bind(this);
	this.maxValue = this.getMaxValue();
	this.typedArray = this.getTypedArray();
	this.audioBufferPool = new Array();
	this.createContext();

	this.maxBufferSize = 2000;
	if (this.option.channels == 2) {
		this.refresh = this.refreshDual;
		this.feedFormatted = this.feedFormattedDual;
	}
	this.refresh();
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
	this.startTime = this.audioCtx.currentTime;
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
	for (k = 0; k < data.length;) {
		this.audioData[this.audioCount] = data[k++];
		this.audioData[this.audioCount] /= this.maxValue;
		this.audioCount++;
		if (this.audioCount >= this.maxBufferSize) {
			this.flush();
			this.feedFormatted(data.slice(k));
			break;
		}
	}
	delete data;
};

PCMPlayer.prototype.feedFormattedDual = function(data) {
	for (k = 0; k < data.length;) {
		this.audioData[this.audioCount] = data[k++];
		this.audioData[this.audioCount] /= this.maxValue;
		this.audioData2[this.audioCount] = data[k++];
		this.audioData2[this.audioCount] /= this.maxValue;
		this.audioCount++;
		if (this.audioCount >= this.maxBufferSize) {
			this.flush();
			this.feedFormattedDual(data.subarray(k));
			break;
		}
	}
	delete data;
};

PCMPlayer.prototype.getFormattedValue = function(data) {
	var 
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
	var length = this.maxBufferSize * this.option.channels;
	this.bufferSource = this.audioCtx.createBufferSource();
	this.audioBuffer = this.audioCtx.createBuffer(this.option.channels, length, this.option.sampleRate);
	this.audioData = this.audioBuffer.getChannelData(0);
	this.audioCount = 0;
};

PCMPlayer.prototype.refreshDual = function() {
	var length = this.maxBufferSize * this.option.channels;
	this.bufferSource = this.audioCtx.createBufferSource();
	this.audioBuffer = this.audioCtx.createBuffer(this.option.channels, length, this.option.sampleRate);
	this.audioData = this.audioBuffer.getChannelData(0);
	this.audioData2 = this.audioBuffer.getChannelData(1);

	this.audioCount = 0;
};

PCMPlayer.prototype.flush = function() {
	if (this.audioCount != this.maxBufferSize) {
		return;
	}
	if (this.startTime < this.audioCtx.currentTime) {
		this.startTime = this.audioCtx.currentTime;
	}

	this.bufferSource.buffer = this.audioBuffer;
	this.bufferSource.playbackRate = 0.99;
	this.bufferSource.connect(this.gainNode);
	this.bufferSource.start(this.startTime);

   	this.startTime += this.audioBuffer.duration;
	this.refresh();
};
