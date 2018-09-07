package org.jackson.puppy.demo.dubbo.confirm.schedule;

import org.jackson.puppy.demo.dubbo.confirm.service.impl.ConfirmServiceImpl;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Kevin Hwang
 * @since 8/10/2018
 */
public class RetrySendConfirmJob extends QuartzJobBean {

	@Autowired
	private ConfirmServiceImpl confirmService;

	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		confirmService.retrySendConfirm();
	}
}
