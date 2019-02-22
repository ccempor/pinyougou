package com.pinyougou.cart.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-13<p>
 */
@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 把SKU商品添加到购物车集合
     * @param carts 购物车集合
     * @param itemId SKU商品的id
     * @param num 购买数量
     * @return 修改后的购物车集合
     */
    public List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num){
        try{

            // 1. 获取商家id
            // 1.1 根据itemId从tb_item表查询一条数据
            Item item = itemMapper.selectByPrimaryKey(itemId);
            String sellerId = item.getSellerId();

            // 2. 根据商家id 从用户的购物车集合中查询 对应的商家购物车
            Cart cart = searchCartBySellerId(carts, sellerId);

            // 3. 判断商家的购物车
            if (cart == null){ // 代表该用户没有买过该商家的商品
                // 创建该商家的购物车
                cart = new Cart();
                // 设置商家的id
                cart.setSellerId(sellerId);
                // 设置店铺名称
                cart.setSellerName(item.getSeller());
                // 创建商家购物车商品集合
                List<OrderItem> orderItems = new ArrayList<>();
                // 创建购物车的商品 Item --> OrderItem
                OrderItem orderItem = createOrderItem(item, num);

                // 添加商品到商家购物车
                orderItems.add(orderItem);
                // 设置购物车商品列表
                cart.setOrderItems(orderItems);
                // 用户的购物车集合添加该商家的购物车
                carts.add(cart);

            }else{ // 代表该用户购买过该商家的商品
                // 获取商家的购物车商品集合
                List<OrderItem> orderItems = cart.getOrderItems();
                // 从商家的购物车商品集合中搜索是否买过该商品
                OrderItem orderItem = searchOrderItemByItemId(orderItems, itemId);

                // 判断是否买过同样的商品
                if (orderItem == null){ // 没有买过同样的商品
                    orderItem = createOrderItem(item, num);
                    orderItems.add(orderItem);
                }else { // 买过同样的商品
                    // 购买数量相加
                    orderItem.setNum(orderItem.getNum() + num);
                    // 重新计算金额
                    orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));

                    // 判断商品购买数量是否等于0
                    if (orderItem.getNum() == 0){
                        // 从商家购物车商品集合中删除该商品
                        orderItems.remove(orderItem);
                    }
                    // 判断商家购物车商品集合中是否还有商品
                    if (orderItems.size() == 0){
                        // 从用户的购物车集合中删除商家的购物车
                        carts.remove(cart);
                    }
                }
            }

            return carts;

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据SKU商品的id从商家的购物车商品集合中查询一个商品 */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItemId().equals(itemId)){
                return orderItem;
            }
        }
        return null;
    }

    /** 创建购物车中商品 */
    private OrderItem createOrderItem(Item item, Integer num) {
        OrderItem orderItem = new OrderItem();
        // 设置SKU商品id
        orderItem.setItemId(item.getId());
        // 设置SPU的id
        orderItem.setGoodsId(item.getGoodsId());
        // 设置商品标题
        orderItem.setTitle(item.getTitle());
        // 设置商品价格
        orderItem.setPrice(item.getPrice());
        // 设置购买数量
        orderItem.setNum(num);
        // 设置小计金额
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        // 设置商品图片
        orderItem.setPicPath(item.getImage());
        // 设置商家id
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /** 根据商家id 从用户的购物车集合中查询 对应的商家购物车 */
    private Cart searchCartBySellerId(List<Cart> carts, String sellerId) {
        for (Cart cart : carts) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 把购物车存储到Redis数据库
     * @param userId 用户id
     * @param carts 购物车数据
     */
    public void saveCartRedis(String userId, List<Cart> carts){
        redisTemplate.boundValueOps("cart_" + userId).set(carts);
    }

    /**
     * 根据用户id从Redis数据库获取购物车
     * @param userId 用户id
     * @return 购物车数据
     */
    public List<Cart> findCartRedis(String userId){
        List<Cart> carts = (List<Cart>)redisTemplate.boundValueOps("cart_" + userId).get();
        if (carts == null){
            carts = new ArrayList<>();
        }
        return carts;
    }

    /**
     * 合并购物车
     * @param cookieCarts Cookie中购物车数据
     * @param redisCarts Redis中购物车数据
     * @return 合并后得购物车集合
     */
    public List<Cart> mergeCart(List<Cart> cookieCarts, List<Cart> redisCarts){
        // 把Cookie中的购物车数据合并到Redis
        for (Cart cookieCart : cookieCarts) {
            // 迭代商家中的商品
            for (OrderItem orderItem : cookieCart.getOrderItems()) {
                redisCarts = addItemToCart(redisCarts, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return redisCarts;
    }

    // 将选中的商品加入选中购物车
    @Override
    public void addToSelecteCart(List<Cart> carts, String userId) {
        List<Cart> totalCart = (List<Cart>) redisTemplate.boundValueOps("cart_" + userId).get();
        List<Cart> selectedCarts = new ArrayList<>();
        for (Cart cart : carts) {
            // 选中的商品加入新购物车, 并将商品从总购物车删除
            Cart cart1 = searchCartBySellerId(totalCart, cart.getSellerId());

            Cart selectedCart = new Cart();
            selectedCart.setSellerId(cart1.getSellerId());
            selectedCart.setSellerName(cart1.getSellerName());

            List<OrderItem> orderItems = cart1.getOrderItems();
            List<OrderItem> selectedOrderItems = new ArrayList<>();
            // 判断商品是否选中
            for (OrderItem orderItem : cart.getOrderItems()) {
                OrderItem orderItem1 = searchOrderItemByItemId(orderItems, orderItem.getItemId());
                orderItems.remove(orderItem1);
                selectedOrderItems.add(orderItem1);
                // 一个商家的商品全被选中, 将商家从总购物车删除
                if (orderItems.size()==0){
                    totalCart.remove(cart1);
                }
            }
            selectedCart.setOrderItems(selectedOrderItems);
            selectedCarts.add(selectedCart);
        }
        redisTemplate.boundValueOps("cart_"+userId).set(totalCart);
        redisTemplate.boundValueOps("selectedCart_"+userId).set(selectedCarts);
    }

    @Override
    public List<Cart> findSelectedCartRedis(String userId) {
        return (List<Cart>) redisTemplate.boundValueOps("selectedCart_"+userId).get();
    }

    @Override
    public List<Cart> deleteFromCart(List<Cart> selectedCarts, List<Cart> totalCarts) {
        for (Cart cart : selectedCarts) {
            // 选中的商品从总购物车删除
            Cart cart1 = searchCartBySellerId(totalCarts, cart.getSellerId());
            List<OrderItem> orderItems = cart1.getOrderItems();
            // 判断商品是否选中
            for (OrderItem orderItem : cart.getOrderItems()) {
                OrderItem orderItem1 = searchOrderItemByItemId(orderItems, orderItem.getItemId());
                orderItems.remove(orderItem1);
                // 一个商家的商品全被选中, 将商家从总购物车删除
                if (orderItems.size()==0){
                    totalCarts.remove(cart1);
                }
            }
        }
        return totalCarts;
    }

}
