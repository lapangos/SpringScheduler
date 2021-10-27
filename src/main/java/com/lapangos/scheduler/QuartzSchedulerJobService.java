package com.lapangos.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

@Service
public class QuartzSchedulerJobService {

	@Autowired
	@Lazy
	SchedulerFactoryBean schedulerFactoryBean;

	@Autowired
	private ApplicationContext context;

	public boolean scheduleCronJob(String jobName, String groupName, Class<? extends QuartzJobBean> jobClass, Date date,
			String cronExpression) {
		System.out.println("Request received to scheduleJob");

		String jobKey = jobName;
		String groupKey = groupName;
		String triggerKey = jobName;

		JobDetail jobDetail = JobUtil.createJob(jobClass, false, context, jobKey, groupKey);

		System.out.println("creating trigger for key :" + jobKey + " at date :" + date);
		Trigger cronTriggerBean = JobUtil.createCronTrigger(triggerKey, date, cronExpression,
				SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			Date dt = scheduler.scheduleJob(jobDetail, cronTriggerBean);
			System.out.println("Job with key jobKey :" + jobKey + " and group :" + groupKey
					+ " scheduled successfully for date :" + dt);
			return true;
		} catch (SchedulerException e) {
			System.out.println(
					"SchedulerException while scheduling job with key :" + jobKey + " message :" + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	public boolean isJobRunning(String jobName, String groupName) {
		System.out.println("Request received to check if job is running");

		String jobKey = jobName;
		String groupKey = groupName;

		System.out.println("Parameters received for checking job is running now : jobKey :" + jobKey);
		try {

			List<JobExecutionContext> currentJobs = schedulerFactoryBean.getScheduler().getCurrentlyExecutingJobs();
			if (currentJobs != null) {
				for (JobExecutionContext jobCtx : currentJobs) {
					String jobNameDB = jobCtx.getJobDetail().getKey().getName();
					String groupNameDB = jobCtx.getJobDetail().getKey().getGroup();
					if (jobKey.equalsIgnoreCase(jobNameDB) && groupKey.equalsIgnoreCase(groupNameDB)) {
						return true;
					}
				}
			}
		} catch (SchedulerException e) {
			System.out.println("SchedulerException while checking job with key :" + jobKey
					+ " is running. error message :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return false;
	}

	/**
	 * Get all jobs
	 */

	public List<Map<String, Object>> getAllJobs() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();

			for (String groupName : scheduler.getJobGroupNames()) {
				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();

					// get job's trigger
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
					Date scheduleTime = triggers.get(0).getStartTime();
					Date nextFireTime = triggers.get(0).getNextFireTime();
					Date lastFiredTime = triggers.get(0).getPreviousFireTime();

					Map<String, Object> map = new HashMap<String, Object>();
					map.put("jobName", jobName);
					map.put("groupName", jobGroup);
					map.put("scheduleTime", scheduleTime);
					map.put("lastFiredTime", lastFiredTime);
					map.put("nextFireTime", nextFireTime);

					if (isJobRunning(jobName, jobGroup)) {
						map.put("jobStatus", "RUNNING");
					} else {
						String jobState = getJobState(jobName, jobGroup);
						map.put("jobStatus", jobState);
					}

					/*
					 * Date currentDate = new Date(); if (scheduleTime.compareTo(currentDate) > 0) {
					 * map.put("jobStatus", "scheduled");
					 * 
					 * } else if (scheduleTime.compareTo(currentDate) < 0) { map.put("jobStatus",
					 * "Running");
					 * 
					 * } else if (scheduleTime.compareTo(currentDate) == 0) { map.put("jobStatus",
					 * "Running"); }
					 */

					list.add(map);
					System.out.println("Job details:");
					System.out.println(
							"Job Name:" + jobName + ", Group Name:" + groupName + ", Schedule Time:" + scheduleTime);
				}

			}
		} catch (SchedulerException e) {
			System.out.println("SchedulerException while fetching all jobs. error message :" + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Check job exist with given name
	 */

	public boolean isJobWithNamePresent(String jobName, String groupName) {
		try {
			String groupKey = groupName;
			JobKey jobKey = new JobKey(jobName, groupKey);
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			if (scheduler.checkExists(jobKey)) {
				return true;
			}
		} catch (SchedulerException e) {
			System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get the current state of job
	 */
	public String getJobState(String jobName, String groupName) {
		System.out.println("JobServiceImpl.getJobState()");

		try {
			String groupKey = groupName;
			JobKey jobKey = new JobKey(jobName, groupKey);

			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);

			List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
			if (triggers != null && triggers.size() > 0) {
				for (Trigger trigger : triggers) {
					TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

					if (TriggerState.PAUSED.equals(triggerState)) {
						return "PAUSED";
					} else if (TriggerState.BLOCKED.equals(triggerState)) {
						return "BLOCKED";
					} else if (TriggerState.COMPLETE.equals(triggerState)) {
						return "COMPLETE";
					} else if (TriggerState.ERROR.equals(triggerState)) {
						return "ERROR";
					} else if (TriggerState.NONE.equals(triggerState)) {
						return "NONE";
					} else if (TriggerState.NORMAL.equals(triggerState)) {
						return "SCHEDULED";
					}
				}
			}
		} catch (SchedulerException e) {
			System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Stop a job
	 */

	public boolean stopJob(String jobName, String groupName) {
		System.out.println("JobServiceImpl.stopJob()");
		try {
			String jobKey = jobName;
			String groupKey = groupName;

			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			JobKey jkey = new JobKey(jobKey, groupKey);

			return scheduler.interrupt(jkey);

		} catch (SchedulerException e) {
			System.out.println("SchedulerException while stopping job. error message :" + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public boolean rescheduleJob(String jobName, String groupName, Date startTime, String cronExpression) {
		System.out.println("JobServiceImpl.resceduleJob()");

		try {
			Scheduler scheduler = schedulerFactoryBean.getScheduler();
			Trigger newTrigger = JobUtil.createCronTrigger(jobName, startTime, cronExpression,
					SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
			if (scheduler.rescheduleJob(new TriggerKey(jobName), newTrigger) != null)
				return true;
		} catch (SchedulerException e) {
			System.out.println("SchedulerException while rescheduling job. error message :" + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}
}
