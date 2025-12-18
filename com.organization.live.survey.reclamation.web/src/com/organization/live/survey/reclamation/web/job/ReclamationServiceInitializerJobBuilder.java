package com.organization.live.survey.reclamation.web.job;

import com.organization.live.db.entity.Entity;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.scheduler.Job;
import com.organization.live.scheduler.SchedulerException;
import com.organization.live.scheduler.impl.JobBuilderImpl;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.web.component.ReclamationServiceInitializerComponent;
import com.organization.live.survey.reclamation.web.impl.ReclamationServiceImpl;

/**
 * 
 * Update database to store the user's email response
 *
 */
public class ReclamationServiceInitializerJobBuilder extends JobBuilderImpl implements ReclamationServiceInitializerComponent
{
	public ReclamationServiceInitializerJobBuilder(final ReclamationServiceImpl service, final LoggerFactory loggerFactory)
	{
		getContext().put(SERVICE, service);
		getContext().put(LOGGER_FACTORY, loggerFactory);
	}
	

	@Override
	public Job build(Entity entity, String type) throws SchedulerException 
	{
		return new ReclamationServiceInitializerJob(type, getName(entity));
	}
	

	@Override
	public boolean canBuild(String type) 
	{
		return Db.ReclamationServletInitJob.TABLE_NAME.equalsIgnoreCase(type);
	}
}
