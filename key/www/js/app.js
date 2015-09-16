angular.module('key', ['ionic'])
//.factory('udpfact')
.controller('keyctrl', ['$scope', 'chrome', function($scope, chrome) {
  //nothin
}])
  .run(function($ionicPlatform, $timeout) {
    alert("runnin");
    $ionicPlatform.ready(function() {
      alert('window.cordova: ' + Object.keys(window.cordova).join(', '));
      alert('window.cordova: ' + Object.keys(window.cordova.plugins).join(', '));
      alert(JSON.stringify(window.cordova));
      alert(JSON.stringify(window.cordova.plugins));

      alert("printed windows.cordova");
      $scope.sendcmd = function sendcmd(cmd) {
        alert("cmd: " + cmd);
        chrome.sockets.udp.create({}, function(socketInfo) {
          var socketId = socketInfo.socketId;
          chrome.sockets.udp.send(socketId, cmd,
            '10.0.0.9', 2390, function(sendInfo) {
              alert("sent " + sendInfo.bytesSent);
          });
        });
      };
  });
});
