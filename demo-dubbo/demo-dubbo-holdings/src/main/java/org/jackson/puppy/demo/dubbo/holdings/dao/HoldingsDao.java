package org.jackson.puppy.demo.dubbo.holdings.dao;

import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.holdings.po.Holdings;

import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface HoldingsDao {

	@Results(
			id = "holdings",
			value = {
					@Result(property = "id", column = "id"),
					@Result(property = "accountNumber", column = "account_number"),
					@Result(property = "unit", column = "unit"),
					@Result(property = "freezeUnit", column = "freeze_unit"),
					@Result(property = "version", column = "version")
			})
	@Select("select id, account_number, unit, freeze_unit, version " +
			"from holdings where account_number = #{accountNumber}")
	Holdings getByAccountNumber(@Param("accountNumber") String accountNumber);

	@Update("update holdings set freeze_unit = #{freezeUnit}, version = #{version} + 1 " +
			"where account_number = #{accountNumber} " +
			"and version = #{version}")
	Integer updateFreezeUnitByAccountNumberAndVersion(@Param("freezeUnit") BigDecimal freezeUnit,
	                                                  @Param("accountNumber") String accountNumber,
	                                                  @Param("version") Integer version);

	@Update("update holdings set unit = #{unit}, freeze_unit = #{freezeUnit}, version = #{version} + 1 " +
			"where account_number = #{accountNumber} " +
			"and version = #{version}")
	Integer updateUnitAndFreezeUnitByAccountNumberAndVersion(@Param("unit") BigDecimal unit,
	                                                         @Param("freezeUnit") BigDecimal freezeUnit,
	                                                         @Param("accountNumber") String accountNumber,
	                                                         @Param("version") Integer version);

	@Update("update holdings set unit = #{unit}, freeze_unit = #{freezeUnit}, version = #{version} " +
			"where account_number = #{accountNumber}")
	Integer updateUnitAndFreezeUnitAndVersionByAccountNumber(@Param("unit") BigDecimal unit,
	                                                         @Param("freezeUnit") BigDecimal freezeUnit,
	                                                         @Param("version") Integer version,
	                                                         @Param("accountNumber") String accountNumber);
}
