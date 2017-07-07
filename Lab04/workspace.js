const PrivateKey = 'LOG735';

var ws;
var id;
var nickname;

//Login button click method. Sets nickname and calls connectToServer with input values
$( "#login" ).click(function() {
  if($.trim($('#txtNickname').val()) == '' || $.trim($('#txtIpAddress').val()) == '' || $.trim($('#txtPort').val()) == '') {
      alert('You must enter a nickname, an IP address and a port');
  }
  else {
    nickname = $.trim($('#txtNickname').val());
    var ipAddress = $.trim($('#txtIpAddress').val());
    var port = $.trim($('#txtPort').val());

    var state = connectToServer(ipAddress, port, true);
  }  
});


//Tries to establish a connection with the ipAddress and port passed in parameters
function connectToServer(ipAddress, port, firstConnection) {
  ws = new WebSocket("ws://"+ipAddress+":"+port);

  ws.onopen = function (event) {
  	console.log(crypt('{ "type" : "idRequest" }' ));
    ws.send(crypt('{ "type" : "idRequest" }' )); 

    if (firstConnection) {
      $("#msgLoginSuccess").css("display", "").delay(5000).fadeOut(400);
    }
  };

  ws.onmessage = function(event) { 
    var message = decrypt(event);
    console.log(message);
    switch(message.type) {
      case "idRequest":
        console.log(message.id);
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

}

//Returns a crypted object
function crypt(object) {
	return CryptoJS.AES.encrypt(object, PrivateKey).toString();
}

//Returns a decrypted object
function decrypt(object) {
	var bytes  = CryptoJS.AES.decrypt(object, PrivateKey);
	return JSON.parse(bytes.toString(CryptoJS.enc.Utf8));
}