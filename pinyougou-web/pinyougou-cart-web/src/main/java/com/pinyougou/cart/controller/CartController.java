package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 购物车控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-13<p>
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 10000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /** 把SKU商品加入购物车 */
    @GetMapping("/addCart")
    @CrossOrigin(origins = {"http://item.pinyougou.com"},
            allowCredentials = "true") // 跨域注解
    public boolean addCart(Long itemId, Integer num){
        // 设置允许跨域访问的域名
        //response.setHeader("Access-Control-Allow-Origin", "http://item.pinyougou.com");
        // 设置允许跨域访问Cookie
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        try{
            // 获取登录用户名
            String userId = request.getRemoteUser();

            // 1. 获取原来的购物车集合
            List<Cart> carts = findCart();

            // 2. 把新购买的商品加入原来的购物车集合中
            carts = cartService.addItemToCart(carts, itemId, num);

            if (StringUtils.isNoneBlank(userId)){ // 已登录
                /** ############ 已登录的用户，购物车数据存储到Redis中 ############ */
                cartService.saveCartRedis(userId, carts);
            }else{ // 未登录
                /** ############ 未登录的用户，购物车数据存储到Cookie中 ############ */
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts),
                        60 * 60 * 24, true);
            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 查询购物车 */
    @GetMapping("/findCart")
    public List<Cart> findCart(){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 定义购物车集合
        List<Cart> carts = null;

        if (StringUtils.isNoneBlank(userId)){ // 已登录
            /** ######### 已登录的用户，从Redis中获取购物车 ########### */
            carts = cartService.findCartRedis(userId);

            /** ########## 把Cookie中的购物车数据合并到Redis数据库,删除Cookie中的购物车数据 ########### */
            // 获取Cookie中的购物车数据
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            if (StringUtils.isNoneBlank(cartJsonStr)){
                // 把Cookie中的购物车json字符串转化成List集合
                List<Cart> cookieCarts = JSON.parseArray(cartJsonStr, Cart.class);
                if (cookieCarts.size() > 0){
                    // 合并购物车，得到合并后的购物车集合
                    carts = cartService.mergeCart(cookieCarts, carts);
                    // 重新保存到Redis数据库
                    cartService.saveCartRedis(userId, carts);

                    // 删除Cookie中购物车
                    CookieUtils.deleteCookie(request,response,
                            CookieUtils.CookieName.PINYOUGOU_CART);
                }
            }

        }else{ // 未登录
            /** ######### 未登录的用户，从Cookie中获取购物车 ########### */
            // 1. 从Cookie获取购物车json字符串 [{},{}] List<Cart>
            String cartJsonStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            // 判断购物车数据是否为空
            if (StringUtils.isBlank(cartJsonStr)){
                // 创建新的购物车
                cartJsonStr = "[]";
            }
            carts = JSON.parseArray(cartJsonStr, Cart.class);
        }
        return carts;
    }

    @PostMapping("addToSelecteCart")
    public boolean addToSelecteCart(@RequestBody List<Cart> carts){
        try{
            cartService.addToSelecteCart(carts,request.getRemoteUser());
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("findSelectedCart")
    public List<Cart> findSelectedCart(){
        String userId = request.getRemoteUser();
        return cartService.findSelectedCartRedis(userId);
    }

    @PostMapping("deleteFromCart")
    public boolean deleteFromCart(@RequestBody List<Cart> carts){
        try{
            List<Cart> totalCarts = findCart();
            List<Cart> resultCarts = cartService.deleteFromCart(carts,totalCarts);
            String userId = request.getRemoteUser();
            if (StringUtils.isNoneBlank(userId)){
                // 登录用户将商品放入redis中
                cartService.saveCartRedis(userId,resultCarts);

            }else {
                // 未登录用户将商品放入cookie
                /** ############ 未登录的用户，购物车数据存储到Cookie中 ############ */
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(resultCarts),
                        60 * 60 * 24, true);
            }
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
