const PrivateKey = 'LOG735';

var ws;
var id;
var thisNickname;

//Login button click method. Sets nickname and calls connectToServer with input values
$( "#btnLogin" ).click(function() {
  if($.trim($("#txtNickname").val()) == '' || $.trim($("#txtIpAddress").val()) == '' || $.trim($("#txtPort").val()) == '') {
      alert('You must enter a nickname, an IP address and a port');
  }
  else {
    thisNickname = $.trim($("#txtNickname").val());
    var ipAddress = $.trim($("#txtIpAddress").val());
    var port = $.trim($("#txtPort").val());

    connectToServer(ipAddress, port, true);
  }  
});

//Tries to establish a connection with the ipAddress and port passed in parameters
function connectToServer(ipAddress, port, firstConnection) {
	ws = new WebSocket("ws://"+ipAddress+":"+port);

	ws.onopen = function (event) {
		if (firstConnection) {
		 	$("#msgLoginSuccess").css("display", "").delay(5000).fadeOut(400);
    		$("#btnLogin").prop("disabled", true);
		}
	};

	ws.onmessage = function(event) { 
		var message = JSON.parse(event.data);

		switch(message.type) {
			case "idRequest":
		    	id = message.id;
		    	ws.send('{ "type":"nicknameRequest", "nickname":"' + crypt(thisNickname)+ '" }');
		    break;

			case "newUser":
			    nickname = decrypt(message.nickname);
			    logInfo(nickname + " has joined the session.");
			    break;

			case "userLeft":
			    nickname = decrypt(message.nickname);
			    logInfo(nickname + " has left the session.");
			    break;

			case "hasRights":
			    updateCurrentWriter(thisNickname);
			    logInfo("You now have writing rights!");

			    $("#sharedText").prop("readOnly", false);
			    $("#btnReleaseRight").val("Release writing rights");
			    break;

	     	case "positionInQueue":
	        	position = decrypt(message.position);
	        	logInfo("You are now position "+ position + " in queue!");

			    $("#btnReleaseRight").val("Leave queue");
	          	break;

			case "newWriter":
			    nickname = decrypt(message.nickname);

			    if (nickname == '') {
					logInfo("The queue is now empty.");
			    }
			    else {
			    	logInfo(nickname + " now has writing rights!");
			    }

			   	updateCurrentWriter(nickname);
			    break;

		    case "leftQueue":
		    	logInfo("You have left the queue.")
		    	break;

		    case "updateSharedText":
		    	$("#sharedText").val(decrypt(message.newText));
		    	break;
		}
	};

	//When the workspace fails to connect to a server
	ws.onerror = function(event){
		if (firstConnection) {
		  $("#msgLoginFailure").css("display", "").delay(5000).fadeOut(400);
		}
	};

	ws.onclose = function (event) {
		console.log("serveur fermé");
	};

	//Requests access to write
	$("#btnRequestRight").click(function() {
		ws.send('{ "type":"writingRequest" }');

		//Update controls
	    $("#btnRequestRight").prop('disabled',true);
	    $("#btnReleaseRight").prop('disabled',false);
	});

	//Release writing rights
	$( "#btnReleaseRight").click(function() {
		ws.send('{ "type":"releaseRequest", "sharedText":"'+ crypt($("#sharedText").val()) +'" }');

		//Update controls
	    $("#btnReleaseRight").prop('disabled',true);
	    $("#btnRequestRight").prop('disabled',false);
	    $("#sharedText").prop("readOnly", true);
	});
}

function updateCurrentWriter(str) {
	$("#lblCurrentWriter").text("Current writer : " + str);
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