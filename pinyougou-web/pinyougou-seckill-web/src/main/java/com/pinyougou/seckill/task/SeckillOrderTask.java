package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单任务调度类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-19<p>
 */
@Component
public class SeckillOrderTask {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;

    /**
     * 定时调度的方法(关闭超时5分钟未支付的订单)
     * cron : 调度的时间表达式
     * 秒 分 小时  日  月 周(间隔3秒)
     * */
    @Scheduled(cron = "0/3 * * * * ?")
    public void closeOrderTask(){
        System.out.println("当前系统时间：" + new Date());

        // 1. 查询超时5分钟未支付的订单
        List<SeckillOrder> seckillOrders = seckillOrderService.findOrderByTimeout();
        System.out.println("超时未支付的订单数量：" + seckillOrders.size());

        // 2.调用微信支付系统的"关闭订单接口"
        for (SeckillOrder seckillOrder : seckillOrders) {

            // 调用微信支付服务接口中的方法关闭订单(调用微信支付系统的"关闭订单接口")
            Map<String,String> resMap = weixinPayService.
                    closePayTimeout(seckillOrder.getId().toString());
            // 判断关单是否成功
            if ("SUCCESS".equals(resMap.get("return_code"))){ // 关单成功

                // 3. 关单成功后，从Redis数据库中删除秒杀订单、恢复秒杀商品的库存
                seckillOrderService.deleteOrderFromRedis(seckillOrder);
            }
        }
    }
}
