package com.lapangos.config;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.lapangos.scheduler.QuartzSchedulerJobService;
import com.lapangos.scheduler.jobs.ScheduledJob;

@Configuration
public class SpringMongoConfig {

	@Value("${scheduledTime}")
	private String scheduledTime;

	@Autowired
	private QuartzSchedulerJobService jobService;

	@PostConstruct
	private void readSchedulerConfig() {
		jobService.scheduleCronJob("SCHEDULED_SERVICE", "SCHEDULER_SERVICES", ScheduledJob.class, new Date(),
				scheduledTime);
	}

}
