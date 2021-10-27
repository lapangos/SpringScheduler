package com.lapangos.scheduler.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.lapangos.service.ScheduledService;

public class ScheduledJob extends QuartzJobBean {

	@Autowired
	ScheduledService scheduledService;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		scheduledService.execute();

	}

}
