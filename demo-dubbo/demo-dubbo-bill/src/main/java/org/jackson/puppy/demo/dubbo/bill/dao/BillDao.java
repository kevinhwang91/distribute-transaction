package org.jackson.puppy.demo.dubbo.bill.dao;

import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.bill.po.Bill;

import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface BillDao {

	@Results(
			id = "bill",
			value = {
					@Result(property = "id", column = "id"),
					@Result(property = "orderNumber", column = "order_number"),
					@Result(property = "agencyFee", column = "agency_fee"),
					@Result(property = "status", column = "status"),
					@Result(property = "createdDate", column = "created_date")
			})
	@Select("select id, order_number, agency_fee, status, created_date " +
			"from bill where order_number = #{orderNumber}")
	Bill getByOrderNumber(String orderNumber);


	@Insert("insert into bill (order_number, agency_fee, status, created_date) " +
			"values( " +
			"#{orderNumber}, " +
			"#{agencyFee}, " +
			"#{status}, " +
			"#{createdDate}) ")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	Integer insert(Bill bill);

	@Update("update bill set status = #{nStatus} " +
			"where status = #{oStatus} " +
			"and order_number = #{orderNumber} " +
			"and created_date = #{createdDate} ")
	Integer updateStatusByStatusAndOrderNumberAndCreatedDate(@Param("nStatus") Integer nStatus,
	                                                         @Param("oStatus") Integer oStatus,
	                                                         @Param("orderNumber") String orderNumber,
	                                                         @Param("createdDate") Date createdDate);

	@Delete("delete from bill " +
			"where status = #{status} " +
			"and order_number = #{orderNumber} " +
			"and created_date = #{createdDate} ")
	Integer deleteByStatusAndOrderNumberAndCreatedDate(@Param("status") Integer status,
	                                                   @Param("orderNumber") String orderNumber,
	                                                   @Param("createdDate") Date createdDate);

	@Delete("delete from bill where order_number = #{orderNumber} ")
	Integer deleteByOrderNumber(String orderNumber);
}
