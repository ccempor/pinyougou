package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List; /**
 * 购物车服务接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-13<p>
 */
public interface CartService {

    /**
     * 把SKU商品添加到购物车集合
     * @param carts 购物车集合
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后的购物车集合
     */
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 把购物车存储到Redis数据库
     * @param userId 用户id
     * @param carts 购物车数据
     */
    void saveCartRedis(String userId, List<Cart> carts);

    /**
     * 根据用户id从Redis数据库获取购物车
     * @param userId 用户id
     * @return 购物车数据
     */
    List<Cart> findCartRedis(String userId);

    /**
     * 合并购物车
     * @param cookieCarts Cookie中购物车数据
     * @param redisCarts Redis中购物车数据
     * @return 合并后得购物车集合
     */
    List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts);

    void addToSelecteCart(List<Cart> carts, String userId);

    List<Cart> findSelectedCartRedis(String userId);

    List<Cart> deleteFromCart(List<Cart> selectedCarts, List<Cart> totalCarts);
}
