function stream(key) {
	this.b = [];
	for(var i = 0; i < 256; ++i)
		this.b[i] = i;
	var j = 0;
	for(var i = 0; i < 256; ++i) {
		j = (j + this.b[i] + key.charCodeAt(i % key.length)) & 0xFF;
		var t = this.b[i];
		this.b[i] = this.b[j];
		this.b[j] = t;
	}
	this.a = 0;
	this.c = 0;
}

stream.prototype.next = function() {
	this.a = (this.a + 1) & 0xFF;
	this.c = (this.c + this.b[this.a]) & 0xFF;
	var t = this.b[this.a];
	this.b[this.a] = this.b[this.c];
	this.b[this.c] = t;
	return this.b[(this.b[this.a] + this.b[this.c]) & 0xFF];
};

function e(x) {
	var s = new stream(x);
	var ox = '';
	for(var i = 0; i < x.length; ++i)
		ox += String.fromCharCode(s.next() ^ x.charCodeAt(i));
	return ox;
}

function hash(x) {
	x += '\x01\x00';
	while((x.length & 0xFF) != 0)
		x += String.fromCharCode((x.length & 0xFF) ^ x.charCodeAt[x.length & 0xFF]);
	
	var h = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
	for(var i = 0; i < x.length; i += 32) {
		var c = e(x.substring(i, i + 32));
		for(var j = 0; j < 32; ++j)
			h[j] ^= c.charCodeAt(j);
	}

	var hs = '';
	for(var i = 0; i < 32; ++i)
		hs += String.fromCharCode(h[i]);
	return hs;
}

function fhash(x) {
	for(var i = 0; i < 256; ++i)
		x = hash(x);
	var h = '';
	for(var i = 0; i < 32; ++i) {
		var t = x.charCodeAt(i).toString(16);
		if(t.length == 1)
			t = '0' + t;
		h += t;
	}
	return h;
}

function login() {
	document.getElementById('hash').value = fhash(
		document.getElementById('username').value +
		'\x05\0\x06' + 
		document.getElementById('password').value
	);
	document.getElementById('form').submit();
}
