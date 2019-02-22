app.controller('orderController', function($scope, $controller,$interval,$location, baseService){
    // 指定继承cartController
    $controller('baseController', {$scope:$scope});


    // 生成支付二维码
    $scope.genPayCode = function () {
        // 发送异步请求
        baseService.sendGet("/order/genPayCode").then(function(response){
            // 获取响应数据 {outTradeNo : '', money : 0, codeUrl: ''}
            // 1. 交易订单号
            $scope.outTradeNo = response.data.outTradeNo;
            // 2. 交易金额
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            // 3. 微信支付url
            $scope.codeUrl = response.data.codeUrl;

            // 生成二维码图片
            document.getElementById("qrious").src="/barcode?url=" + $scope.codeUrl;

            /**
             * 开启定时器，间隔3秒发送异步请求检测支付状态
             * 第一个参数：回调函数
             * 第二个参数：间隔的毫秒数 3秒
             * 第三个参数：总调用次数 100
             */
            var timer = $interval(function(){
                baseService.sendGet("/order/queryPayStatus?outTradeNo="
                    + $scope.outTradeNo).then(function(response){
                    // 获取响应数据: {status : 1|2|3}
                    if (response.data.status == 1){ // 支付成功
                        // 取消定时器
                        $interval.cancel(timer);
                        // 跳转到支付成功页面
                        location.href = "/order/paysuccess.html?money=" + $scope.money;
                    }
                    if (response.data.status == 3){ // 支付失败
                        // 取消定时器
                        $interval.cancel(timer);
                        // 跳转到支付失败页面
                        location.href = "/order/payfail.html";
                    }
                });
            }, 3000, 100);

            // 总调用次数完成后，会调用then方法
            timer.then(function () {
                // 调用关闭订单接口
                // 定义提示信息
                $scope.msg = "二维码已过期，刷新页面重新获取二维码。";
            });

        });
    };

    // 获取支付金额
    $scope.getMoney = function () {
        return $location.search().money;
    };



});