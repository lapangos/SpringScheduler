package com.lapangos.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lapangos.service.ScheduledService;

public class ScheduledServiceImpl implements ScheduledService {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledServiceImpl.class);

	public void execute() {
		
		logger.info("I am scheduled");
	}
}
