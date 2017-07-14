/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/

//Server initialisation
const WebSocket = require("ws");
const CryptoJS = require("crypto-js");
const Tcpp = require('tcp-ping');
const _ = require('lodash');

const PrivateKey = 'LOG735';
const thisServerName = process.argv[2];

var currentId = 0;
var workspaces = {}; //contains clients websocket
var serverList = {}; //contains other servers info
var serverConnections = {}; //contains servers websocket
var serversConnectedTo = {}; //contains servers it's connected to
var isMain = false;
var sharedText = '';
var currentWriter = null;
var writingFifo = [];
var wsClient; //Client port listener
var wsServer; //Server port listener

initializeServerList();
const clientPort = serverList[thisServerName].clientPort;
const serverPort = serverList[thisServerName].serverPort;

connectToExistingServers();


//Tries to ping other servers to see if they are online, if so, connects to them
function connectToExistingServers() {
	var finished = _.after(Object.keys(serverList).length - 1, startServer);

	Object.keys(serverList).map(function(objectKey, index) {
	    var value = serverList[objectKey];
	    if (thisServerName !== value.serverName) {
			Tcpp.probe(value.ip, value.serverPort, function(err, available) {
			    if (available) {
			    	serversConnectedTo[value.serverName]  = new WebSocket('ws://'+value.ip + ':' + value.serverPort);
			    	setClientServerListeners(value.serverName);
			    }
				finished();
			});
		}
	});	
}

//Starts the server and check if he's the main
function startServer() {
	if (Object.keys(serversConnectedTo).length == 0) {
		isMain = true;
	}

	console.log(isMain);

	wsClient = new WebSocket.Server( { port: clientPort } );
	console.log("Client port ready..." + clientPort);
	setClientListeners();

	wsServer = new WebSocket.Server( { port: serverPort } );
	console.log("Server port ready..." + serverPort);
	setServerListeners();
}


//Server port listener
function setServerListeners() {
	wsServer.on('connection', function connection(ws) {

		ws.on('message', function incoming(str) {
			var object = JSON.parse(str);
		    switch(object.type) {
		    	//When a server connects to another server
			    case 'newClientServer':
			    	ws.serverName = object.serverName;
					serverConnections[object.serverName] = ws;
					console.log("new server connected to me : " + object.serverName);

					if (isMain) {
						syncFifo();
						syncText();
					}					
			        break;
			}
		});

		//When a server is closed, inform other workspaces
	    ws.on("close", function() {
	    	console.log(ws.serverName + " just closed");
	    	delete serverConnections[ws.serverName]; //removes the closed server from the list
	    })
	});
}

//When a server is client to another server.
function setClientServerListeners(serverName) {
	serversConnectedTo[serverName].onopen = function(event) {
		//Tell other servers i'm connected to them
		serversConnectedTo[serverName].send('{ "type":"newClientServer", "serverName":"'+thisServerName+'" }');
	};

	serversConnectedTo[serverName].onmessage = function(event) { 
		var message = JSON.parse(event.data);

		switch(message.type) {
			case "syncFifo":
		    	currentWriter = message.currentWriter;
		    	writingFifo = message.currentFifo;
		    break;

		    case "syncText":
		    	sharedText = message.currentText;
		    break;
		}
	};
}

//Sets all the client listeners
function setClientListeners() {
	//Called when a workspace connects to the server
	wsClient.on('connection', function (ws) {
	  	console.log("Browser connected online...");
	  	//A new workspace connects and requests and ID from the server
	  	ws.id = currentId;
	  	workspaces[ws.id.toString()] = ws;
	  	ws.send('{ "type":"idRequest", "id":"'+ crypt(currentId.toString()) +' "}'); //Informs the workspace of his ID
	  	currentId++;

	  	//If a workspace connects and there's a current writer
	  	if (currentWriter != null) {
			var data = '{ "type":"newWriter", "nickname":"'+ crypt(workspaces[currentWriter.toString()].nickname) +' "}';
			ws.send(data);
	  	}

	  	//If a workspace joins and the text is not empty
	  	if (sharedText != '') {
	  		var data = '{ "type":"updateSharedText", "newText":"'+ crypt(sharedText) +' "}';
	  		ws.send(data);
	  	}

	    //When a workspace sends a message to the server
	  	ws.on("message", function (str) {
		    var object = JSON.parse(str);

		    switch(object.type) {
		    	//Sets the nickname for the workspace
			    case 'nicknameRequest':
					workspaces[ws.id.toString()].nickname = decrypt(object.nickname); //Saves nickname
			        var data = '{ "type":"newUser", "nickname":"'+ object.nickname +' "}';
			        broadcastToClientsExeptSender(ws, data);
			        break;

			    case 'writingRequest':
					writingRequest(ws.id);

					if (currentWriter == ws.id) {
						ws.send('{ "type":"hasRights" }');  //informs the workspace it has writing rights
			        	var data = '{ "type":"newWriter", "nickname":"'+ crypt(ws.nickname) +' "}'; //informs other workspaces
						broadcastToClientsExeptSender(ws, data);
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
			    		var newText = decrypt(object.sharedText);
			    		sharedText = newText; //Update the new text

			    		assignNextWriter(ws);		    		
			    	}
			    	//Workspace just wants to leave the queue
			    	else {
			    		var currentPosition =  getPositionInQueue(ws.id);
			    		removeFromFifo(ws.id);
			    		updateQueuePosition(ws, currentPosition); //When someone leaves the queue, only the ones after him have to be updated
			    		ws.send('{ "type":"leftQueue" }');
			    	}

			    	updateSharedText(ws);
			        break;

			    case 'updateSharedText':
			    	if (currentWriter == ws.id) {
			    		var newText = decrypt(object.sharedText);
			    		sharedText = newText; //Update the new text
			    		
			    		updateSharedText(ws);
			    	}
			    	break;
		    }   
	    })

	  	//When a client is closed, inform other workspaces
	    ws.on("close", function() {
	    	var data = '{ "type":"userLeft", "nickname":"'+ crypt(ws.nickname) +' "}';
	    	broadcastToClientsExeptSender(ws, data);

	    	//If the closed workspce was in queue, proceed to removed it from the the queue and update queue positions
	    	var currentPosition =  getPositionInQueue(ws.id);
	    	if (currentPosition > 0) {
	    		removeFromFifo(ws.id); //removes from fifo
				updateQueuePosition(ws, currentPosition); //When someone leaves the queue, only the ones after him have to be updated
	    	} 
	    	//If the closed workspace is the one with the token
	    	else if (currentWriter == ws.id) {
	    		assignNextWriter(ws);
	    	}

	    	delete workspaces[ws.id.toString()]; //removes the closed workspace from the list
	    })
	});
}


//Simply initialize the array with other servers info
function initializeServerList() {
	serverList["server1"] = {"serverName":"server1", "ip":"127.0.0.1", "clientPort":"8080", "serverPort":"8081"};
	serverList["server2"] = {"serverName":"server2", "ip":"127.0.0.1", "clientPort":"8082", "serverPort":"8083"};
	serverList["server3"] = {"serverName":"server3", "ip":"127.0.0.1", "clientPort":"8084", "serverPort":"8085"};
}

//Broadcasts a message to everyone in a group except the sender
function broadcastToClientsExeptSender(ws, data) {
	//Broadcast to other workspaces
	wsClient.clients.forEach(function each(client) {
		if (client !== ws && client.readyState === 1) {
	    	client.send(data);
		}
	});
}

//broadcasts a message to everyoneS
function broadcastToEveryClient(data) {
	//Broadcast to other workspaces
	wsClient.clients.forEach(function each(client) {
		if (client.readyState === 1) {
	    	client.send(data);
		}
	});
}

function broadcastToServers(data) {
	//Broadcast to other servers
	wsServer.clients.forEach(function each(client) {
		if (client.readyState === 1) {
	    	client.send(data, function ack(error) {
				if (typeof error != 'undefined') {
					console.log("erreur dans la synchro");
					//todo je sais pas comment faire l'erreur
				}
				else {
					console.log("Synchro avec "+client.serverName+" réussie");
				}
			});
		}
	});
}

function syncFifo() {
	var data = '{ "type":"syncFifo", "currentWriter":"'+currentWriter+'", "currentFifo":"'+writingFifo+'" }';
	broadcastToServers(data);
}

function syncText() {
	var data = '{ "type":"syncText", "currentText":"'+sharedText+'" }';
	broadcastToServers(data);
}

function updateSharedText(ws) {
	syncText();

	//send updated text to other workspaces
	var data = '{ "type":"updateSharedText", "newText":"'+ crypt(sharedText) +' "}';
	broadcastToClientsExeptSender(ws, data);
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

		syncFifo();
	}
}

//Removes a specific workspace from the queue
function removeFromFifo(workspaceId) {
	var index = checkIfInQueue(workspaceId);
	if (index != -1) {
	    writingFifo.splice(index, 1);
	    syncFifo();
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
	wsClient.clients.forEach(function each(client) {
		var workspacePositionInQueue = getPositionInQueue(client.id);
		//Check if the workspace is in the queue and if his position changed
		if (client !== ws && client.readyState === 1 && workspacePositionInQueue > 0 && leaverPositionInQueue <= workspacePositionInQueue && client.id != currentWriter) {  //dirty af, but idk what else to do
	    	var data = '{ "type":"positionInQueue", "position":"'+ crypt((getPositionInQueue(client.id)).toString())+'" }';
			client.send(data);
		}
	});
}


//Assigns the next writer if the queue is not empty, send a notice to workspace about the new writer and updates the queue positions
function assignNextWriter(ws) {
	if (typeof writingFifo[0] != 'undefined') {
		currentWriter = writingFifo[0];
		workspaces[currentWriter.toString()].send('{ "type":"hasRights" }');
		var data = '{ "type":"newWriter", "nickname":"'+ crypt(workspaces[currentWriter.toString()].nickname) +' "}';
		broadcastToClientsExeptSender(workspaces[currentWriter.toString()], data);

		shiftFifo(); //dequeue
		updateQueuePosition(ws, 0); //When the first one in queue is removed, everyone's position is updated
	}
	else {
		currentWriter = null;
		var data = '{ "type":"newWriter", "nickname":"' + crypt("") +'"}';
		broadcastToEveryClient(data);
	}

	syncFifo();	
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