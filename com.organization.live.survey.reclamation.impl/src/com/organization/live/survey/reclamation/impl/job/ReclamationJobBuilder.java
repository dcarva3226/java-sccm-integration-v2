package com.organization.live.survey.reclamation.impl.job;

import com.organization.live.db.entity.Entity;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.notification.transport.service.TransportService;
import com.organization.live.scheduler.Job;
import com.organization.live.scheduler.SchedulerException;
import com.organization.live.scheduler.impl.JobBuilderImpl;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.impl.ReclamationPlanImpl;
import com.organization.live.survey.reclamation.impl.component.ReclamationJobComponent;

/**
 * Reclamation Job Builder
 * 
 * @author Danny Carvajal
 */
public class ReclamationJobBuilder extends JobBuilderImpl implements ReclamationJobComponent 
{
	public ReclamationJobBuilder(final ReclamationPlanImpl service, 
			final LoggerFactory loggerFactory,
			final TransportService transportService)
	{
		getContext().put(SERVICE, service);
		getContext().put(LOGGER_FACTORY, loggerFactory);
		getContext().put(TRANSPORT_SERVICE, transportService);
	}	
	
	@Override
	public Job build(final Entity entity, final String type) throws SchedulerException 
	{
		return new ReclamationJob(type, getName(entity));
	}

	@Override
	public boolean canBuild(final String type) 
	{
		return Db.ReclamationJob.TABLE_NAME.equalsIgnoreCase(type);
	}

}