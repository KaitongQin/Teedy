'use strict';

angular.module('docs').controller('ModalRegisterRequest', function ($scope, $uibModalInstance) {
  $scope.data = {};
  $scope.close = function(data) {
    $uibModalInstance.close(data);
  }
});
