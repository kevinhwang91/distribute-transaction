package org.jackson.puppy.demo.dubbo.confirm.dao;


import org.apache.ibatis.annotations.*;
import org.jackson.puppy.demo.dubbo.confirm.po.RetryMqMessage;

import java.util.Date;
import java.util.List;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Mapper
public interface RetryMqMessageDao {

	@Results(
			id = "retryMqMessage",
			value = {
					@Result(column = "id", property = "id"),
					@Result(column = "message", property = "message"),
					@Result(column = "status", property = "status"),
					@Result(column = "created_date", property = "createdDate"),
					@Result(column = "retry", property = "retry")
			})
	@Select("select id,  message, status, created_date, retry " +
			"from retry_mq_message where status = #{status} " +
			"and created_date < #{createdDate}")
	List<RetryMqMessage> findByStatusAndMoreThanCreatedDate(@Param("status") Integer status,
	                                                        @Param("createdDate") Date createdDate);

	@Insert("insert into retry_mq_message (id, message, status, created_date, retry) " +
			"values( " +
			"#{id}, " +
			"#{message}, " +
			"#{status}, " +
			"#{createdDate}, " +
			"#{retry})")
	Integer insert(RetryMqMessage retryMqMessage);

	@Update("update retry_mq_message set status = #{status} where id = #{id} ")
	Integer updateStatusById(@Param("status") Integer status, @Param("id") String id);

	@Update("update retry_mq_message set retry = #{retry} where id = #{id} ")
	Integer updateRetryById(@Param("retry") Integer retry, @Param("id") String id);

}
