package org.jackson.puppy.demo.dubbo.bill.api.service;

import org.jackson.puppy.demo.dubbo.bill.api.dto.BillDto;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface BillService {

	@TccTransactional
	void addBill(BillDto billDto);

	void rollBackBill(String orderNumber);
}
