package org.jackson.puppy.demo.dubbo.holdings.api.service;

import org.jackson.puppy.demo.dubbo.holdings.api.dto.HoldingsDto;
import org.jackson.puppy.demo.dubbo.holdings.api.exception.OptimisticLockTimeoutException;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public interface HoldingsService {

	@TccTransactional
	void addHoldings(HoldingsDto holdingsDto) throws OptimisticLockTimeoutException;

	void rollBackHoldings(String orderNumber, String accountNumber);
}
