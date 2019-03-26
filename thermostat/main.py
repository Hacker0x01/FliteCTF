from flask import Flask, abort, redirect, render_template, request, Response, session
from jinja2 import Template
import base64, json, os, random, re, subprocess, time

app = Flask(__name__)
app.secret_key = '99807ef08993b1cf019f6cd30fa3acbfbda992ee2aeffc5339f0f130e25604c4'

def render(tpl, **kwargs):
	return render_template('%s.html' % tpl, **kwargs)

@app.after_request
def add_header(r):
	r.headers["Cache-Control"] = "no-cache, no-store, must-revalidate"
	r.headers["Pragma"] = "no-cache"
	r.headers["Expires"] = "0"
	r.headers['Cache-Control'] = 'public, max-age=0'
	return r

def sanitize(data):
	return data.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace('"', '&quot;')

validLogin = 'f9865a4952a4f5d74b43f3558fed6a0225c6877fba60a250bcbde753f5db13d8'

@app.route('/', methods=['GET', 'POST'])
def index():
	if 'loggedIn' in session and session['loggedIn']:
		return redirect('/main')
	error = False
	if request.method == 'POST':
		h = request.form['hash']
		if len(h) != 64:
			error = True
		else:
			for i in range(0, 64, 2):
				time.sleep(0.5)
				if validLogin[i:i+2] != h[i:i+2]:
					error = True
					break
		if not error:
			session['loggedIn'] = True
			return redirect('/main')

	return render('index', page='login', error='Invalid username or password' if error else None)

@app.route('/login.js')
def login_js():
	return open('login.js', 'r').read()

@app.route('/main')
def main_():
	if 'loggedIn' not in session or not session['loggedIn']:
		return redirect('/')
	return render('main', page='main')

@app.route('/control')
def control():
	if 'loggedIn' not in session or not session['loggedIn']:
		return redirect('/')
	with open('temptarget', 'r') as fp:
		temp = int(fp.read())
	return render('control', temp=temp, cooling=temp <= 70)

@app.route('/setTemp', methods=['POST'])
def setTemp():
	if 'loggedIn' not in session or not session['loggedIn']:
		return redirect('/')
	temp = int(request.form['temp'])
	with open('temptarget', 'w') as fp:
		fp.write(str(temp))
	return redirect('/control')

@app.route('/update')
def update():
	if 'loggedIn' not in session or not session['loggedIn']:
		return redirect('/')
	
	host = request.args.get('update_host', 'update.flitethermostat')
	port = int(request.args.get('port', '5000'))

	resp = subprocess.check_output('bash update.sh http://%s:%i/' % (host, port), shell=True, stderr=subprocess.STDOUT)
	time.sleep(3)
	return render('update', page='update', output=resp.decode('utf-8'))

@app.route('/diagnostics')
def diagnostics():
	if 'loggedIn' not in session or not session['loggedIn']:
		return redirect('/')
	return 'Forbidden', 403

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=80)
