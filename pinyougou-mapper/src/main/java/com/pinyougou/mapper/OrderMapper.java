package com.pinyougou.mapper;

import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.Order;

import java.util.List;

/**
 * OrderMapper 数据访问接口
 * @date 2019-01-11 09:53:21
 * @version 1.0
 */
public interface OrderMapper extends Mapper<Order>{

    Long findTotalCount();

    List<Order> findByPage(@Param("userId") String userId,@Param("curPage") Integer page,@Param("rows") Integer rows);
}