// 定义基础的控制器
app.controller('baseController', function($scope, baseService){

    // 定义获取登录用户名的方法
    $scope.loadUsername = function () {
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        // 发送异步请求
        baseService.sendGet("/user/showName").then(function(response){
            $scope.loginName = response.data.loginName;
        });
    };
});