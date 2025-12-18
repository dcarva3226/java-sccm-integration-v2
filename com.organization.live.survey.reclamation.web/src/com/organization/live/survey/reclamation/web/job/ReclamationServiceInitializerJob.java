package com.organization.live.survey.reclamation.web.job;

import java.util.Map;

import static com.organization.live.survey.reclamation.component.ReclamationComponent.*;

import com.organization.live.db.DbApi;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.logger.LoggerService;
import com.organization.live.scheduler.impl.JobImpl;
import com.organization.live.survey.reclamation.web.component.ReclamationServiceInitializerComponent;
import com.organization.live.survey.reclamation.web.impl.ReclamationServiceImpl;

/**
 * 
 * @author Danny Carvajal
 *
 */
@SuppressWarnings("serial")
public class ReclamationServiceInitializerJob extends JobImpl implements ReclamationServiceInitializerComponent
{
	public ReclamationServiceInitializerJob(String type, String name) 
	{
		super(type, name);
	}

	
	@Override
	public Object run(Map<String, Object> jobContext) throws Exception 
	{
		DbApi dbApi = getDbApi(jobContext);
		ReclamationServiceImpl service = (ReclamationServiceImpl) jobContext.get(SERVICE);
		LoggerFactory loggerFactory = (LoggerFactory) jobContext.get(LOGGER_FACTORY);
		LoggerService loggerService = loggerFactory.createLoggerService(dbApi);
		Logger logger = getLogger(getClass().getSimpleName(), loggerService);
				
		for(LogLevel level : LogLevel.values()) 
		{
	         logger.keepFor(level, 90);
	    }
		
		service.init(dbApi, logger, getSecurityToken(jobContext));
		return null;
	}
	
	
    public Logger getLogger(final String type, final LoggerService loggerService) 
    {
        return loggerService.getLogger(LOG_PREFIX + type, 
        		COMPONENT_NAME, 
        		LOG_PREFIX + type );
    }    
}
