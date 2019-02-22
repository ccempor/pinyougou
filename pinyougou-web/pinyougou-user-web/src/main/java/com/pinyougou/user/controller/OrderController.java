package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.PayLog;
import com.pinyougou.service.OrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-02-15<p>
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(timeout = 10000)
    private OrderService orderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService;
    @Autowired
    private HttpServletRequest request;

    /** 查询订单 */
    @GetMapping("/searchOrder")
    public Map<String,Object> findByPage(Integer page, Integer rows){
        String userId = request.getRemoteUser();
        PageResult pageResult = orderService.findByPage(page,rows,userId);
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("rows",pageResult.getRows());
        Long row = pageResult.getTotal() % rows == 0 ? (pageResult.getTotal()/rows) : (pageResult.getTotal()/rows)+1;
        resMap.put("total",row);
        resMap.put("count",pageResult.getTotal());
        return resMap;
    }

    @GetMapping("/savePaylog")
    public boolean savePaylog(Long orderId){
        try{
            orderService.savePayLog(orderId);
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /** 生成支付二维码 */
    @GetMapping("/genPayCode")
    public Map<String, Object> genPayCode(HttpServletRequest request){
        // 获取登录用户名
        String userId = request.getRemoteUser();
        // 调用订单服务接口，从Redis数据库中查询支付日志
        PayLog payLog = orderService.findPayLogFromRedis(userId);
        // 调用微信服务接口，生成支付二维码
        return weixinPayService.genPayCode(payLog.getOutTradeNo(),
                String.valueOf(payLog.getTotalFee()));
    }

    /** 查询支付状态 */
    @GetMapping("/queryPayStatus")
    public Map<String, Integer> queryPayStatus(String outTradeNo){
        Map<String, Integer> data = new HashMap<>();
        data.put("status", 3);
        try{
            // 调用微信支付服务接口
            Map<String,String> resMap = weixinPayService.queryPayStatus(outTradeNo);
            if ("SUCCESS".equals(resMap.get("trade_state"))){ // 支付成功

                // 支付成功，业务处理
                // 修改支付日志、修改订单状态、从Redis中删除支付日志。
                orderService.updateOrderStatus(outTradeNo, resMap.get("transaction_id"));

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
