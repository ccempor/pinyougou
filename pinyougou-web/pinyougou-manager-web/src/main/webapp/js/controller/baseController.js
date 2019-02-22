/** 定义基础控制器 */
app.controller('baseController', function ($scope) {

    // 定义分页指令需要的配置信息对象
    $scope.paginationConf = {
        currentPage : 1, // 当前页码
        totalItems : 0, // 总记录数
        itemsPerPage : 10, // 页大小
        perPageOptions : [10,15,20,25,30], // 页码下拉列表框
        onChange : function () { // 页码发生改变，需要调用的函数(默认就会调用一次)
            $scope.reload();
        }
    };

    // 定义重新加载数据的方法
    $scope.reload = function () {
        // 分页查询品牌
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    };


    // 定义数组记录用户选择的品牌id
    $scope.ids = [];

    // 为checkbox绑定点击事件
    $scope.updateSelection = function ($event,$index,id) {
        // 事件对象: $event
        // 获取dom元素: $event.target
        // 判断checkbox是否选中
        if($event.target.checked){
            // checkbox选中
            // 往数组中添加元素
            $scope.ids.push(id);
        }else{
            // checkbox没有选中
            // 获取元素在数组中的索引号
            var idx = $scope.ids.indexOf(id);
            // 从数组中删除元素
            // 第一个参数：元素在数组中的索引号
            // 第二个参数：删除的个数
            $scope.ids.splice(idx, 1);

        }
        $scope.checkedArr[$index] = $event.target.checked;

        $scope.ckAll = $scope.dataList.length==$scope.ids.length ? true:false;

    };
    $scope.checkedArr = [];
    $scope.ckAll = false;

    $scope.updateAllSelection=function ($event) {
        $scope.checkedArr = [];
        $scope.ids=[]
        if($event.target.checked){
            for(var i = 0; i <$scope.dataList.length ; i++) {
                var data = $scope.dataList[i];
                if($scope.ids.indexOf(data.id)>=0){
                    continue;
                }
                $scope.ids.push(data.id);
                $scope.checkedArr.push(true);
            }
            $scope.ckAll = true;
        }else {
            $scope.ids = [];
            $scope.checkedArr = [];
            // $scope.iChecked = false;
        }
    }



    /** 提取数组中json某个属性，返回拼接的字符串(逗号分隔) */
    $scope.jsonArr2Str = function (jsonArrStr, key) {
        // [{"id":24,"text":"欧米媞"}]
        // 把json数组字符串转化成 json对象数组
        var jsonArr = JSON.parse(jsonArrStr);
        // 定义数组
        var res = [];
        for (var i = 0; i < jsonArr.length; i++){
            // 取一个数组元素 {"id":24,"text":"欧米媞"}
            var json = jsonArr[i];
            res.push(json[key]);
        }
        // join : 把数组中的元素用 逗号 分隔 返回一个字符串
        return res.join(",");
    };

});