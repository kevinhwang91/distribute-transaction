package org.jackson.puppy.demo.dubbo.confirm.dao;


import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.confirm.po.Confirm;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface ConfirmDao {

	@Insert("insert into confirm (order_number, agency_fee, unit) " +
			"values( " +
			"#{orderNumber}, " +
			"#{agencyFee}, " +
			"#{unit}) ")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	Integer insert(Confirm confirm);

	@Delete("delete from confirm where order_number = #{orderNumber} ")
	Integer deleteByOrderNumber(String orderNumber);

	@Select("select count(order_number) from confirm where order_number = #{orderNumber}")
	Integer countByOrderNumber(String orderNumber);
}
