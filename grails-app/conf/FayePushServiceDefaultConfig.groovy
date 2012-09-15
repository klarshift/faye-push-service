import grails.plugin.quartz2.ClosureJob

import org.quartz.impl.triggers.SimpleTriggerImpl

/* quartz setup */
grails.plugin.quartz2.autoStartup = true
org{
	quartz{
		scheduler.instanceName = 'PushServiceScheduler'
		threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
		threadPool.threadCount = 20
		threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
		jobStore.class = 'org.quartz.simpl.RAMJobStore'
	}
}
grails.plugin.quartz2.jobSetup.checkServices = { quartzScheduler, ctx ->
	def jobDetail = ClosureJob.createJob { jobCtx , appCtx->
		appCtx.pushService.checkServices()		
	}
	def trigger1 = new SimpleTriggerImpl(name:"trig1", startTime:new Date(),repeatInterval:15*1000,repeatCount:-1)
	quartzScheduler.scheduleJob(jobDetail, trigger1)
}