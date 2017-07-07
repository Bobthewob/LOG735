const PrivateKey = 'LOG735';

var ws;
var id;
var nickname;

//Login button click method. Sets nickname and calls connectToServer with input values
$( "#btnLogin" ).click(function() {
  if($.trim($('#txtNickname').val()) == '' || $.trim($('#txtIpAddress').val()) == '' || $.trim($('#txtPort').val()) == '') {
      alert('You must enter a nickname, an IP address and a port');
  }
  else {
    nickname = $.trim($('#txtNickname').val());
    var ipAddress = $.trim($('#txtIpAddress').val());
    var port = $.trim($('#txtPort').val());
    connectToServer(ipAddress, port, true);
    $('#btnLogin').prop('disabled', true);
  }  
});

//Tries to establish a connection with the ipAddress and port passed in parameters
function connectToServer(ipAddress, port, firstConnection) {
	ws = new WebSocket("ws://"+ipAddress+":"+port);

	ws.onopen = function (event) {
		if (firstConnection) {
		  $("#msgLoginSuccess").css("display", "").delay(5000).fadeOut(400);
		}
	};

	ws.onmessage = function(event) { 
		var message = JSON.parse(event.data);

		switch(message.type) {
		  case "idRequest":
		    id = message.id;
		    ws.send('{ "type":"nicknameRequest", "nickname":"' + crypt(nickname)+ '" }');
		    break;

			case "newUser":
			    nickname = decrypt(message.nickname);
			    logInfo(nickname + " has joined the session");
			    break;

			case "userLeft":
			    nickname = decrypt(message.nickname);
			    logInfo(nickname + " has left the session");
			    break;

			case "hasRights":
			    document.getElementById("sharedText").readOnly = false;
			    $("#lblCurrentWriter").text("Current writer : " + nickname);
			    logInfo("You know have writing rights!");
			    break;

			case "newWriter":
			    nickname = decrypt(message.nickname);
			    logInfo(nickname + " now has writing rights!");
			    $("#lblCurrentWriter").text("Current writer : " + nickname);
			    break;
		}
	};

	ws.onerror = function(event){
		if (firstConnection) {
		  $("#msgLoginFailure").css("display", "").delay(5000).fadeOut(400);
		}
	};

	ws.onclose = function (event) {
		console.log("serveur fermé");
	};

	//Requests access to write
	$( "#btnRequestRight").click(function() {
		ws.send('{ "type":"writingRequest" }');
	});

}

//Logs the info passed by parameter
function logInfo(info) {
	document.getElementById("txtLog").value += info + "\n";
}

//Returns a crypted object
function crypt(object) {
	return CryptoJS.AES.encrypt(object, PrivateKey);
}

//Returns a decrypted object
function decrypt(object) {
	var bytes  = CryptoJS.AES.decrypt(object.toString(), PrivateKey);
	return bytes.toString(CryptoJS.enc.Utf8);
}