//Server initialisation
const WebSocketServer = require("ws").Server;
const CryptoJS = require("crypto-js");
const PrivateKey = 'LOG735';
const ServerPort = process.argv[2];

var ws = new WebSocketServer( { port: ServerPort } );
console.log("Server started...");

//id sent to workspaces;
var currentId = 1;


//Called when a workspace connects to the server
ws.on('connection', function (ws) {
  	console.log("Browser connected online...");
   
  	ws.on("message", function (str) {
	    var object = JSON.parse(str);
	    switch(object.type) {
	        //A new workspace connects and requests and ID from the server
	        case 'idRequest':
		        console.log("Received idRequest");     
    			ws.send('{ "type":"idRequest", "id":"'+ crypt(currentId.toString()) +' "}'); 
		        currentId++;
		        break;
	    	}   
    })

    ws.on("close", function() {
        console.log("Browser gone.")
    })
});


//Returns a crypted object
function crypt(object) {
	return CryptoJS.AES.encrypt(object, PrivateKey);
}

//Returns a decrypted object
function decrypt(object) {
	var bytes  = CryptoJS.AES.decrypt(object.toString(), PrivateKey);
	return bytes.toString(CryptoJS.enc.Utf8);
}