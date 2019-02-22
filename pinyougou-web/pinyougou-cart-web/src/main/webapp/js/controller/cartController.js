app.controller('cartController', function($scope, $controller, baseService){
    // 指定继承baseController
    $controller('baseController', {$scope:$scope});

    // 获取购物车
    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart").then(function(response){
            // [{},{}]
            $scope.carts = response.data;

            // 定义json对象封装购买总数与总金额
            $scope.totalEntity = {totalItem : 0, totalNum : 0, totalMoney : 0};
            for (var i = 0; i < $scope.carts.length; i++){
                // 获取数组元素 Cart对象
                var cart = $scope.carts[i];

                // 统计买的商品种类
                $scope.totalEntity.totalItem += cart.orderItems.length;
            }
        });
    };

    // 添加商品到购物车(加减、删除)
    $scope.addCart = function (itemId, num) {
        baseService.sendGet("/cart/addCart?itemId="
            + itemId + "&num=" + num).then(function(response){
            if (response.data){
                // 加入购物车成功，重新查询购物车
                $scope.findCart();
            }else {
                alert("操作失败！");
            }
        });
    };

    $scope.selectedCarts = [{sellerId:"",orderItems:[{itemId:0}]}];
    $scope.cartsSelected = false;
    // 更新选中状态
    $scope.updateSelection=function ($event,sellerId,itemId) {
        if($event.target.checked == true){
            $scope.addToSelectedCarts(sellerId,itemId);
        }else {
            $scope.deleteFromSelectedCarts(sellerId,itemId);
        }

        // ng-checked 后的方法会在点击之后自动执行
        // if(sellerId && itemId){
        //     $scope.isItemChecked(sellerId,itemId);
        // }
        // else if(sellerId && !itemId){
        //     $scope.isSellerChecked(sellerId);
        // }else {
        //     $scope.isCartsChecked();
        // }
        // $scope.isCartsChecked();
        $scope.totalEntity = {totalItem : 0, totalNum : 0, totalMoney : 0};
        // 购买总数与总金额
        for (var i = 0; i < $scope.carts.length; i++){
            // 获取数组元素 Cart对象
            var cart = $scope.carts[i];

            // 统计买的商品种类
            $scope.totalEntity.totalItem += cart.orderItems.length;
            var selectedCart = isSellerSelected($scope.selectedCarts,cart.sellerId);
            if(selectedCart){
                for (var j = 0; j < cart.orderItems.length; j++){
                    // 获取数组元素
                    var orderItem = cart.orderItems[j];
                    if(isOrderItemSelected(selectedCart,orderItem.itemId)){
                        // 统计购买的总件数
                        $scope.totalEntity.totalNum += orderItem.num;
                        // 统计购买的总金额
                        $scope.totalEntity.totalMoney += orderItem.totalFee;
                    }else {
                        continue;
                    }

                }
            }else {
                continue;
            }
        }
        $scope.isCartsChecked();

    }

    $scope.isCartsChecked=function () {
        if(!$scope.carts){
            $scope.cartsSelected = false;
        }
        $scope.cartsSelected = false;
        // $scope.selectedCarts比$scope.carts多一个无用的首位元素
        if($scope.selectedCarts.length-1!=$scope.carts.length){
            $scope.cartsSelected = false;
        }else {
            for(var i = 0; i < $scope.carts.length ; i++) {
                if (!$scope.isSellerChecked($scope.selectedCarts[i+1].sellerId)){
                    $scope.cartsSelected = false;
                    return;
                }
            }
            $scope.cartsSelected = true;
        }

    }

    $scope.isItemChecked=function (sellerId,itemId) {
        var isChecked = false;
        if(!itemId){
            return false;
        }
        var cart = isSellerSelected($scope.selectedCarts,sellerId);
        if(cart){
            if(isOrderItemSelected(cart,itemId)){
                isChecked = true;
            }else {
                isChecked = false;
            }
        }else {
            isChecked = false;
        }

        return isChecked;
    }

    $scope.isSellerChecked=function (sellerId) {
        var isChecked = false;
        var cart = isSellerSelected($scope.selectedCarts,sellerId);
        var pageCart = isSellerSelected($scope.carts,sellerId);
        if(cart){
            if(cart.orderItems.length == pageCart.orderItems.length){
                isChecked = true;
            }
        }else {
            isChecked = false;
        }
        return isChecked;
    }



    $scope.addToSelectedCarts=function (sellerId,itemId) {
        // 根据传入参数的不同判断checkbox的位置, 以此来操作
        if(sellerId && itemId){
            var cart = isSellerSelected($scope.selectedCarts,sellerId);
            if(cart){
                cart.orderItems.push({itemId:itemId})
            }else {
                $scope.selectedCarts.push({sellerId:sellerId,orderItems:[{itemId:itemId}]});
            }
        }else if(sellerId && !itemId) {
            var cart = isSellerSelected($scope.carts,sellerId);

            // for(var i = 0; i < cart.orderItems.length ; i++) {
            //     cart.orderItems[i] = {id:cart.orderItems[i].id}
            // }
            $scope.selectedCarts.push(JSON.parse(JSON.stringify(cart)));
        }else {
            // 解除selectedCarts中的对象与cart中对象的引用关系
            $scope.selectedCarts = JSON.parse(JSON.stringify($scope.carts));
            // 为了后面的逻辑判断, 索引0处加入一个无用元素
            $scope.selectedCarts.splice(0,0,{sellerId:"",orderItems:[{itemId:0}]});
        }
    }

    $scope.deleteFromSelectedCarts=function (sellerId,itemId) {
        if(sellerId && itemId){
            var cart = isSellerSelected($scope.selectedCarts,sellerId);
            var orderItem = isOrderItemSelected(cart,itemId);
            var index = cart.orderItems.indexOf(orderItem);
            cart.orderItems.splice(index,1)
            // 若选中的商品为0,删除该商家
            if(cart.orderItems.length == 0){
                var index = $scope.selectedCarts.indexOf(cart);
                $scope.selectedCarts.splice(index,1);
            }
        }else if(sellerId && !itemId){
            var cart = isSellerSelected($scope.selectedCarts,sellerId);
            var index = $scope.selectedCarts.indexOf(cart);
            $scope.selectedCarts.splice(index,1);

        }else {
            $scope.selectedCarts = [{sellerId:"",orderItems:[{itemId:0}]}];
        }
    }

    var isSellerSelected=function (carts,sellerId) {
        for(var i = 0; i < carts.length ; i++) {
            // selecteCarts中是否存在该商家
            if (sellerId == carts[i].sellerId){
                return carts[i];
            }
        }
        return null;
    }

    var isOrderItemSelected=function (cart,itemId) {
        for(var i = 0; i < cart.orderItems.length ; i++) {
            if (itemId == cart.orderItems[i].itemId){
                return cart.orderItems[i];
            }
        }
        return null;
    }

    // 提交选中的商品
    $scope.addToSelecteCart=function () {
        // 去除无用数据
        $scope.selectedCarts.splice(0,1);
        if(!$scope.loginName){
            location.href="http://sso.pinyougou.com/login?service="+$scope.redirectUrl;
        } else if($scope.selectedCarts.length == 0){
            alert("请选择商品");
            $scope.selectedCarts = [{sellerId:"",orderItems:[{itemId:0}]}];
        }else {
            baseService.sendPost("/cart/addToSelecteCart",$scope.selectedCarts).then(function (response) {
                if(response.data){
                    location.href="/order/getOrderInfo.html"
                }else {
                    alert("操作失败")
                }
            })
        }

    }

    // 从selectedCarts中查询数据
    $scope.findSelectedCart=function () {
        baseService.sendGet("/cart/findSelectedCart").then(function (response) {
            $scope.carts = response.data;
            $scope.totalEntity = {totalItem: 0, totalNum: 0, totalMoney: 0};
            // 统计买的商品种类
            for(var i = 0; i <$scope.carts.length ; i++) {
                var cart = $scope.carts[i];
                $scope.totalEntity.totalItem += cart.orderItems.length;
                for (var j = 0; j < cart.orderItems.length; j++) {
                    // 获取数组元素
                    var orderItem = cart.orderItems[j];
                    // 统计购买的总件数
                    $scope.totalEntity.totalNum += orderItem.num;
                    // 统计购买的总金额
                    $scope.totalEntity.totalMoney += orderItem.totalFee;
                }
            }
        })
    }

    $scope.deleteFromCart = function () {
        // 去除无用数据
        $scope.selectedCarts.splice(0,1);
        if($scope.selectedCarts.length == 0){
            alert("请选择商品");
            $scope.selectedCarts = [{sellerId:"",orderItems:[{itemId:0}]}];
        }else {
            baseService.sendPost("/cart/deleteFromCart",$scope.selectedCarts).then(function (response) {
                if(response.data){
                    $scope.findCart();
                    $scope.selectedCarts = [{sellerId:"",orderItems:[{itemId:0}]}];
                }else {
                    alert("操作失败")
                }
            })
        }
    }

});