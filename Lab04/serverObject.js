/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
//Server initialisation
const WebSocket = require("ws");
const Tcpp = require('tcp-ping');
const _ = require('lodash');
const CryptoHelper = require('./cryptoHelper.js');
const ServerInfo = require('./serverInfo.js');

function ServerObject(n) {
	var thisServer = this;

    thisServer.serverName = n;
    thisServer.currentId = 0;
	thisServer.workspaces = {}; //contains clients websocket
	thisServer.serverConnections = {}; //contains servers websocket
	thisServer.serversConnectedTo = {}; //contains servers it's connected to
	thisServer.isMain = false;
	thisServer.sharedText = '';
	thisServer.currentWriter = -1;
	thisServer.writingFifo = [];
	thisServer.wsClient; //Client port listener
	thisServer.wsServer; //Server port listener
	thisServer.serverList = ServerInfo.initializeServerList();
	thisServer.clientPort = thisServer.serverList[thisServer.serverName].clientPort;
	thisServer.serverPort = thisServer.serverList[thisServer.serverName].serverPort;

	thisServer.connectToExistingServers = function () {
		var finished = _.after(Object.keys(thisServer.serverList).length - 1, startServer);

		Object.keys(thisServer.serverList).map(function(objectKey, index) {
		    var value = thisServer.serverList[objectKey];
		    if (thisServer.serverName !== value.serverName) {
				Tcpp.probe(value.ip, value.serverPort, function(err, available) {
				    if (available) {
				    	thisServer.serversConnectedTo[value.serverName] = new WebSocket('ws://'+value.ip + ':' + value.serverPort);
				    	thisServer.serversConnectedTo[value.serverName].serverName = value.serverName;
				    	setClientServerListeners(value.serverName);
				    }
					finished();
				});
			}
		});	
	}

	//Checks if this node is now the main node
	setMain = function() {
		if (Object.keys(thisServer.serversConnectedTo).length == 0) {
			thisServer.isMain = true;
		}
	}

	//Starts the server and check if he's the main
	startServer = function() {
		setMain();
		console.log(thisServer.isMain);

		thisServer.wsClient = new WebSocket.Server( { port: thisServer.clientPort } );
		console.log("Client port ready..." + thisServer.clientPort);
		setClientListeners();

		thisServer.wsServer = new WebSocket.Server( { port: thisServer.serverPort } );
		console.log("Server port ready..." + thisServer.serverPort);
		setServerListeners();
	}

	//When a server is client to another server.
	setClientServerListeners = function(serverName) {
		thisServer.serversConnectedTo[serverName].onopen = function(event) {
			//Tell other servers i'm connected to them
			thisServer.serversConnectedTo[serverName].send('{ "type":"newClientServer", "serverName":"'+thisServer.serverName+'" }');
		};

		thisServer.serversConnectedTo[serverName].onmessage = function(event) { 
			var message = JSON.parse(event.data);

			switch(message.type) {
				case "syncFifo":
			    	thisServer.currentWriter = message.currentWriter;
			    	thisServer.writingFifo = JSON.parse(message.currentFifo);

			    	console.log(thisServer.writingFifo);
			    break;

			    case "syncText":
			    	thisServer.sharedText = CryptoHelper.decrypt(message.currentText);
			    break;

			    case "syncCurrentId":
			    	thisServer.currentId = message.currentId;
			    break;
			}
		};

		//Triggered when the server is closed
		thisServer.serversConnectedTo[serverName].onclose = function (event) {
			delete thisServer.serversConnectedTo[serverName];

			setMain();
			console.log(thisServer.isMain);
		};
	}

	//Sets all the client listeners
	setClientListeners = function() {
		//Called when a workspace connects to the server
		thisServer.wsClient.on('connection', function (ws) {
		  	if (thisServer.isMain) {
		  		console.log("Browser connected online...");
		  		//When a workspace sends a message to the server
			  	ws.on("message", function (str) {
				    var object = JSON.parse(str);

				    switch(object.type) {
				    	//Sets the nickname for the workspace
					    case 'connectionRequest':
				    		//A new workspace connects and requests an ID from the server
					    	workspaceId = parseInt(CryptoHelper.decrypt(object.id));
						  	ws.id = workspaceId == -1 ? thisServer.currentId : workspaceId;
						  	thisServer.workspaces[ws.id.toString()] = ws;
						  	ws.send('{ "type":"idRequest", "id":"'+ CryptoHelper.crypt(ws.id.toString()) +' "}'); //Informs the workspace of his ID
						  	thisServer.currentId++;
						  	syncCurrentId();

							thisServer.workspaces[ws.id.toString()].nickname = CryptoHelper.decrypt(object.nickname); //Saves nickname
					        var data = '{ "type":"newUser", "nickname":"'+ object.nickname +' "}';

					        //only if it's a new workspace
					        if (workspaceId == -1) {
					        	broadcastToClientsExeptSender(ws, data);

					        	//If a workspace connects and there's a current writer
							  	if (thisServer.currentWriter != -1 && typeof thisServer.workspaces[thisServer.currentWriter.toString()] != 'undefined') {
									var data = '{ "type":"newWriter", "nickname":"'+ CryptoHelper.crypt(thisServer.workspaces[thisServer.currentWriter.toString()].nickname) +' "}';
									ws.send(data);
							  	}

							  	//If a workspace joins and the text is not empty
							  	if (thisServer.sharedText != '') {
							  		var data = '{ "type":"updateSharedText", "newText":"'+ CryptoHelper.crypt(thisServer.sharedText) +' "}';
							  		ws.send(data);
							  	}

							  	//If there are redundant servers online
							  	if (Object.keys(thisServer.serverConnections).length != 0)  {
							  		Object.keys(thisServer.serverConnections).map(function(objectKey, index) {
									    var value = thisServer.serverConnections[objectKey];
									    sendServerInfoToClient(ws, value.serverName);
									});	
							  	}
					        }			    	
				        	break;

				        //Receives a writing request from a workspace
					    case 'writingRequest':	
							writingRequest(ws.id);

							if (thisServer.currentWriter == ws.id) {
								ws.send('{ "type":"hasRights" }');  //informs the workspace it has writing rights
					        	var data = '{ "type":"newWriter", "nickname":"'+ CryptoHelper.crypt(ws.nickname) +' "}'; //informs other workspaces
								broadcastToClientsExeptSender(ws, data);
							}
							else {
								//Send the position of the client in the FIFO to the client
								var data = '{ "type":"positionInQueue", "position":"'+ CryptoHelper.crypt((getPositionInQueue(ws.id)).toString())+'" }';
								ws.send(data);
							}
					        break;

					    //Receives a release request from a workspace
					    case 'releaseRequest':
					    	//If currentWriter, the client at position 0 in the fifo becomes the new writer
					    	if (thisServer.currentWriter == ws.id) {
					    		var newText = CryptoHelper.decrypt(object.sharedText);
					    		thisServer.sharedText = newText; //Update the new text

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

					    //Receives updated shared texte
					    case 'updateSharedText':
					    	if (thisServer.currentWriter == ws.id) {
					    		var newText = CryptoHelper.decrypt(object.sharedText);
					    		thisServer.sharedText = newText; //Update the new text
					    		
					    		updateSharedText(ws);
					    	}
					    	break;
				    }   
			    });

			  	//When a client is closed, inform other workspaces
			    ws.on("close", function() {
			    	var data = '{ "type":"userLeft", "nickname":"'+ CryptoHelper.crypt(ws.nickname) +' "}';
			    	broadcastToClientsExeptSender(ws, data);

			    	//If the closed workspce was in queue, proceed to removed it from the the queue and update queue positions
			    	var currentPosition =  getPositionInQueue(ws.id);
			    	if (currentPosition > 0) {
			    		removeFromFifo(ws.id); //removes from fifo
						updateQueuePosition(ws, currentPosition); //When someone leaves the queue, only the ones after him have to be updated
			    	} 
			    	//If the closed workspace is the one with the token
			    	else if (thisServer.currentWriter == ws.id) {
			    		assignNextWriter(ws);
			    	}

			    	delete thisServer.workspaces[ws.id.toString()]; //removes the closed workspace from the list
			    });
		  	}	
		  	else {
		  		redirectClient(ws);	
		  		ws.close();
		  	} 	 
		});
	}

	//Used to redirect a workspace when it connects to a server that is not the main
	redirectClient = function(ws) {
		var value = thisServer.serversConnectedTo[Object.keys(thisServer.serversConnectedTo)[0]];
		var data = '{ "type":"redirect", "serverName":"'+CryptoHelper.crypt(value.serverName)+'", "ip":"'+CryptoHelper.crypt(thisServer.serverList[value.serverName].ip)+'", "port":"'+CryptoHelper.crypt(thisServer.serverList[value.serverName].clientPort)+'" }';
		ws.send(data);
	}

	//Syncs the fifo with the other servers
	syncFifo = function() {
		var data = '{ "type":"syncFifo", "currentWriter":"'+thisServer.currentWriter+'", "currentFifo":"'+JSON.stringify(thisServer.writingFifo)+'" }';
		broadcastToServers(data);
	}

	//Syncs the text with the other servers
	syncText = function() {
		var data = '{ "type":"syncText", "currentText":"'+CryptoHelper.crypt(thisServer.sharedText)+'" }';
		broadcastToServers(data);
	}

	//Syncs the currentId with the other servers
	syncCurrentId = function() {
		var data = '{ "type":"syncCurrentId", "currentId":"'+thisServer.currentId+'" }';
		broadcastToServers(data);
	}

	//Sends the updated text to workspaces
	updateSharedText = function(ws) {
		syncText();

		//send updated text to other workspaces
		var data = '{ "type":"updateSharedText", "newText":"'+ CryptoHelper.crypt(thisServer.sharedText) +' "}';
		broadcastToClientsExeptSender(ws, data);
	}

	//Sends server info to workspaces
	sendServerInfoToClients = function(serverName) {
		var data = '{ "type":"newServer", "serverName":"'+CryptoHelper.crypt(serverName)+'", "ip":"'+CryptoHelper.crypt(thisServer.serverList[serverName].ip)+'", "port":"'+CryptoHelper.crypt(thisServer.serverList[serverName].clientPort)+'" }';
		broadcastToEveryClient(data);
	}

	//Sends server into to a specific workspace
	sendServerInfoToClient = function(ws, serverName) {
		var data = '{ "type":"newServer", "serverName":"'+CryptoHelper.crypt(serverName)+'", "ip":"'+CryptoHelper.crypt(thisServer.serverList[serverName].ip)+'", "port":"'+CryptoHelper.crypt(thisServer.serverList[serverName].clientPort)+'" }';
		ws.send(data);
	}

	//Informs workspace when a server is removed
	sendServerRemoveToClients = function(serverName) {
		var data = '{ "type":"serverRemove", "serverName":"'+CryptoHelper.crypt(serverName)+'" }';
		broadcastToEveryClient(data);
	}

	//Server port listener
	setServerListeners = function() {
		thisServer.wsServer.on('connection', function connection(ws) {
			ws.on('message', function incoming(str) {
				var object = JSON.parse(str);
			    switch(object.type) {
			    	//When a server connects to another server
				    case 'newClientServer':
				    	ws.serverName = object.serverName;
						thisServer.serverConnections[object.serverName] = ws;
						console.log("new server connected to me : " + object.serverName);

						if (thisServer.isMain) {
							syncFifo();
							syncText();
							syncCurrentId();
						}

						sendServerInfoToClients(object.serverName);					
				        break;
				}
			});

			//When a server is closed, inform other workspaces
		    ws.on("close", function() {
		    	console.log(ws.serverName + " just closed");
		    	delete thisServer.serverConnections[ws.serverName]; //removes the closed server from the list
		    	sendServerRemoveToClients(ws.serverName);
		    })
		});
	}

	//Broadcasts a message to everyone in a group except the sender
	broadcastToClientsExeptSender = function(ws, data) {
		//Broadcast to other workspaces
		thisServer.wsClient.clients.forEach(function each(client) {
			if (client !== ws && client.readyState === 1) {
		    	client.send(data);
			}
		});
	}

	//broadcasts a message to everyoneS
	broadcastToEveryClient = function(data) {
		//Broadcast to other workspaces
		thisServer.wsClient.clients.forEach(function each(client) {
			if (client.readyState === 1) {
		    	client.send(data);
			}
		});
	}

	//Broadcasts a message to every server, used to sync servers
	broadcastToServers = function(data) {
		//Broadcast to other servers
		thisServer.wsServer.clients.forEach(function each(client) {
			if (client.readyState === 1) {
		    	client.send(data, function ack(error) {
		    		//If an error occurs with a server during the sync, close the connection
					if (typeof error != 'undefined') {
						console.log("Erreur de synchro avec "+client.serverName);
						client.close();
					}
					else {
						console.log("Synchro avec "+client.serverName+" réussie");
					}
				});
			}
		});
	}


	//Manage insertion into Fifo and sets currentWriter if empty
	writingRequest = function(workspaceId) {
		//Make sure the workspace is not already in the FIFO
		if (checkIfInQueue(workspaceId) == -1) {
			//Check is FIFO is empty, if so sets current writer
			if (thisServer.writingFifo.length == 0 && thisServer.currentWriter == -1) {
				thisServer.currentWriter = workspaceId;
			}
			else {
				thisServer.writingFifo.push(workspaceId);
			}

			syncFifo();
		}
	}

	//Removes a specific workspace from the queue
	removeFromFifo = function(workspaceId) {
		var index = checkIfInQueue(workspaceId);
		if (index != -1) {
		    thisServer.writingFifo.splice(index, 1);
		    syncFifo();
		}
	}

	//Current writer releases the his writing rights
	shiftFifo = function() {
		if (thisServer.writingFifo.length > 0) {
			thisServer.currentWriter = thisServer.writingFifo.shift();
			return thisServer.currentWriter;
		}
	}

	//Returns the position of the workspace in the queue, adds 1 so first position is "1" and not "0"
	getPositionInQueue = function(id) {
		return thisServer.writingFifo.indexOf(id) + 1;
	}

	//Check if the workspace is in queue, returns -1 if it isn't
	checkIfInQueue = function(id) {
		return thisServer.writingFifo.indexOf(id);
	}

	//When the writer releases his rights or somebody leaves the queue, updates new positions
	updateQueuePosition = function(ws, leaverPositionInQueue) {
		//Broadcast to other workspaces
		thisServer.wsClient.clients.forEach(function each(client) {
			var workspacePositionInQueue = getPositionInQueue(client.id);
			//Check if the workspace is in the queue and if his position changed
			if (client !== ws && client.readyState === 1 && workspacePositionInQueue > 0 && leaverPositionInQueue <= workspacePositionInQueue && client.id != thisServer.currentWriter) {  //dirty af, but idk what else to do
		    	var data = '{ "type":"positionInQueue", "position":"'+ CryptoHelper.crypt((getPositionInQueue(client.id)).toString())+'" }';
				client.send(data);
			}
		});
	}


	//Assigns the next writer if the queue is not empty, send a notice to workspace about the new writer and updates the queue positions
	assignNextWriter = function(ws) {
		if (typeof thisServer.writingFifo[0] != 'undefined') {
			thisServer.currentWriter = thisServer.writingFifo[0];
			thisServer.workspaces[thisServer.currentWriter.toString()].send('{ "type":"hasRights" }');
			var data = '{ "type":"newWriter", "nickname":"'+ CryptoHelper.crypt(thisServer.workspaces[thisServer.currentWriter.toString()].nickname) +' "}';
			broadcastToClientsExeptSender(thisServer.workspaces[thisServer.currentWriter.toString()], data);

			shiftFifo(); //dequeue
			updateQueuePosition(ws, 0); //When the first one in queue is removed, everyone's position is updated
		}
		else {
			thisServer.currentWriter = -1;
			var data = '{ "type":"newWriter", "nickname":"' + CryptoHelper.crypt("") +'"}';
			broadcastToEveryClient(data);
		}

		syncFifo();	
	}

}

module.exports = ServerObject;