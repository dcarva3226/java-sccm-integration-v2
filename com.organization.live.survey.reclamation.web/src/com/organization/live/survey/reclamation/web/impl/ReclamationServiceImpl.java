package com.organization.live.survey.reclamation.web.impl;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.organization.live.auth.AuthService;
import com.organization.live.auth.AuthorizationDeniedException;
import com.organization.live.auth.SecurityToken;
import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.survey.reclamation.component.ReclamationLoggerDetails;
import com.organization.live.survey.reclamation.db.Db;

/**
 * ReclamationServlet Implementation
 * responseType: true means user wants to uninstall software, false means user does not...
 * @author Danny Carvajal
 */
public class ReclamationServiceImpl extends BaseReclamationServiceImpl implements ReclamationService
{  
	private AuthService authService;
	private DbApi dbApi;
	private Logger logger;
	private String token;
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private static final long REFRESH_INTERVAL = 15;
	private static final String CONFIG_LOGO = "logo";
	

	/**
	 * 
	 * Initialize this object via the job context.
	 *
	 */
	public void init(final DbApi dbApi, Logger logger, String token)
	{
		this.token = token;
		this.dbApi = dbApi;
		this.logger = logger;
		startPeriodicTokenRefresh();
	}    
	
	
	public void setAuthService(final AuthService authService) 
	{
		this.authService = authService;
	}
	
	
	/**
	 * 
	 * Update database to store the user's email response
	 *
	 */
	public boolean updateEmailResponse(final Long reclamationComputerID, final String token, final boolean responseType)
	{
		boolean status = false;
		
		// Get reclamation plan id
		SelectQuery query = Query.select("r." + Db.Reclamation.COLUMN_ID);
		query.from(Db.Reclamation.TABLE_NAME, "r");
		query.join(Db.ReclamationComputer.TABLE_NAME, "rc", "rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, "r." + Db.Reclamation.COLUMN_ID);
		query.where(Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_ID, reclamationComputerID));		
		
		final Long reclamationPlanID = this.dbApi.getQueryExecutor().execute1(query);
		ReclamationLoggerDetails logDetails = new ReclamationLoggerDetails(reclamationPlanID);
		
		// Validate token sent in from email response (token + ReclamationComputer.id) but return approved value so we can do a check
		query = Query.select("r." + Db.Reclamation.COLUMN_APPROVED);
		query.from(Db.ReclamationEmail.TABLE_NAME, "re");
		query.join(Db.ReclamationComputer.TABLE_NAME, "rc", "rc." + Db.ReclamationComputer.COLUMN_ID, "re." + Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER);
		query.join(Db.Reclamation.TABLE_NAME, "r", "r." + Db.Reclamation.COLUMN_ID, "rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID);
		query.where(Criterion.AND(
				Criterion.EQ("re." + Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, reclamationComputerID),
				Criterion.EQ("re." + Db.ReclamationEmail.COLUMN_TOKEN, token)));		
		
		Boolean recDetail = this.dbApi.getQueryExecutor().execute1(query);
										
		// Did we found the ID + token match the Reclamation Email table?
		if (recDetail == null)
		{		
			// Log error
			writeToLog(LogLevel.ERROR, 
					String.format("Could not process user request for reclamation computer ID %d. The ID + Token combination was not found in database.", 
						reclamationComputerID), 
						this.logger,
						logDetails);
		}
		else if (recDetail)
		{
			// We found the ID + token match but this plan has already been submitted to SCCM
			writeToLog(LogLevel.WARN, 
					String.format("Could not update Reclamation computer with ID %d. The reclamation plan for this computer has already been submitted to SCCM.", 
						reclamationComputerID), 
						this.logger,
						logDetails);
		}
		else
		{	
			// We matched the computer + token sent in email. Update ReclamationComputer record with user's response
	    	Entity rec = this.dbApi.getEntityManager().readEntity(Db.ReclamationComputer.TABLE_NAME, Criterion.EQ(Db.ReclamationComputer.COLUMN_ID, reclamationComputerID));
			rec.set(Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, true);
			rec.set(Db.ReclamationComputer.COLUMN_USER_RESPONSE, responseType);
			if (responseType) rec.set(Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, null);
			rec.save();
			
			// Get Reclamation Email record id
			query = Query.select(Db.ReclamationEmail.COLUMN_ID);
			query.from(Db.ReclamationEmail.TABLE_NAME);
			query.where(Criterion.AND(
					Criterion.EQ(Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, reclamationComputerID),
					Criterion.EQ(Db.ReclamationEmail.COLUMN_TOKEN, token)));
			
			final Long reclamationEmailID = this.dbApi.getQueryExecutor().execute1(query);	
			
			// Now update Reclamation Email record to show that user responded
			Calendar calendar = Calendar.getInstance();
			Date now = calendar.getTime();
	    	rec = this.dbApi.getEntityManager().readEntity(Db.ReclamationEmail.TABLE_NAME, Criterion.EQ(Db.ReclamationEmail.COLUMN_ID, reclamationEmailID));
			rec.set(Db.ReclamationEmail.COLUMN_EMAIL_RESPONSE_TIMESTAMP, now);
			rec.save();
			
			writeToLog(LogLevel.INFO, 
					String.format("Updated 'reclamation computer' record ID=%d based on user response: User chose to %sinstall software.", 
						reclamationComputerID,
						responseType==true ? "un" : ""), 
						this.logger,
						logDetails);
			
			status = true;
		}
		return status;
	}
	
	
	/**
	 * 
	 * Prolong the token, otherwise the servlet will time out.
	 *
	 */
	private void startPeriodicTokenRefresh() 
	{
		executor.scheduleWithFixedDelay(new Runnable() 
		{
			@Override
			public void run() 
			{
				try 
				{
					authService.prolongToken(new SecurityToken(token));
				} 
				catch 
				(AuthorizationDeniedException e) 
				{
					throw new IllegalStateException("Failure to prolong user token", e);
				}
			}
		}, 0L, REFRESH_INTERVAL, MINUTES);
	}
		
    
    
	/**
	 * Returns the referenced logo image for use in email.
	 * @param none
	 * @return byte[] of logo
	 */		
    public byte[] getLogo() 
    {
    	// First read in logo from reclamation config table
    	List<Entity> reclamationConfig = dbApi.getEntityManager().readAll(Db.ReclamationConfig.TABLE_NAME);
    	Entity config = reclamationConfig.get(0);
    	byte[] logo = config.getBinary(CONFIG_LOGO);
    	
    	// If we found no logo (i.e. user has not uploaded one) then use the one from email dashboard config table
    	if (logo == null)
    	{
	    	List<Entity> configs = dbApi.getEntityManager().readAll(Db.UIDashboardEmailConfig.TABLE_NAME);
	        if (configs.size() <= 0) 
	        {
	            throw new RuntimeException(String.format("%s: Could not read logo from email configs.",
	            		getClass().getSimpleName()));
	        }
	        
	        config = configs.get(0);
	        logo = config.getBinary(CONFIG_LOGO);
    	}
    	
        return logo;
    }   
}
