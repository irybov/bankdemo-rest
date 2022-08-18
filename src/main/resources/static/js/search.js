$(document).ready(function(){
    $('#search_form').submit(function (ev) {
        ev.preventDefault();
		$('#client_body').empty();
		$('#bill_body').empty();
 		$.getJSON('http://localhost:8080/bankdemo/accounts/search/'+$('#phone').val(),
 			function(data){
	 		var account = '<tr>'
				+ '<td class=align-middle align=center>'+data.id+'</td>'
				+ '<td class=align-middle align=center>'+data.name+'</td>'
				+ '<td class=align-middle align=center>'+data.surname+'</td>'
				+ '<td class=align-middle align=center>'+data.phone+'</td>'
				+ '<td></td>'
			    + '<td class=align-middle align=center>'+data.birthday+'</td>'
			    + '<td class=align-middle align=center id="target_bool">'+data.active+'</td>'
				+ '<td><button class="btn btn-danger" id="target_status">Switch<br/>status'
				+ '</button></td></tr>';
		 	$(account).appendTo('#client_body');
		 	
			$('#target_status').click(function(){
				if(!confirm('Are you sure to switch the status?')){
                    return false;
   				}
     			$.ajax({
    				type: 'GET',
    				url: 'http://localhost:8080/bankdemo/accounts/status/'+data.id,
     	        	success: function(bool) {
     	        		$('#target_bool').html(bool);
     	        	}
    			});
			});
			
 			$.each(data.bills, function(key, value){
 				var rowID = value.id;
	 			var info = '<tr id="row'+rowID+'">'
					+ '<td class=align-middle align=center>'+value.id+'</td>'
					+ '<td class=align-middle align=center>'+value.balance+'</td>'
					+ '<td class=align-middle align=center>'+value.currency+'</td>'
					+ '<td class=align-middle align=center id="bill_bool'+rowID+'">'+value.active+'</td>'
					+ '<td class=align-middle align=center>'
 					+ '<button class="btn btn-danger" id="bill_status'+rowID+'">Switch<br/>status'
 					+ '</button>'
 					+ '<button class="btn btn-primary" id="show_events'+rowID+'">Show<br/>events'
 					+ '</button>'
 					+ '<button class="btn btn-success" id="export_csv'+rowID+'">Export<br/>to CSV'
 					+ '</button></td></tr>';
				$(info).appendTo('#bill_body');
				
				var statusBTN = '#bill_status'+rowID;
	 			$(statusBTN).click(function(){
    				if(!confirm('Are you sure to switch the status?')){
                        return false;
       				}
	 				var switcher = '#bill_bool'+rowID;
	 	     			$.ajax({
	 					type: 'GET',
	 					url: 'http://localhost:8080/bankdemo/bills/status/'+rowID,
	 	 	        	success: function(bool) {
	 	 	        		$(switcher).html(bool);
	 	 	        	}
	 				});
	 			});
	 			var eventsBTN = '#show_events'+rowID;
	 	    	$(eventsBTN).click(function(){
	 	 			$('#info_body').empty();
	 		 		$.getJSON('http://localhost:8080/bankdemo/operations/list/'+rowID,
	 		 				function(data){
	 		 			var information = '';
	 		 			$.each(data, function(key, value){
	 						information += '<tr>';
	 						information += '<td align=center>'+value.id+'</td>';
	 						information += '<td align=center>'+value.action+'</td>';
	 						information += '<td></td>';
	 						information += '<td align=center>'+value.amount+'</td>';
	 						information += '<td align=center>'+value.currency+'</td>';
	 						information += '<td align=center>'+value.sender+'</td>';
	 						information += '<td align=center>'+value.recipient+'</td>';
	 						information += '<td></td>';
	 						information += '<td align=center>'+value.timestamp+'</td>';				
	 						information += '</tr>';
	 		 			});
	 			 		$('#info_body').append(information);
	 			    });
	 	 			$('#info_body').hide().fadeIn('slow');
	 		    });
	 	    	var exportBTN = '#export_csv'+rowID;
	 			$(exportBTN).click(function(){
 	     			$.ajax({
	 					type: 'GET',
	 					url: 'http://localhost:8080/bankdemo/operations/print/'+rowID,
	 				});
	 			});		 			
 			});    			
	    })
	    .done(function() { $('#message').empty(); })
	    .fail(function() { $('#message').html('Phone number not found'); });
		$('#client_body').hide().fadeIn('fast');
		$('#bill_body').hide().fadeIn('slow');
    });            
});
$(document).ready(function(){
	$('.account_status').click(function(){
 			$.ajax({
			type: 'GET',
			url: 'http://localhost:8080/bankdemo/accounts/status/'+$(this).val(),
        	success: function(bool) {
        		$('#account_bool').html(bool);
        	}
		});
	});
});
$(document).ready(function(){
	$('.bill_status').click(function(){
		var switcher = '#bill_bool'+$(this).val();
 			$.ajax({
			type: 'GET',
			url: 'http://localhost:8080/bankdemo/bills/status/'+$(this).val(),
        	success: function(bool) {
        		$(switcher).html(bool);
        	}
		});
	});
});
$(document).ready(function(){
	$('.export_csv').click(function(){
		$.get('http://localhost:8080/bankdemo/operations/print/'+$(this).val());
	});
});
$(document).ready(function(){
	$('.show_events').click(function(){
		$('#info_body').empty();
 		$.getJSON('http://localhost:8080/bankdemo/operations/list/'+$(this).val(),
 				function(data){
 			var information = '';
 			$.each(data, function(key, value){
				information += '<tr>';
				information += '<td align=center>'+value.id+'</td>';
				information += '<td align=center>'+value.action+'</td>';
				information += '<td align=center>'+'</td>';
				information += '<td align=center>'+value.amount+'</td>';
				information += '<td align=center>'+value.currency+'</td>';
				information += '<td align=center>'+value.sender+'</td>';
				information += '<td align=center>'+value.recipient+'</td>';
				information += '<td align=center>'+'</td>';
				information += '<td align=center>'+value.timestamp+'</td>';				
				information += '</tr>';
 			});
 			$('#info_body').append(information);
	    });
 		$('#info_body').hide().fadeIn('slow');
    });
});