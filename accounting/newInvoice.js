var itemCount = 0;

function addItem() {
	var table = $('#item-table > tbody');
	var tr = $('<tr>');
	function add(pfx, def) {
		tr.append($('<td><input type="text" id="' + pfx + '-' + itemCount + '" value="' + def + '"></td>'));
	}
	add('quantity', '1');
	add('name', '');
	add('desc', '');
	add('unit-price', '10');
	tr.append($('<td id="total-' + itemCount + '"></td>'));
	table.append(tr);
	$('#quantity-' + itemCount).change(updateTotals);
	$('#unit-price-' + itemCount).change(updateTotals);
	itemCount++;
	updateTotals();
}

function updateTotals() {
	for(var i = 0; i < itemCount; ++i) {
		var amt = parseFloat($('#quantity-' + i).val()) * parseFloat($('#unit-price-' + i).val());
		$('#total-' + i).text('$' + amt);
	}
}

function encodeInvoice() {
	var items = [];
	for(var i = 0; i < itemCount; ++i) {
		items[i] = [
			$('#quantity-' + i).val(), 
			$('#name-' + i).val(), 
			$('#desc-' + i).val(), 
			$('#unit-price-' + i).val()
		];
	}

	var jobj = {
		companyName: $('#recipient-company-name').val(), 
		email: $('#recipient-email').val(), 
		invoiceNumber: $('#invoice-number').val(), 
		date: $('#invoice-date').val(), 
		items: items, 
		styles: {body: {'background-color' : 'white'}}
	};

	var p = JSON.stringify(jobj);
	return p;
}

function preview() {
	// kTHJ9QYJY5597pY7uLEQCv9xEbpk41BDeRy82yzx24VggvcViiCuXqXvF11TPusmb5TucH
	//  5MmCWZhKJD29KVGZLrB6hBbLkRPn8o6H5bF73SgHyR3BdmoVJ9hWvtHfD3NNz6rBsLqV9
	var p = encodeInvoice();
	var url = 'http://' + window.location.hostname + '/invoices/preview?d=' + encodeURIComponent(p);
	url = url.replace(/[\u00A0-\u9999<>\&]/gim, function(i) { return '&#'+i.charCodeAt(0)+';'; });
	$('#iframe-box').empty();
	$('#iframe-box').append($('<iframe width="100%" height="500px" src="' + url + '"></iframe>'));
}

function savePDF() {
	var p = encodeInvoice();
	var url = 'http://' + window.location.hostname + '/invoices/pdfize?d=' + encodeURIComponent(p);
	url = url.replace(/[\u00A0-\u9999<>\&]/gim, function(i) { return '&#'+i.charCodeAt(0)+';'; });
	var a = $('<a download href="' + url + '"><span><i>If your download does not start, click here</i></span></a>');
	$('#iframe-box').append(a);
	a.find('span').trigger('click');
}

$(document).ready(function() {
	addItem();
	$('#add-item').click(addItem);
	$('#preview').click(preview);
	$('#save-pdf').click(savePDF);
});