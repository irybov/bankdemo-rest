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