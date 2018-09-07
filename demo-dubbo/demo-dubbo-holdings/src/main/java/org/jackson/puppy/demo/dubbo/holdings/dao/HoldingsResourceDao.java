package org.jackson.puppy.demo.dubbo.holdings.dao;

import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.holdings.po.HoldingsResource;

import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface HoldingsResourceDao {

	@Results(
			id = "holdings",
			value = {
					@Result(property = "id", column = "id"),
					@Result(property = "orderNumber", column = "order_number"),
					@Result(property = "status", column = "status"),
					@Result(property = "createdDate", column = "created_date")
			})
	@Select("select id, order_number, status, created_date " +
			"from holdings_resource where order_number = #{orderNumber}")
	HoldingsResource getByOrderNumber(String orderNumber);

	@Insert("insert into holdings_resource(order_number, status, created_date) " +
			"values(" +
			"#{orderNumber}, " +
			"#{status}, " +
			"#{createdDate} )")
	Integer insert(HoldingsResource holdingsResource);

	@Update("update holdings_resource set status = #{nStatus} " +
			"where order_number = #{orderNumber} " +
			"and status = #{oStatus} " +
			"and created_date = #{createdDate} ")
	Integer updateStatusByOrderNumberAndStatusAndCreatedDate(@Param("nStatus") Integer nStatus,
	                                                         @Param("orderNumber") String orderNumber,
	                                                         @Param("oStatus") Integer status,
	                                                         @Param("createdDate") Date createdDate);

	@Delete("delete from holdings_resource " +
			"where order_number = #{orderNumber} " +
			"and status = #{status} " +
			"and created_date = #{createdDate} ")
	Integer deleteByOrderNumberAndStatusAndCreatedDate(@Param("orderNumber") String orderNumber,
	                                                   @Param("status") Integer status,
	                                                   @Param("createdDate") Date createdDate);

	@Delete("delete from holdings_resource where order_number = #{orderNumber}")
	Integer deleteByOrderNumber(String orderNumber);

}
