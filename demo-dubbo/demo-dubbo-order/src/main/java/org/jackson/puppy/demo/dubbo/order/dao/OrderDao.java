package org.jackson.puppy.demo.dubbo.order.dao;

import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.order.pojo.Order;

import java.util.Date;


/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface OrderDao {

	@Results(
			id = "order",
			value = {
					@Result(column = "id", property = "id"),
					@Result(column = "order_number", property = "orderNumber"),
					@Result(column = "account_number", property = "accountNumber"),
					@Result(column = "status", property = "status"),
					@Result(column = "updated_date", property = "updatedDate")
			})
	@Select("select id,  order_number, account_number, status,  updated_date " +
			"from `order` where order_number = #{orderNumber}")
	Order getByOrderNumber(String orderNumber);

	@Update("update `order` set status = #{nStatus}, updated_date = #{updatedDate} " +
			"where status = #{oStatus} " +
			"and order_number = #{orderNumber} ")
	Integer updateStatusByStatusAndOrderNumber(@Param("nStatus") Integer nStatus,
	                                           @Param("oStatus") Integer oStatus,
	                                           @Param("orderNumber") String orderNumber,
	                                           @Param("updatedDate") Date updatedDate);

	@Update("update `order` set status = #{nStatus} " +
			"where status = #{oStatus} " +
			"and order_number = #{orderNumber} " +
			"and updated_date = #{updatedDate} ")
	Integer updateStatusByStatusAndOrderNumberAndUpdatedDate(@Param("nStatus") Integer nStatus,
	                                                         @Param("oStatus") Integer oStatus,
	                                                         @Param("orderNumber") String orderNumber,
	                                                         @Param("updatedDate") Date updatedDate);

	@Update("update `order` set status = #{status} where order_number = #{orderNumber} ")
	Integer updateStatusByOrderNumber(@Param("status") Integer status, @Param("orderNumber") String orderNumber);

}
