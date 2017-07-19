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