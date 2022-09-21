$(document).ready(function(){
    $('#validate').click(function (ev) {
        ev.preventDefault();
		$.ajax({
			type: 'GET',
        	url: 'http://localhost:8080/bankdemo/bills/validate/'+$('#recipient').val(),
        	dataType: "text",
    		success: function(data){
    			$('#info').text(data);
    		}
    	});
    });
});