/*// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
angular.module('starter', ['ionic'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if(window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
    }
    if(window.StatusBar) {
      StatusBar.styleDefault();
    }
  });
})
*/


// Utility functions to convert between array buffers and strings

/* permissions
"sockets": {
"udp": { "send" : ":*" }
}
*/
function stringToArrayBuffer(string) {
    var buffer = new ArrayBuffer(string.length);
    var bufView = new Uint8Array(buffer);
    for (var i=0; i < string.length; i++) {
        bufView[i] = string.charCodeAt(i);
    }
    return buffer;
}

function arrayBufferToString(buffer) {
    return String.fromCharCode.apply(null, new Uint8Array(buffer));
}

// Set the hostname; we'll need it for the HTTP request as well
var hostname = "www.yahoo.com";

document.addEventListener('deviceready', function() {
    chrome.socket.create("tcp", function(createInfo) {
        var socketId = createInfo.socketId;
        chrome.socket.connect(socketId, hostname, 80, function(result) {
            if (result === 0) {
                var requestString = "GET / HTTP/1.1\r\nHost: "+hostname+"\r\nConnection: close\r\n\r\n";
                var requestBuffer = stringToArrayBuffer(requestString);
                chrome.socket.write(socketId, requestBuffer, function(writeInfo) {
                    chrome.socket.read(socketId, 1000, function(readInfo) {
                        var htmlString = arrayBufferToString(readInfo.data);
                        // do something with htmlString here
                        console.log(htmlString);
                    });
                });
            }
        });
    });
}, false);



var arrayBuffer = 'l';
// Create the Socket
chrome.sockets.udp.create({}, function(socketInfo) {
  // The socket is created, now we can send some data
  var socketId = socketInfo.socketId;
  chrome.sockets.udp.send(socketId, arrayBuffer,
    '192.168.1.129', 2390, function(sendInfo) {
      console.log("sent " + sendInfo.bytesSent);
  });
});
