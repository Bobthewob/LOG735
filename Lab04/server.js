//Server initialisation
const WebSocketServer = require("ws").Server;
const CryptoJS = require("crypto-js");
const PrivateKey = 'LOG735';
const ServerPort = process.argv[2];

var wss = new WebSocketServer( { port: ServerPort } );
console.log("Server started...");

var currentId = 0;
var workspaces = [];
var sharedText = '';
var currentWriter = null;
var writingFifo = [];

//Called when a workspace connects to the server
wss.on('connection', function (ws) {
  	console.log("Browser connected online...");
  	//A new workspace connects and requests and ID from the server
  	ws.id = currentId;
  	workspaces[ws.id] = ws;
  	ws.send('{ "type":"idRequest", "id":"'+ crypt(currentId.toString()) +' "}'); //Informs the workspace of his ID
  	currentId++;

  	//If a workspace connects and there's a current writer
  	if (currentWriter != null) {
		var data = '{ "type":"newWriter", "nickname":"'+ crypt(workspaces[currentWriter].nickname) +' "}';
		ws.send(data);
  	}

    //When a workspace sends a message to the server
  	ws.on("message", function (str) {
	    var object = JSON.parse(str);

	    switch(object.type) {
	    	//Sets the nickname for the workspace
		    case 'nicknameRequest':
				workspaces[ws.id].nickname = decrypt(object.nickname); //Saves nickname
		        var data = '{ "type":"newUser", "nickname":"'+ object.nickname +' "}';
		        broadcastToEveryoneElse(ws, data);
		        break;

		    case 'writingRequest':
				writingRequest(ws.id);

				if (currentWriter == ws.id) {
					ws.send('{ "type":"hasRights" }');  //informs the workspace it has writing rights
		        	var data = '{ "type":"newWriter", "nickname":"'+ crypt(ws.nickname) +' "}'; //informs other worspaces
					broadcastToEveryoneElse(ws, data);
				}
				else {
					//Send the position of the client in the FIFO to the client
					var data = '{ "type":"positionInQueue", "position":"'+ crypt((getPositionInQueue(ws.id)).toString())+'" }';
					ws.send(data);
				}
		        break;

		    case 'releaseRequest':
		    	//If currentWriter, the client at position 0 in the fifo becomes the new writer
		    	if (currentWriter == ws.id) {
		    		if (typeof writingFifo[0] != 'undefined'){
		    			currentWriter = writingFifo[0];
		    			workspaces[writingFifo[0]].send('{ "type":"hasRights" }');
		        		var data = '{ "type":"newWriter", "nickname":"'+ crypt(workspaces[writingFifo[0]].nickname) +' "}';
						broadcastToEveryoneElse(workspaces[writingFifo[0]], data);

						shiftFifo(); //dequeue
						updateQueuePosition(ws, 0); //When the first one in queue is removed, everyone's position is updated
		    		}
		    		else {
		    			currentWriter = null;
		    			var data = '{ "type":"newWriter", "nickname":"' + crypt("") +'"}';
		    			broadcast(data);
		    		}
		    	}
		    	//Workspace just wants to leave the queue
		    	else {
		    		var currentPosition =  getPositionInQueue(ws.id);
		    		removeFromFifo(ws.id);
		    		updateQueuePosition(ws, currentPosition); //When someone leaves the queue, only the ones after him have to be updated
		    		ws.send('{ "type":"leftQueue" }');
		    	}
		        break;
	    }   
    })

  	//When a client is closed, inform other workspaces
    ws.on("close", function() {
    	var data = '{ "type":"userLeft", "nickname":"'+ crypt(ws.nickname) +' "}';
    	broadcastToEveryoneElse(ws, data);
    	workspaces.splice(ws.id); //removes the closed workspace from the list
    	removeFromFifo(ws.id); //removes from fifo
    })
});


//broadcasts a message to everyone except the sender
function broadcastToEveryoneElse(ws, data) {
	//Broadcast to other workspaces
	wss.clients.forEach(function each(client) {
		if (client !== ws && client.readyState === 1) {
	    	client.send(data);
		}
	});
}

//broadcasts a message to everyoneS
function broadcast(data) {
	//Broadcast to other workspaces
	wss.clients.forEach(function each(client) {
		if (client.readyState === 1) {
	    	client.send(data);
		}
	});
}

//Manage insertion into Fifo and sets currentWriter if empty
function writingRequest(workspaceId) {
	//Make sure the workspace is not already in the FIFO
	if (checkIfInQueue(workspaceId) == -1) {
		//Check is FIFO is empty, if so sets current writer
		if (writingFifo.length == 0 && currentWriter == null) {
			currentWriter = workspaceId;
		}
		else {
			writingFifo.push(workspaceId);
		}
	}
}

//Removes a specific workspace from the queue
function removeFromFifo(workspaceId) {
	var index = checkIfInQueue(workspaceId);
	if (index != -1) {
	    writingFifo.splice(index, 1);
	}
}

//Current writer releases the his writing rights
function shiftFifo() {
	if (writingFifo.length > 0) {
		currentWriter = writingFifo.shift();
		return currentWriter;
	}
}

//Returns the position of the workspace in the queue, adds 1 so first position is "1" and not "0"
function getPositionInQueue(id) {
	return writingFifo.indexOf(id) + 1;
}

//Check if the workspace is in queue, returns -1 if it isn't
function checkIfInQueue(id) {
	return writingFifo.indexOf(id);
}

//When the writer releases his rights or somebody leaves the queue, updates new positions
function updateQueuePosition(ws, leaverPositionInQueue) {
	//Broadcast to other workspaces
	wss.clients.forEach(function each(client) {
		var workspacePositionInQueue = getPositionInQueue(client.id);
		//Check if the workspace is in the queue and if his position changed
		if (client !== ws && client.readyState === 1 && workspacePositionInQueue > 0 && leaverPositionInQueue <= workspacePositionInQueue && client.id != currentWriter) {  //dirty af, but idk what else to do
	    	var data = '{ "type":"positionInQueue", "position":"'+ crypt((getPositionInQueue(client.id)).toString())+'" }';
			client.send(data);
		}
	});
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