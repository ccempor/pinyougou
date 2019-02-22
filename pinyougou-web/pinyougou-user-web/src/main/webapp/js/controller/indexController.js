/** 定义控制器层 */
app.controller('indexController', function($scope, baseService){
    // 定义获取登录用户名
    $scope.loadUsername= function () {
        // 发送异步请求
        baseService.sendGet("/user/showName").then(function(response){
            $scope.loginName = response.data.loginName;
        });
    };

    $scope.statusArr =["等待买家付款","买家已付款","未发货","已发货","交易成功","交易关闭","待评价"];

    $scope.searchEntity = {page:1,rows:4}
    // 订单搜索方法
    $scope.search = function () {

        // 发送异步请求
        baseService.sendGet("/order/searchOrder?page="+$scope.searchEntity.page+"&rows="+$scope.searchEntity.rows).then(function(response){

            $scope.pageResult =  response.data;
            $scope.jumpPage = $scope.searchEntity.page;
            // 生成页码
            $scope.initPages();
        });
    };

    // 生成页码的方法
    $scope.initPages = function () {
        // 定义页码数组
        $scope.pageNums = [];
        // 定义开始页码
        var firstPage = 1;
        // 定义结束页码
        var lastPage = $scope.pageResult.total;

        // 控制省略号
        $scope.firstDot = true;
        $scope.lastDot = true;

        // 判断总页数是不是大于3
        if ($scope.pageResult.total > 3){
            // 判断当前页码是否靠首页近些
            if ($scope.searchEntity.page <= 2){
                $scope.firstDot = false;
                lastPage = 3; // 结束页码
            }else if ($scope.searchEntity.page >= $scope.pageResult.total - 1){
                // 判断当前页码是否靠尾页近些
                firstPage = $scope.pageResult.total - 2; // 开始页码
                $scope.lastDot = false;
            }else{
                // 当前页码在中间位置
                firstPage = $scope.searchEntity.page - 1;
                lastPage = $scope.searchEntity.page + 1;
            }
        }else{
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        // 循环产生页码
        for (var i = firstPage; i <= lastPage; i++){
            $scope.pageNums.push(i);
        }
    };

    // 根据页码搜索
    $scope.pageSearch = function (page) {
        //alert(typeof page);
        page = parseInt(page);
        if (page > $scope.pageResult.total){
            page = $scope.pageResult.total;
        }
        if (page < 1){
            page = 1;
        }
        // 判断页码的有效性
        if (page >= 1 && page != $scope.searchEntity.page
            && page <= $scope.pageResult.total){

            $scope.searchEntity.page = page;
            // 执行搜索
            $scope.search();
        }
        $scope.jumpPage = page;
    };

    $scope.toPayment=function (orderId) {
        baseService.sendGet("/order/savePaylog?orderId="+orderId).then(function (response) {
            if(response.data){
                location.href= "/order/pay.html";
            }else {
                alert("操作失败")
            }
        })
    }

    $scope.toBigInt=function (num) {
        return BigInt(num)
    }
});