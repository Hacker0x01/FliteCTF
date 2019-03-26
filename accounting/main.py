"""
CONGRATULATIONS!

If you're reading this, you've made it to the end of the road for this CTF.

Go to https://hackerone.com/50m-ctf and submit your write up, including as much detail as you can.
Make sure to include 'c8889970d9fb722066f31e804e351993' in the report, so we know for sure you made it through!

Congratulations again, and I'm sorry for the red herrings. :)
"""

from flask import Flask, abort, redirect, render_template, request, Response
from jinja2 import Template
from weasyprint import HTML
import base64, json, os, random, re

app = Flask(__name__)

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

@app.route('/')
def index():
	return render('index', page='home')

@app.route('/auth', methods=['GET', 'POST'])
def auth(page=None):
	error = None
	if request.method == 'POST':
		password = request.form['password']
		error = makeSqlError(password)
		if error is False or ("'" in password and 'sqlmap' in request.headers.get('User-Agent') and random.randrange(3) != 0):
			raise Exception('SQL Error')
	return render('login', page=page or 'login', error=error)

def makeSqlError(password):
	password = "'" + password + "'"
	quotes = 0
	escape = False
	nonquoted = ''
	for c in password:
		if escape:
			escape = False
		elif c == '\\':
			escape = True
		elif c == '\'':
			quotes += 1
		elif (quotes & 1) == 0:
			nonquoted += c

	if (quotes & 1) != 0:
		return False
	elif ' OR ' in nonquoted:
		return 'Invalid password'
	elif 'UNION' in nonquoted:
		return 'Invalid username'
	return 'Invalid username or password'

@app.route('/invoices')
def invoices():
	return auth('invoices')

@app.route('/reports')
def reports():
	return auth('reports')

def stripClosingTags(data):
	return re.sub(r'<\s*/[^<]+?>', '', data)

@app.route('/invoices/new')
def newInvoice():
	return render('newInvoice', page='new_invoice')

@app.route('/invoices/newInvoice.js')
def newInvoiceScript():
	return open('newInvoice.js', 'r').read()

def getData():
	jobj = json.loads(request.args['d'])
	data = dict(
		invoiceNumber=jobj['invoiceNumber'], 
		companyName=jobj['companyName'], 
		email=jobj['email'], 
		date=jobj['date'], 
		items=[
			dict(
				quantity=float(item[0]), 
				name=item[1], 
				desc=item[2], 
				price=float(item[3]), 
				total=float(item[0]) * float(item[3])
			)
			for item in jobj['items']], 
	)
	data['total'] = sum(item['total'] for item in data['items'])

	data['style'] = '\n'.join('\t\t%s {\n%s\n\t\t}' % (sanitize(selector), 
			'\n'.join('\t\t\t%s: %s;' % (stripClosingTags(k).replace('script', ''), sanitize(v)) for k, v in traits.items()))
		for selector, traits in jobj['styles'].items())

	return data

@app.route('/invoices/preview')
def preview():
	return render('invoice', page='preview_invoice', **getData())

@app.route('/invoices/pdfize')
def pdfize():
	html = render('invoice', page='pdfize_invoice', **getData())
	pdf = HTML(string=html).write_pdf()
	return Response(pdf, mimetype='application/pdf')

if __name__ == "__main__":
	app.run(host='0.0.0.0', port=80)
