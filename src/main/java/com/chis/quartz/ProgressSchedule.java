package com.chis.quartz;

/**
 * 日期：2019年03月05日
 * 作者：刘铭
 * 邮箱：liuming@bsoft.com.cn
 */

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * 进度查询任务类
 */
public class ProgressSchedule{

    private static Scheduler scheduler;

    /**
     * 单例实现
     */
    private static class ScheduleHolder{
        private static Scheduler instance;
        static {
            try {
                instance = new StdSchedulerFactory().getScheduler();
            } catch (SchedulerException e) {
                throw new IllegalStateException();
            }
        }
    }
    public static Scheduler getInstance(){
        ProgressSchedule.scheduler = ScheduleHolder.instance;
        return ProgressSchedule.scheduler;
    }

    /**
     * 开始进行任务
     */
    public void start(JobKey jobKey) throws SchedulerException {
        scheduler.start();
        JobDetail jobDetail = JobBuilder.newJob(ProgressJob.class).withIdentity(jobKey).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("dateUpdateStat","kettle").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3).repeatForever()).build();
        scheduler.scheduleJob(jobDetail,trigger);
    }

}
