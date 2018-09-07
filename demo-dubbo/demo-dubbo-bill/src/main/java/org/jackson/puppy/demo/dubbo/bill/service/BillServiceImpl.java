package org.jackson.puppy.demo.dubbo.bill.service;

import org.jackson.puppy.demo.dubbo.bill.api.dto.BillDto;
import org.jackson.puppy.demo.dubbo.bill.api.enums.BillStatus;
import org.jackson.puppy.demo.dubbo.bill.api.service.BillService;
import org.jackson.puppy.demo.dubbo.bill.dao.BillDao;
import org.jackson.puppy.demo.dubbo.bill.po.Bill;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;
import org.jackson.puppy.tcc.transaction.dubbo.context.DubboTransactionContextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Service("billService")
public class BillServiceImpl implements BillService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private BillDao billDao;

	@Value("${tcc.isTested:false}")
	private boolean isTestedTcc;

	@Override
	@TccTransactional(confirmMethod = "confirm", cancelMethod = "cancel", transactionContextEditor = DubboTransactionContextEditor.class)
	@Transactional
	public void addBill(BillDto billDto) {
		logger.info("Add bill in trying stage.");

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		String orderNumber = billDto.getOrderNumber();

		Bill bill = new Bill();
		bill.setAgencyFee(billDto.getAgencyFee());
		bill.setOrderNumber(orderNumber);
		bill.setStatus(BillStatus.PENDING.getStatus());
		bill.setCreatedDate(billDto.getDate());
		billDao.insert(bill);

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add bill try completed");
	}

	@Transactional
	public void confirm(BillDto billDto) {
		logger.info("Add bill in confirming stage.");

		billDao.updateStatusByStatusAndOrderNumberAndCreatedDate(BillStatus.CONFIRMED.getStatus(),
				BillStatus.PENDING.getStatus(), billDto.getOrderNumber(), billDto.getDate());

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add bill confirm completed.");
	}

	@Transactional
	public void cancel(BillDto billDto) {
		logger.info("Add bill in canceling stage.");

		billDao.deleteByStatusAndOrderNumberAndCreatedDate(BillStatus.PENDING.getStatus(),
				billDto.getOrderNumber(), billDto.getDate());

		if (isTestedTcc && ((int) (Math.random() * 50) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add bill cancel completed.");
	}

	@Override
	public void rollBackBill(String orderNumber) {
		logger.info("Roll back bill by order number: {}.", orderNumber);

		billDao.deleteByOrderNumber(orderNumber);
	}
}
