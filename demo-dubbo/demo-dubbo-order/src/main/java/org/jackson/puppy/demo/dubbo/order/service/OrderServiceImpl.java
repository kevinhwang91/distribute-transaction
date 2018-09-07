package org.jackson.puppy.demo.dubbo.order.service;

import org.jackson.puppy.demo.dubbo.bill.api.dto.BillDto;
import org.jackson.puppy.demo.dubbo.bill.api.service.BillService;
import org.jackson.puppy.demo.dubbo.holdings.api.dto.HoldingsDto;
import org.jackson.puppy.demo.dubbo.holdings.api.exception.OptimisticLockTimeoutException;
import org.jackson.puppy.demo.dubbo.holdings.api.service.HoldingsService;
import org.jackson.puppy.demo.dubbo.order.api.dto.OrderDto;
import org.jackson.puppy.demo.dubbo.order.api.enums.OrderStatus;
import org.jackson.puppy.demo.dubbo.order.api.exception.OrderConfirmTryException;
import org.jackson.puppy.demo.dubbo.order.api.exception.OrderProcessingException;
import org.jackson.puppy.demo.dubbo.order.api.service.OrderService;
import org.jackson.puppy.demo.dubbo.order.dao.OrderDao;
import org.jackson.puppy.demo.dubbo.order.pojo.Order;
import org.jackson.puppy.redis.lock.annotation.RdLock;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;
import org.jackson.puppy.tcc.transaction.dubbo.context.DubboTransactionContextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Service("orderService")
public class OrderServiceImpl implements OrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BillService billService;

	@Autowired
	private HoldingsService holdingsService;

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${tcc.isTested:false}")
	private boolean isTestedTcc;

	@Override
	@RdLock(preName = "orderNumber:", keyIndex = 0, retry = 30, retryInterval = 300, expire = 60000)
	public void handleOrder(String orderNumber, OrderDto orderDto) throws OrderProcessingException, OrderConfirmTryException {
		OrderService orderService = applicationContext.getBean(OrderService.class);
		Order order = orderDao.getByOrderNumber(orderNumber);
		if (order == null) {
			logger.info("Failed to find order by orderNumber: {}.", orderNumber);
			return;
		}

		Integer status = order.getStatus();
		if (status == OrderStatus.RECEIVED.getStatus()) {
			logger.info("OrderNumber: {} had processed.", orderNumber);
			return;
		} else if (status == OrderStatus.RECEIVING.getStatus()) {
			throw new OrderProcessingException("Confirm order tcc trying now, please try again later.");
		}
		orderService.confirmOrder(orderDto, order.getAccountNumber(), new Date());
	}

	@Override
	@TccTransactional(confirmMethod = "confirm", cancelMethod = "cancel", transactionContextEditor = DubboTransactionContextEditor.class)
	@Transactional
	public void confirmOrder(OrderDto orderDto, String accountNumber, Date date) throws OrderConfirmTryException {
		logger.info("Confirm order in trying stage.");

		String orderNumber = orderDto.getOrderNumber();

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		orderDao.updateStatusByStatusAndOrderNumber(OrderStatus.RECEIVING.getStatus(),
				OrderStatus.PAID.getStatus(), orderNumber, date);

		BigDecimal unit = orderDto.getUnit();
		BigDecimal agencyFee = orderDto.getAgencyFee();

		billService.addBill(buildBillDto(orderNumber, agencyFee, date));

		try {
			holdingsService.addHoldings(buildHoldingsDto(accountNumber, orderNumber, unit, date));
		} catch (OptimisticLockTimeoutException e) {
			throw new OrderConfirmTryException(e);
		}

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Confirm order try completed.");
	}

	@Transactional
	public void confirm(OrderDto orderDto, String accountNumber, Date date) {
		logger.info("Confirm order in confirm stage.");

		orderDao.updateStatusByStatusAndOrderNumberAndUpdatedDate(OrderStatus.RECEIVED.getStatus(),
				OrderStatus.RECEIVING.getStatus(), orderDto.getOrderNumber(), date);

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Confirm order confirm completed.");
	}

	@Transactional
	public void cancel(OrderDto orderDto, String accountNumber, Date date) {
		logger.info("Confirm order in cancel stage.");

		orderDao.updateStatusByStatusAndOrderNumberAndUpdatedDate(OrderStatus.PAID.getStatus(),
				OrderStatus.RECEIVING.getStatus(), orderDto.getOrderNumber(), date);

		if (isTestedTcc && ((int) (Math.random() * 50) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Confirm order cancel completed.");
	}

	private HoldingsDto buildHoldingsDto(String accountNumber, String orderNumber, BigDecimal unit, Date date) {
		HoldingsDto holdingsDto = new HoldingsDto();

		holdingsDto.setAccountNumber(accountNumber);
		holdingsDto.setUnit(unit);
		holdingsDto.setOrderNumber(orderNumber);
		holdingsDto.setDate(date);

		return holdingsDto;
	}

	private BillDto buildBillDto(String orderNumber, BigDecimal agencyFee, Date date) {
		BillDto billDto = new BillDto();

		billDto.setAgencyFee(agencyFee);
		billDto.setOrderNumber(orderNumber);
		billDto.setDate(date);

		return billDto;
	}

	@Override
	@Transactional
	public void rollBackOrder(String orderNumber) {
		Order order = orderDao.getByOrderNumber(orderNumber);

		if (order == null || order.getStatus() == OrderStatus.PAID.getStatus()) {
			return;
		}
		String accountNumber = order.getAccountNumber();

		logger.info("Roll back order by order number: {}.{}", orderNumber, accountNumber);

		orderDao.updateStatusByOrderNumber(OrderStatus.PAID.getStatus(), orderNumber);

		billService.rollBackBill(orderNumber);

		holdingsService.rollBackHoldings(orderNumber, accountNumber);
	}

}
