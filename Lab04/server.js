//Server initialisation
const WebSocketServer = require("ws").Server;
const serverPort = process.argv[2];
var ws = new WebSocketServer( { port: serverPort } );
console.log("Server started...");

//id sent to workspaces;
var currentId = 1;


//Called when a workspace connects to the server
ws.on('connection', function (ws) {
  	console.log("Browser connected online...")
   
  	ws.on("message", function (str) {
    var ob = JSON.parse(str);
    switch(ob.type) {
    	case 'text':
	        console.log("Received: " + ob.content)
	        break;

        //A new workspace connects and requests and ID from the server
        case 'idRequest':
	        console.log("Received idRequest");         
	        var data = '{ "type":"idRequest", "id":"' + currentId + '"}';
	        currentId++;
	        ws.send(data); 
	        break;
    	}   
    })

    ws.on("close", function() {
        console.log("Browser gone.")
    })
});