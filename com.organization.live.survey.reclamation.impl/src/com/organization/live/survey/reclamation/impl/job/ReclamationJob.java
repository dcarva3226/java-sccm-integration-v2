package com.organization.live.survey.reclamation.impl.job;

import static com.organization.live.survey.reclamation.component.ReclamationComponent.COMPONENT_NAME;
import static com.organization.live.survey.reclamation.component.ReclamationComponent.LOG_PREFIX;

import java.util.Map;

import com.organization.live.db.DbApi;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.logger.LoggerService;
import com.organization.live.notification.transport.service.TransportService;
import com.organization.live.scheduler.impl.JobImpl;
import com.organization.live.survey.reclamation.impl.ReclamationPlanImpl;
import com.organization.live.survey.reclamation.impl.component.ReclamationJobComponent;

/**
 * Reclamation Job
 * 
 * @author Danny Carvajal
 */
@SuppressWarnings("serial")
public class ReclamationJob extends JobImpl implements ReclamationJobComponent 
{
	public ReclamationJob(final String type, final String name) 
	{
		super(type, name);
	}

	/* (non-Javadoc)
	 * @see com.organization.live.scheduler.Job#run(java.util.Map)
	 */
	@Override
	public Object run(final Map<String, Object> jobContext) throws Exception 
	{		
		DbApi dbApi = getDbApi(jobContext);
		ReclamationPlanImpl service = new ReclamationPlanImpl();
		LoggerFactory loggerFactory = (LoggerFactory) jobContext.get(LOGGER_FACTORY);
		LoggerService loggerService = loggerFactory.createLoggerService(dbApi);
		Logger logger = getLogger(getClass().getSimpleName(), loggerService);
		
		for(LogLevel level : LogLevel.values()) 
		{
	         logger.keepFor(level, 90);
	    }		
		
        service.doRun(dbApi, 
        		logger, 
        		(TransportService) jobContext.get(TRANSPORT_SERVICE));
		
        return null;
	}
	
	
	/**
	 * Return Logger object.
	 */
    public Logger getLogger(final String type, final LoggerService loggerService) 
    {
        return loggerService.getLogger(LOG_PREFIX + type, 
        		COMPONENT_NAME, 
        		LOG_PREFIX + type );
    }  	
}