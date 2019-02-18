package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-18<p>
 */
@RestController
@RequestMapping("/order")
public class SeckillOrderController {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /** 秒杀下单 */
    @GetMapping("/submitOrder")
    public boolean submitOrder(Long id, HttpServletRequest request){
        try{
            // 获取登录用户名
            String userId = request.getRemoteUser();
            // 提交订单到Redis数据库
            return seckillOrderService.submitOrderRedis(userId, id);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /** 生成支付二维码 */
    @GetMapping("/genPayCode")
    public Map<String, Object> genPayCode(HttpServletRequest request){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 调用订单服务接口，从Redis数据库中查询预订单
        SeckillOrder seckillOrder = seckillOrderService.findSeckillOrderFromRedis(userId);

        // 把金额转化成分
        long totalFee = (long)(seckillOrder.getMoney().doubleValue() * 100);

        // 调用微信服务接口，生成支付二维码
        return weixinPayService.genPayCode(String.valueOf(seckillOrder.getId()),
                String.valueOf(totalFee));
    }

    /** 查询支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo, HttpServletRequest request){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            // 调用微信支付服务接口
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if ("SUCCESS".equals(resMap.get("trade_state"))){ // 支付成功


                // 获取登录用户名
                String userId = request.getRemoteUser();
                // 支付成功，业务处理
                // 把Redis中的秒杀订单同步到mysql数据库，删除Redis中的秒杀订单
                seckillOrderService.saveOrder(userId, resMap.get("transaction_id"));

                data.put("status", 1);
            }
            if ("NOTPAY".equals(resMap.get("trade_state"))){ // 未支付
                data.put("status", 2);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return data;
    }
}
