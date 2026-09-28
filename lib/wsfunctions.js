/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function jsonToQueryString(json) {
return Object.keys(json).map(function(key) {
        return encodeURIComponent(key) + '=' +
            encodeURIComponent(json[key]);
    }).join('&');
}

function LOGIN(myWSS, customerID, correlId, token, channel, functionId) {

	var request = 
{
 "requests": [
  {
   "requestid": 0,
   "service": "ADMIN",
   "command": "LOGIN",
   "SchwabClientCustomerId": customerID,
   "SchwabClientCorrelId": correlId,
   "parameters": {
    "Authorization": token,
    "SchwabClientChannel": channel,
    "SchwabClientFunctionId": functionId
   }
  }
 ]
}

    myWSS.sendMessage(JSON.stringify(request));
} 


function SUBS(myWSS, service, requestId, customerId, correlId, keys, fields) {

    var request = {
                    "requests": [
                    {
                                    "service": service,
                                    "requestid": requestId,
                                    "command": "SUBS",
                                    "SchwabClientCustomerId": customerId,
                                    "SchwabClientCorrelId": correlId,
                                    "parameters": {
                                                    "keys": keys,
                                                    "fields": fields
                                    }
                    }
                    ]
    }
    myWSS.sendMessage(JSON.stringify(request));
}



function UNSUBS(myWSS, service, requestId, customerId, correlId, keys, fields) {

    var request = {
                    "requests": [
                    {
                                    "service": service,
                                    "requestid": requestId,
                                    "command": "UNSUBS",
                                    "SchwabClientCustomerId": customerId,
                                    "SchwabClientCorrelId": correlId,
                                    "parameters": {
                                                    "keys": keys,
                                                    "fields": fields
                                    }
                    }
                    ]
    }
    myWSS.sendMessage(JSON.stringify(request));
}
function ADD(myWSS, service, requestId, customerId, correlId, keys, fields) {

    var request = {
                    "requests": [
                    {
                                    "service": service,
                                    "requestid": requestId,
                                    "command": "ADD",
                                    "SchwabClientCustomerId": customerId,
                                    "SchwabClientCorrelId": correlId,
                                    "parameters": {
                                                    "keys": keys,
                                                    "fields": fields
                                    }
                    }
                    ]
    }
    myWSS.sendMessage(JSON.stringify(request));
}
 
function VIEW(myWSS, requestId, accountId, appId, QOSlevel) {

    var request = {
                    "requests": [
                    {
                                    "service": service,
                                    "requestid": requestId,
                                    "command": "ADD",
                                    "SchwabClientCustomerId": customerId,
                                    "SchwabClientCorrelId": correlId,
                                    "parameters": {
                                                    "keys": keys,
                                                    "fields": fields
                                    }
                    }
                    ]
    }
    myWSS.sendMessage(JSON.stringify(request));
 }

function LOGOUT(myWSS, requestId, customerId, correllId) {

    var request = {
                    "requests": [
                    {
                                    "service": "ADMIN",
                                    "command": "LOGOUT",
                                    "requestid": requestId,
                                    "SchwabClientCustomerId": customerId,
                                    "SchwabClientCorrelId": correllId,
                                    "parameters": { }
                    }
                    ]
    }
    myWSS.sendMessage(JSON.stringify(request));
 }

