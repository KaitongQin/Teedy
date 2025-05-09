'use strict';

angular.module('docs').controller('SettingsUserRequests', function($scope, Restangular, $translate, $dialog) {
  $scope.loadRequests = function() {
    Restangular.one('register_request/list').get().then(function(list) {
      $scope.requests = list.requests || list;  
    });
  };
  $scope.loadRequests();

  $scope.accept = function(req) {
    Restangular.allUrl('register_request', '/docs-web/api/register_request/' + req.id + '/accept').post().then(function() {
      $scope.loadRequests();
    });
  };
  $scope.reject = function(req) {
    Restangular.allUrl('register_request', '/docs-web/api/register_request/' + req.id + '/reject').post().then(function() {
      $scope.loadRequests();
    });
  };
});
