package com.lapangos.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ScheduledService {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledService.class);

	@Scheduled
	public void execute() {
		
		logger.info("I am scheduled");
	}
}
