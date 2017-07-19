/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Projet de session
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
 
const PrivateKey = 'LOG735';
var CryptoJS;

(function(CryptoHelper) {
	if (!(typeof exports === 'undefined')){
		CryptoJS = require("crypto-js");
	}
	//Returns a crypted object
	CryptoHelper.crypt = function(object) {
		return CryptoJS.AES.encrypt(object, PrivateKey);
	}

		//Returns a decrypted object
	CryptoHelper.decrypt = function(object) {
		var bytes  = CryptoJS.AES.decrypt(object.toString(), PrivateKey);
		return bytes.toString(CryptoJS.enc.Utf8);
	}
})(typeof exports === 'undefined'? this['CryptoHelper']={}: exports);