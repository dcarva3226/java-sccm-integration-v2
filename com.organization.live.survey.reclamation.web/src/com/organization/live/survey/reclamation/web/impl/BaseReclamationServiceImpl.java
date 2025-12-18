package com.organization.live.survey.reclamation.web.impl;

import com.organization.live.db.DbApi;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.survey.reclamation.component.ReclamationLoggerDetails;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class BaseReclamationServiceImpl 
{
	protected DbApi dbApi;
	
	/**
	 * 
	 * @author Logger wrapper
	 *
	 */	
    protected void writeToLog(LogLevel logLevel, 
    		String msg, 
    		Logger logger, 
    		ReclamationLoggerDetails loggerDetails)
    {    

    	switch(logLevel)
        {
            case INFO : 
        		logger.info(msg, loggerDetails);
        		break;
        	case WARN:
        		logger.warn(msg, loggerDetails);
        		break;
        	case ERROR:
        		logger.error(msg, loggerDetails);
        		break;    
        	case TRACE:
        		logger.trace(msg, loggerDetails);
        		break;
        	case FATAL:
        		logger.fatal(msg, loggerDetails);
        		break;        
        	case DEBUG:
        		logger.debug(msg, loggerDetails);
        		break;
        }        
    }
}
