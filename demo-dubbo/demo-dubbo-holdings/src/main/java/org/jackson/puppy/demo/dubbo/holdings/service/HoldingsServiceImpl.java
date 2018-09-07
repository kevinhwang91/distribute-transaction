package org.jackson.puppy.demo.dubbo.holdings.service;

import org.jackson.puppy.demo.dubbo.holdings.api.dto.HoldingsDto;
import org.jackson.puppy.demo.dubbo.holdings.api.enums.HoldingsResourceStatus;
import org.jackson.puppy.demo.dubbo.holdings.api.exception.OptimisticLockTimeoutException;
import org.jackson.puppy.demo.dubbo.holdings.api.service.HoldingsService;
import org.jackson.puppy.demo.dubbo.holdings.dao.HoldingsDao;
import org.jackson.puppy.demo.dubbo.holdings.dao.HoldingsResourceDao;
import org.jackson.puppy.demo.dubbo.holdings.po.Holdings;
import org.jackson.puppy.demo.dubbo.holdings.po.HoldingsResource;
import org.jackson.puppy.tcc.transaction.api.TccTransactional;
import org.jackson.puppy.tcc.transaction.dubbo.context.DubboTransactionContextEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
@Service("holdingsService")
public class HoldingsServiceImpl implements HoldingsService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HoldingsDao holdingsDao;

	@Autowired
	private HoldingsResourceDao holdingsResourceDao;

	@Value("${tcc.isTested:false}")
	private boolean isTestedTcc;

	@Override
	@TccTransactional(confirmMethod = "confirm", cancelMethod = "cancel", transactionContextEditor = DubboTransactionContextEditor.class)
	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = OptimisticLockTimeoutException.class)
	public void addHoldings(HoldingsDto holdingsDto) throws OptimisticLockTimeoutException {
		logger.info("Add holdings in trying stage.");

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		String orderNumber = holdingsDto.getOrderNumber();

		int ret = doServiceWithOptimisticLock(holdingsDto.getAccountNumber(),
				holdingsDto.getUnit(), this::tryUnit, 100, 10 * 1000);
		if (ret > 0) {
			HoldingsResource holdingsResource = new HoldingsResource();
			holdingsResource.setOrderNumber(orderNumber);
			holdingsResource.setStatus(HoldingsResourceStatus.PENDING.getStatus());
			holdingsResource.setCreatedDate(holdingsDto.getDate());
			holdingsResourceDao.insert(holdingsResource);
		}

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add holdings try completed.");
	}


	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = OptimisticLockTimeoutException.class)
	public void confirm(HoldingsDto holdingsDto) throws OptimisticLockTimeoutException {
		logger.info("Add holdings in confirming stage.");

		Integer ret = holdingsResourceDao.updateStatusByOrderNumberAndStatusAndCreatedDate(HoldingsResourceStatus.CONFIRM.getStatus(),
				holdingsDto.getOrderNumber(), HoldingsResourceStatus.PENDING.getStatus(), holdingsDto.getDate());
		if (ret == 0) {
			return;
		}
		doServiceWithOptimisticLock(holdingsDto.getAccountNumber(),
				holdingsDto.getUnit(), this::confirmUnit, 100, 10 * 1000);

		if (isTestedTcc && ((int) (Math.random() * 100) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add holdings confirm completed.");
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = OptimisticLockTimeoutException.class)
	public void cancel(HoldingsDto holdingsDto) throws OptimisticLockTimeoutException {
		logger.info("Add holdings in canceling stage.");

		Integer ret = holdingsResourceDao.deleteByOrderNumberAndStatusAndCreatedDate(holdingsDto.getOrderNumber(),
				HoldingsResourceStatus.PENDING.getStatus(), holdingsDto.getDate());
		if (ret == 0) {
			return;
		}
		doServiceWithOptimisticLock(holdingsDto.getAccountNumber(),
				holdingsDto.getUnit(), this::cancelUnit, 100, 10 * 1000);

		if (isTestedTcc && ((int) (Math.random() * 50) == 0)) {
			int throwException = 1 / 0;
		}

		logger.info("Add holdings cancel completed.");
	}

	private int tryUnit(String accountNumber, BigDecimal addUnit, Holdings holdings) {
		BigDecimal oUnit = holdings.getUnit();
		BigDecimal oFreezeUnit = holdings.getFreezeUnit();
		Integer version = holdings.getVersion();
		BigDecimal nUnit = oUnit.add(addUnit);
		BigDecimal nFreezeUnit = oFreezeUnit.add(addUnit);

		return holdingsDao.updateUnitAndFreezeUnitByAccountNumberAndVersion(nUnit, nFreezeUnit, accountNumber, version);
	}

	private int confirmUnit(String accountNumber, BigDecimal addUnit, Holdings holdings) {
		BigDecimal oFreezeUnit = holdings.getFreezeUnit();
		Integer version = holdings.getVersion();
		BigDecimal nFreezeUnit = oFreezeUnit.subtract(addUnit);

		return holdingsDao.updateFreezeUnitByAccountNumberAndVersion(nFreezeUnit, accountNumber, version);
	}

	private int cancelUnit(String accountNumber, BigDecimal addUnit, Holdings holdings) {
		BigDecimal oUnit = holdings.getUnit();
		BigDecimal oFreezeUnit = holdings.getFreezeUnit();
		Integer version = holdings.getVersion();
		BigDecimal nUnit = oUnit.subtract(addUnit);
		BigDecimal nFreezeUnit = oFreezeUnit.subtract(addUnit);

		return holdingsDao.updateUnitAndFreezeUnitByAccountNumberAndVersion(nUnit, nFreezeUnit, accountNumber, version);
	}

	private int doServiceWithOptimisticLock(String accountNumber,
	                                        BigDecimal addUnit,
	                                        HoldingsServiceStrategy strategy,
	                                        long retryInterval,
	                                        long timeout) throws OptimisticLockTimeoutException {
		int ret = 0;
		long start = System.currentTimeMillis();

		while (true) {

			Holdings holdings = holdingsDao.getByAccountNumber(accountNumber);
			if (holdings == null) {
				break;
			}

			ret = strategy.doService(accountNumber, addUnit, holdings);
			if (ret > 0) {
				break;
			} else if (System.currentTimeMillis() - start > timeout) {
				throw new OptimisticLockTimeoutException("Optimist lock timeout.");
			}
			logger.info("Optimist lock retry with accountNumber: {}, addUnit: {}, version: {}.", accountNumber, addUnit, holdings.getVersion());

			try {
				Thread.sleep(retryInterval);
			} catch (InterruptedException e) {
			}
		}

		return ret;
	}

	@Override
	@Transactional
	public void rollBackHoldings(String orderNumber, String accountNumber) {
		logger.info("Roll back holdings by order number: {}, account number: {}.", orderNumber, accountNumber);

		Holdings holdings = holdingsDao.getByAccountNumber(accountNumber);

		if (holdings != null && !(holdings.getVersion() == 1 && holdings.getUnit().equals(BigDecimal.ZERO) && holdings.getFreezeUnit().equals(BigDecimal.ZERO))) {
			holdingsDao.updateUnitAndFreezeUnitAndVersionByAccountNumber(BigDecimal.ZERO, BigDecimal.ZERO, 1, accountNumber);
		}

		holdingsResourceDao.deleteByOrderNumber(orderNumber);
	}

	@FunctionalInterface
	private interface HoldingsServiceStrategy {

		int doService(String accountNumber, BigDecimal addUnit, Holdings holdings);

	}
}



