/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/

var ws;
var id = -1;
var thisNickname;
var periodicCall;
var otherServers = {};
var lastAction = "";
var hadError = false;

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
		//If it's the first connection, so when the user clicks login
		if (firstConnection) {
		 	$("#msgLoginSuccess").css("display", "").delay(5000).fadeOut(400);
    		$("#btnLogin").prop("disabled", true);

    		//Requests access to write
			$("#btnRequestRight").click(function() {
				ws.send('{ "type":"writingRequest" }');
				lastAction = '{ "type":"writingRequest" }';

				//Update controls
			    $("#btnRequestRight").prop('disabled',true);
			    $("#btnReleaseRight").prop('disabled',false);
			});

			//Release writing rights
			$( "#btnReleaseRight").click(function() {
				ws.send('{ "type":"releaseRequest", "sharedText":"'+ CryptoHelper.crypt($("#sharedText").val()) +'" }');
				lastAction = '{ "type":"releaseRequest", "sharedText":"'+ CryptoHelper.crypt($("#sharedText").val()) +'" }';

				//Update controls
			    $("#btnReleaseRight").prop('disabled',true);
			    $("#btnRequestRight").prop('disabled',false);
			    $("#sharedText").prop("readOnly", true);

			    clearInterval(periodicCall); //stop periodically calling server to save text
			});
		}

		ws.send('{ "type":"connectionRequest", "id":"'+CryptoHelper.crypt(id.toString())+'", "nickname":"'+ CryptoHelper.crypt(thisNickname)+'" }');

		//If the workspace reconnects and it's last action was not handled properly, send it again upon reconnection
		if (lastAction != "") {
			var tmp = JSON.parse(lastAction);

			if (tmp.type == "releaseRequest") {
		 		$("#msgLoginOupelaye").css("display", "").delay(5000).fadeOut(400);
			    $("#btnReleaseRight").prop('disabled',false);
			    $("#btnRequestRight").prop('disabled',true);	
			}
			else {
				ws.send(lastAction);				
			}
		}
	};

	ws.onmessage = function(event) { 
		var message = JSON.parse(event.data);

		switch(message.type) {
			//Sets the workspace's id
			case "idRequest":
		    	id = CryptoHelper.decrypt(message.id);
		    break;

		    //Notices the workspace when a new user connects
			case "newUser":
			    nickname = CryptoHelper.decrypt(message.nickname);
			    logInfo(nickname + " has joined the session.");
			    break;

			//Notices the workspace when a user leaves the session
			case "userLeft":
			    nickname = CryptoHelper.decrypt(message.nickname);
			    logInfo(nickname + " has left the session.");
			    break;

			//Triggered when the workspace obtains writing rights
			case "hasRights":
				lastAction = "";
			    updateCurrentWriter(thisNickname);
			    logInfo("You now have writing rights!");

			    periodicCall = setInterval(function(){
			    	ws.send('{ "type":"updateSharedText", "sharedText":"'+ CryptoHelper.crypt($("#sharedText").val()) +'" }');
			    }, 5000);

			    $("#sharedText").prop("readOnly", false);
			    $("#btnReleaseRight").val("Release writing rights");
			    break;


			//Notices the workspace when their position in queue changed
	     	case "positionInQueue":
	     		lastAction = "";
	        	position = CryptoHelper.decrypt(message.position);
	        	logInfo("You are now position "+ position + " in queue!");

			    $("#btnReleaseRight").val("Leave queue");
	          	break;

	        //Notices the workspaces when there's a new writer
			case "newWriter":
				lastAction = "";
			    nickname = CryptoHelper.decrypt(message.nickname);

			    if (nickname == '') {
					logInfo("The queue is now empty.");
			    }
			    else if (($("#lblCurrentWriter").text().replace(/\s/g,'').split(":")[1] != nickname && nickname != thisNickname) ){
		    		logInfo(nickname + " now has writing rights!");			    		
			    }

			   	updateCurrentWriter(nickname);

			    break;

			//Notices the workspace that they successfully left the queue
		    case "leftQueue":
		    	lastAction = "";
		    	logInfo("You have left the queue.")
		    	break;

		    //Updates the shared text
		    case "updateSharedText":
		    	$("#sharedText").val(CryptoHelper.decrypt(message.newText));
		    	break;

		    //When a new node is available
		    case "newServer":
		    	var serverName = CryptoHelper.decrypt(message.serverName);
	    		var ip = CryptoHelper.decrypt(message.ip);
	    		var port = CryptoHelper.decrypt(message.port);
	    		addNewServer(serverName, ip, port);
		    	console.log(otherServers);
		    	break;

		    //When a available node is removed
		    case "serverRemove":
		    	delete otherServers[CryptoHelper.decrypt(message.serverName)];
		    	console.log(otherServers);
		    	break;

		    //When the workspace is told to connect to a new server
	    	case "redirect":
	    		var serverName = CryptoHelper.decrypt(message.serverName);
	    		var ip = CryptoHelper.decrypt(message.ip);
	    		var port = CryptoHelper.decrypt(message.port);
	    		addNewServer(serverName, ip, port);
	    		break;
		}
	};

	//When the workspace fails to connect to a server
	ws.onerror = function(event){
		if (firstConnection) {
		  $("#msgLoginFailure").css("display", "").delay(5000).fadeOut(400);
		  hadError = true;
		}
	};

	//Triggered when the connection is closed
	ws.onclose = function (event) {
		console.log("serveur fermé");
		if (Object.keys(otherServers).length != 0) {
			var value = otherServers[Object.keys(otherServers)[0]];
		    delete otherServers[Object.keys(otherServers)[0]];	  
			connectToServer(value.ip, value.port, false);				
		}
		else if(!hadError) {
		  	$("#msgLoginCatastrophe").css("display", "").delay(5000).fadeOut(400);
		  	clearInterval(periodicCall);
			console.log("catastrophe");
		}

		hadError = false;
	};
}

//Adds a new server to his list of known nodes
function addNewServer(serverName, ip, port) {
	otherServers[serverName] = { "ip":ip, "port":port };
}

//Updates the label showing the current writer
function updateCurrentWriter(str) {
	$("#lblCurrentWriter").text("Current writer : " + str);
}

//Logs the info passed by parameter
function logInfo(info) {
	document.getElementById("txtLog").value += info + "\n";
}
