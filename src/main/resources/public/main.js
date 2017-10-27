$(document).ready(function(){

function doAction(actionUrl, successMessage) {
	$('#controls,#loader').toggleClass('hidden');
	$.ajax({
        method: 'POST',
        url: actionUrl
    }).done(function(data) {
        console.log('OK');
        displaySuccessNotification(successMessage);
        $('#controls,#loader').toggleClass('hidden');
    })
    .fail(function(data) {
    	console.error(data);
    	$('#error').text(JSON.stringify(data));
    	$('#error,#loader').toggleClass('hidden');
    });
}

function displaySuccessNotification(message) {
	let successElement = $('#success');
	successElement.text(message);
	successElement.toggleClass('hidden');
	setTimeout(() => {
		successElement.toggleClass('hidden');
	}, 3000);
}

$('#btnResetDemo').on('click', function() { doAction('/reset', 'Demo has been reset.'); });
$('#btnCompleteDemo').on('click', function() { doAction('/complete', 'Demo has been completed.'); });

});