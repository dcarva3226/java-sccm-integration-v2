package com.organization.live.survey.reclamation.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

import com.organization.live.db.DbApi;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.survey.reclamation.Reclamation;
import com.organization.live.survey.reclamation.component.ReclamationLoggerDetails;

/**
 * @author Danny Carvajal
 */
public class BaseReclamationPlan implements Reclamation
{
	private static final String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALGORITHM = "SHA1PRNG";
	protected Logger logger;

	public void setLogger(final Logger logger)
	{
		// Since this is kicked off every time the init() method runs, this may already be set
		if (this.logger == null)
		{
			this.logger = logger;
		}
	}
       
    public String getName() 
    {
        return getClass().getSimpleName();
    }        

    
    /**
     * Logger wrapper
     */	
    protected void writeToLog(LogLevel logLevel, String msg, ReclamationLoggerDetails loggerDetails, Logger logger)
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
        		logger.fatal(msg, loggerDetails);
        		break;
        }        
    }
    
    
    /**
     * Return random string
     */	
	protected String getRandomString(final int length) 
	{  
	    SecureRandom rand;
	    try 
	    {
	        rand = SecureRandom.getInstance(ALGORITHM);
	    } 
	    catch (final NoSuchAlgorithmException e) 
	    {
			writeToLog(LogLevel.WARN, 
					String.format("Reclamation %s algorithm was not found, using default.", ALGORITHM), 
					null,
					this.logger);

			rand = new SecureRandom();
	    }
	    final StringBuilder randomString = new StringBuilder(length);
	    for (int i = 0; i < length; i++) 
	    {
	        final int pos = rand.nextInt(charset.length());
	        randomString.append(charset.charAt(pos));
	    }
	    return randomString.toString();
	}
	
	
    /**
     * Get diff date in days from startDate to Now.
     */	
	protected long getDiffDaysFromNow(Date startDate)
	{
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(startDate);
		cal2.setTime(cal2.getTime());
		long mil1 = cal1.getTimeInMillis();
		long mil2 = cal2.getTimeInMillis();
		long diff = mil2 - mil1;
		return diff / (24 * 60 * 60 * 1000);	
	}

	@Override
	public void doRun(DbApi dbApi) {
		// TODO Auto-generated method stub
		
	}
	
	
    /**
     * Slow things down a tad for logging reasons.
     */		
	protected void Sleep(final Long interval)
	{
		try 
		{
		    Thread.sleep(interval);
		} 
		catch (InterruptedException e) 
		{
		    // Just continue
		}
	}
}
