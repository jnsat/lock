angular.module('key', ['ionic'])
.controller('keyctrl', function($scope) {
  $scope.sendcmd = function sendcmd(cmd) {
    console.log("cmd: " + cmd);
    chrome.sockets.udp.create({}, function(socketInfo) {
      var socketId = socketInfo.socketId;
      chrome.sockets.udp.send(socketId, cmd,
        '192.168.1.129', 2390, function(sendInfo) {
          console.log("sent " + sendInfo.bytesSent);
      });
    });
  };
});
