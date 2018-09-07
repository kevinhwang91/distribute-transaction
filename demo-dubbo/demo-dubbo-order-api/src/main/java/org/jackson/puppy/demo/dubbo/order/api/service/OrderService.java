package org.jackson.puppy.demo.dubbo.order.api.service;

import org.jackson.puppy.demo.dubbo.order.api.dto.OrderDto;
import org.jackson.puppy.demo.dubbo.order.api.exception.OrderConfirmTryException;
import org.jackson.puppy.demo.dubbo.order.api.exception.OrderProcessingException;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;

import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface OrderService {

	@TccTransactional
	void confirmOrder(OrderDto orderDto, String accountNumber, Date date) throws OrderProcessingException, OrderConfirmTryException;

	void handleOrder(String orderNumber, OrderDto orderDto) throws OrderProcessingException, OrderConfirmTryException;

	void rollBackOrder(String orderNumber);
}
