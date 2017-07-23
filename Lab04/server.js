/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
const ServerObject = require("./serverObject.js");
var thisServer = new ServerObject(process.argv[2]);

thisServer.connectToExistingServers();
