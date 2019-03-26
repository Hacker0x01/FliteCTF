from flask import Flask, abort, request, Response, session
from jinja2 import Template
import base64, hashlib, json, os, random, re, time, traceback
from Crypto.Cipher import AES
from Crypto import Random
import MySQLdb

app = Flask(__name__)

def getDb():
	return MySQLdb.connect(host="localhost", user="root", password="", db="flitebackend")

@app.after_request
def add_header(r):
	r.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
	r.headers["Pragma"] = "no-cache"
	r.headers["Expires"] = "0"
	r.headers['Cache-Control'] = 'public, max-age=0'
	return r

key = '8O.j\x1a\x05\xe5";\x80\xe9`\xa0\xa6Pt' # 384f2e6a1a05e5223b80e960a0a65074

def pad(data):
	dlen = 16 - (len(data) % 16)
	data += chr(dlen) * dlen
	return data

def unpad(data):
	padding = data[-1]
	pval = ord(padding)
	return data[:-pval]

def build(obj):
	data = json.dumps(obj)
	rand = Random.new()
	iv = rand.read(16)
	cipher = AES.new(key, AES.MODE_CBC, iv)
	return base64.encodestring(iv + cipher.encrypt(pad(data))).replace('\n', '')

def error(err='Unknown'):
	return build(dict(success=False, error=err))

def success():
	return build(dict(success=True))

@app.route('/', methods=['POST'])
def index():
	try:
		payload = base64.decodestring(request.form['d'])
		iv = payload[:16]
		cipher = AES.new(key, AES.MODE_CBC, iv)
		payload = json.loads(unpad(cipher.decrypt(payload[16:])))

		cur = getDb().cursor()
		if cur.execute('SELECT password FROM users WHERE username=\'%s\'' % payload['username'].replace('%', '%%')) == 0:
			return error('Invalid username or password')
		rph, = cur.fetchone()
		pph = hashlib.md5(payload['password']).hexdigest()
		if pph != rph:
			return error('Invalid username or password')

		print payload

		cmd = payload['cmd']
		if cmd == 'setTemp':
			return success() if 'temp' in payload else error('Missing temperature')
		elif cmd == 'getTemp':
			return build(dict(success=True, temperature=73))
		elif cmd == 'diag':
			return error('Missing diagnostic parameters')
		else:
			return error('Unknown command')
	except:
		traceback.print_exc()
		return error()

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=80)
