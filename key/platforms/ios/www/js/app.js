angular.module('key', ['ionic'])
//.factory('udpserv')
.controller('keyctrl', ['$scope', 'chrome', function($scope, chrome) {
  //hgfjhgf
}])
  .run(function($ionicPlatform, $timeout) {
    $ionicPlatform.ready(function() {
    alert('window.cordova: ' + Object.keys(window.cordova).join(', '));
    alert("printed windows.cordova");
    // $scope.sendcmd = function sendcmd(cmd) {
    //   console.log("cmd: " + cmd);
    //   chrome.sockets.udp.create({}, function(socketInfo) {
    //     var socketId = socketInfo.socketId;
    //     chrome.sockets.udp.send(socketId, cmd,
    //       '10.0.0.9', 2390, function(sendInfo) {
    //         console.log("sent " + sendInfo.bytesSent);
    //     });
    //   });
    // };
  });
});
