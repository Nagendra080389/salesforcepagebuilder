var testApp = angular.module("sfPageBuilder", []);

testApp.controller("sfPageController", function($scope, $http) {
    $http.get("/getObjectNames").then(
        function successCallback(response) {
            $scope.objectsName = response.data;
        },
        function errorCallback(response) {
            console.log("Unable to perform get request");
        }
    );

    $scope.fetchFields = function() {
        $http.get("/getSobjectFields", {
            params: {strObjectName: $scope.selectedObjectName}
        }).then(
            function successCallback(response) {
                $scope.allFields = response.data;
            },
            function errorCallback(response) {
                console.log("Unable to perform get request");
            }
        );
    }
});