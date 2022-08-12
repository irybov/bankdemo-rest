$(document).ready(function(){
    // как-то сильно много обработчиков у вас в скрипте
    // уберем лишнее
    // $('.add_bill').click(function(){
        // отсюда валюту убрал
        $('#currency_form').submit(function (ev) {
            ev.preventDefault();
            // ПЕРЕНЕС СЮДА ОПРЕДЕЛЕНИЕ ВАЛЮТЫ
            var phone = $('#account_phone').val();
            var type = $('#currency').val();
            // еще Вы у себя забыли знак доллада перед ('#currency')
            // получаем название заголовка и значение токена
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            // определяем массив с заголовками запроса и прокидываем в него CSRF
            var requestHeaders = {};
            requestHeaders[header] = token;
            $.ajax({
                type: 'POST',
                // убираем данные из урла
                url: 'http://localhost:8080/bankdemo/bills/add', 
                // передаем данные в тело запроса
                data: {
                	"phone": phone,
                    "currency": type
                },
                // пробрасываем в запрос наши заголовки
                headers: requestHeaders,
/*                     success: function(msg) {
                    alert(msg);
                }, // <- ЗАПЯТУЮ ЕЩЕ ЗАБЫЛИ
                error: function (xhr, statusText, err) {
                  alert(xhr.status);
                } */
                success: function(data){
                	var rowID = data.id;
	    	 		var info = '<tr id="row'+rowID+'">'
	   					+ '<td class=align-middle align=center>'+data.id+'</td>'
	   					+ '<td class=align-middle align=center>'+data.balance+'</td>'
	   					+ '<td class=align-middle align=center>'+data.currency+'</td>'
	   					+ '<td class=align-middle align=center>'+data.active+'</td>'
	   					+ '<td class=align-middle align=center><form><div><button class="btn btn-primary">Deposit</button><button class="btn btn-info">Withdraw</button><button class="btn btn-warning">Transfer</button></div></form></td>'
	 					+ '<td><button class="btn btn-danger" id="erase'+rowID+'">Erase</button></td>'
	   					+ '</tr>';
	    		 	$(info).appendTo('#bill_body');
	    		 	var dataRow = '#row'+rowID; 
	    		 	var eraseBTN = '#erase'+rowID;
	    			$(eraseBTN).click(function(){
	    				if(!confirm('Are you sure to delete this bill?')){
	                        return false;
	       				}
	    				$.ajax({
	    				    url: 'http://localhost:8080/bankdemo/bills/delete?id='+rowID,
	    				    type: 'DELETE',
	    				    headers: requestHeaders,
	    				    success: function(){
	    				    	$(dataRow).remove();
	    				        return false;
	    				    }
	    				});
	    			});
                }
            });
        });
    // }); // это закрытие обработчика кнопки. его мы убрали пока что
});