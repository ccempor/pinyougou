// 定义品牌控制器
app.controller("brandController", function($scope, $controller, baseService){

    // 指定继承baseController 把baseController的$scope中的方法 全部给brandController的$scope
    $controller("baseController", {$scope : $scope});

    // 分页查询品牌
    $scope.search = function (page, rows) {

        // 发送异步请求
        baseService.findByPage("/brand/findByPage", page, rows,
            $scope.searchEntity).then(function(response){
            // 获取响应数据: 品牌的分页数据、总记录数 response.data: {total : 100, rows : [{},{}]}
            // 获取品牌分页数据 ng-repeat时用
            $scope.dataList = response.data.rows;
            // 更新分页指令中的总记录数
            $scope.paginationConf.totalItems = response.data.total;

            $scope.checkedArr = [];
            $scope.ckAll = false;
            $scope.ids=[]
        });

    };




    // 定义添加或修改品牌的方法
    $scope.saveOrUpdate = function () {

        var url = "save"; // 添加
        // 判断品牌的id
        if ($scope.entity.id){ // 不是undefined
            url = "update"; // 修改
        }
        // 发送异步请求
        baseService.sendPost("/brand/" + url, $scope.entity)
            .then(function(response){
            // 获取响应数据 response.data : true|false
            if (response.data){
                // 重新加载数据
                $scope.reload();
            }else{
                alert("操作失败！")
            }
        });
    };


    // 为修改按钮绑定点击事件
    $scope.show = function (entity) {
        // entity : {id : '', name : '', firstChar : ''}
        // 把json对象转化成json字符串
        var jsonStr = JSON.stringify(entity);
        // 把json字符串转化成json对象(产生一个新的json对象)
        $scope.entity = JSON.parse(jsonStr);
    };


    // 为删除按钮绑定点击事件
    $scope.delete = function () {
        // 判断用户是否选择了品牌
        if ($scope.ids.length > 0){
            baseService.deleteById("/brand/delete",
                $scope.ids).then(function(response){
                // 获取响应数据 response.data
                if (response.data){
                    // 删除成功
                    // 重新加载数据
                    $scope.reload();
                    // 清空ids数组
                    $scope.ids = [];
                }else{
                    alert("删除失败！");
                }
            });
        }else{
            alert("请选择要删除的品牌！");
        }
    };
});