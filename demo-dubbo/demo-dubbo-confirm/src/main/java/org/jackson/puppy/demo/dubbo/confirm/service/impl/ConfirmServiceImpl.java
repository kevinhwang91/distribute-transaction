package org.jackson.puppy.demo.dubbo.confirm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jackson.puppy.demo.dubbo.confirm.api.constant.ConfirmQueue;
import org.jackson.puppy.demo.dubbo.confirm.dao.ConfirmDao;
import org.jackson.puppy.demo.dubbo.confirm.dao.RetryMqMessageDao;
import org.jackson.puppy.demo.dubbo.confirm.enums.RetryMqMessageStatus;
import org.jackson.puppy.demo.dubbo.confirm.po.Confirm;
import org.jackson.puppy.demo.dubbo.confirm.po.RetryMqMessage;
import org.jackson.puppy.demo.dubbo.confirm.util.KryoSerializer;
import org.jackson.puppy.demo.dubbo.order.api.dto.OrderDto;
import org.jackson.puppy.rabbitmq.common.dto.CallBackContext;
import org.jackson.puppy.rabbitmq.common.dto.MqMessage;
import org.jackson.puppy.rabbitmq.common.queue.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Service
public class ConfirmServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Producer producer;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ConfirmDao confirmDao;

	@Autowired
	private RetryMqMessageDao retryMqMessageDao;

	@Transactional
	public void sendConfirm(String orderNumber, BigDecimal agencyFee, BigDecimal unit) {
		if (isConfirmExisted(orderNumber)) {
			logger.info("Order Number exist in confirm.");
			return;
		}

		OrderDto orderDto = buildOrderDto(orderNumber, agencyFee, unit);

		byte[] msg = KryoSerializer.serializer(orderDto);
		MqMessage mqMessage = new MqMessage(msg, ConfirmQueue.CONFIRM_EXCHANGE, ConfirmQueue.CONFIRM_ROUTE_KEY);

		String uuid = UUID.randomUUID().toString();
		CallBackContext callBackContext = buildContext(uuid);

		mqMessage.setCache(callBackContext);
		saveConfirm(orderNumber, agencyFee, unit, new Date());
		saveRetryMqMessage(uuid, mqMessage);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCommit() {
				producer.send(mqMessage);
			}
		});
	}


	@Transactional
	public void retrySendConfirm() {
		Date date = new Date(Calendar.getInstance().getTimeInMillis() - 30 * 1000);

		List<RetryMqMessage> retryMqMessages = retryMqMessageDao.findByStatusAndMoreThanCreatedDate(RetryMqMessageStatus.SENDING.getStatus(), date);

		retryMqMessages.forEach(retryMqMessage -> {

			String uuid = retryMqMessage.getId();
			Integer retry = retryMqMessage.getRetry();

			logger.info("Retrying message uuid: {}, retry: {}", uuid, retry);

			if (retry < 10) {
				retryMqMessageDao.updateRetryById(retry + 1, uuid);

				byte[] serializableMsg = retryMqMessage.getMessage();
				MqMessage mqMessage = KryoSerializer.deSerializer(serializableMsg, MqMessage.class);

				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

					@Override
					public void afterCommit() {
						producer.send(mqMessage);
					}
				});
			} else {
				retryMqMessageDao.updateStatusById(RetryMqMessageStatus.FAILED.getStatus(), uuid);
				logger.error("Please manual check the id of retry mq message: {}.", uuid);
			}
		});

	}

	private CallBackContext buildContext(String callBackArg) {
		try {
			Method method = getClass().getMethod("askConfirm", String.class);
			return new CallBackContext(getClass(), method.getName(), method.getParameterTypes(), new String[]{callBackArg});
		} catch (NoSuchMethodException e) {
			logger.warn(e.toString());
		}
		return null;
	}

	@Transactional(readOnly = true)
	public boolean isConfirmExisted(String orderNumber) {
		Integer ret = confirmDao.countByOrderNumber(orderNumber);
		return ret > 0;
	}

	@Transactional
	public boolean askConfirm(String value) {
		Integer ret = retryMqMessageDao.updateStatusById(RetryMqMessageStatus.SUCCEEDED.getStatus(), value);
		return ret > 0;
	}

	private void saveConfirm(String orderNumber, BigDecimal agencyFee, BigDecimal unit, Date createdDate) {
		Confirm confirm = new Confirm();

		confirm.setAgencyFee(agencyFee);
		confirm.setOrderNumber(orderNumber);
		confirm.setUnit(unit);

		confirmDao.insert(confirm);
	}

	private void saveRetryMqMessage(String uuid, MqMessage mqMessage) {
		RetryMqMessage retryMqMessage = new RetryMqMessage();

		byte[] serializableMsg = KryoSerializer.serializer(mqMessage);

		retryMqMessage.setCreatedDate(new Date());
		retryMqMessage.setMessage(serializableMsg);
		retryMqMessage.setRetry(0);
		retryMqMessage.setStatus(RetryMqMessageStatus.SENDING.getStatus());
		retryMqMessage.setId(uuid);

		retryMqMessageDao.insert(retryMqMessage);
	}

	@Transactional
	public void deleteByOrderNumber(String orderNumber) {
		confirmDao.deleteByOrderNumber(orderNumber);
	}

	private OrderDto buildOrderDto(String orderNumber, BigDecimal agencyFee, BigDecimal unit) {
		OrderDto orderDto = new OrderDto();

		orderDto.setOrderNumber(orderNumber);
		orderDto.setAgencyFee(agencyFee);
		orderDto.setUnit(unit);

		return orderDto;
	}
}
