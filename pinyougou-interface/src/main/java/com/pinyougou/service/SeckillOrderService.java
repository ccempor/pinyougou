package com.pinyougou.service;

import com.pinyougou.pojo.SeckillOrder;
import java.util.List;
import java.io.Serializable;
/**
 * SeckillOrderService 服务接口
 * @date 2019-01-11 09:57:49
 * @version 1.0
 */
public interface SeckillOrderService {

	/** 添加方法 */
	void save(SeckillOrder seckillOrder);

	/** 修改方法 */
	void update(SeckillOrder seckillOrder);

	/** 根据主键id删除 */
	void delete(Serializable id);

	/** 批量删除 */
	void deleteAll(Serializable[] ids);

	/** 根据主键id查询 */
	SeckillOrder findOne(Serializable id);

	/** 查询全部 */
	List<SeckillOrder> findAll();

	/** 多条件分页查询 */
	List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows);

	/** 提交订单到Redis数据库 */
    boolean submitOrderRedis(String userId, Long id);

    /** 查询秒杀查询预订单 */
	SeckillOrder findSeckillOrderFromRedis(String userId);

	/** 支付成功,订单存储到mysql数据库 */
	void saveOrder(String userId, String transactionId);

	/** 查询超时5分钟未支付的订单 */
	List<SeckillOrder> findOrderByTimeout();

	/** 删除Redis中的秒杀订单 */
	void deleteOrderFromRedis(SeckillOrder seckillOrder);
}