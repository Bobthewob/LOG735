/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
var ServerInfo = {};
//Simply initialize the array with other servers info
ServerInfo.initializeServerList = function(object) {
	var serverList = {};
	serverList["server1"] = {"serverName":"server1", "ip":"127.0.0.1", "clientPort":"8080", "serverPort":"8081"};
	serverList["server2"] = {"serverName":"server2", "ip":"127.0.0.1", "clientPort":"8082", "serverPort":"8083"};
	serverList["server3"] = {"serverName":"server3", "ip":"127.0.0.1", "clientPort":"8084", "serverPort":"8085"};
	return serverList;
}

module.exports = ServerInfo;