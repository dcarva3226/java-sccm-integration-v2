package com.organization.live.survey.reclamation.impl;

import static com.organization.live.db.query.Query.count;

import java.util.Arrays;
import java.util.List;

import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.notification.transport.service.TransportService;
import com.organization.live.survey.reclamation.component.ReclamationLoggerDetails;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;

/**
 * Implementation of ReclamationPlan. This is where we poll reclamation records to see
 * what work needs to be done. (i.e. the sending of email notifications)
 * 
 * @author Danny Carvajal
 */
public class ReclamationPlanImpl extends BaseReclamationPlan
{
	private static final String DEFAULT_RECLAMATION_CONFIG = "Default Reclamation Configuration"; 
	private StringBuilder logMessage;
	private String reclamationPlanName = null;
	private ReclamationLoggerDetails logDetails = null;
	
	public void doRun(DbApi dbApi, 
			Logger logger, 
			TransportService transportService) throws Exception
	{					

		// Set up logger objects
		super.setLogger(logger);
			
		// Is reclamation enabled?
		if (! reclamationEnabled(dbApi))
		{
			writeToLog(LogLevel.INFO, "Reclamation is disabled in the reclamation config table. There is no work to be done...", 
					new ReclamationLoggerDetails(null), 
					logger);
			return;
		}
		else
		{
			writeToLog(LogLevel.INFO, 
					"Start checking Reclamation plans for pending work.", 
					new ReclamationLoggerDetails(null),
					logger);			
		}
		
		// Get reclamation records that have not already been submitted to SCCM	
		List<Object[]> reclamations = getUnsumbittedReclamationPlans(dbApi);
		
		// Do we have records to process?
		if (reclamations.size() == 0)
		{
			writeToLog(LogLevel.INFO, 
					"Checking Reclamation status : There are no reclamaton plans to process.", 
					new ReclamationLoggerDetails(null),
					logger);			
			return;
		}	
		
		// Loop through reclamation records that have not already been submitted to SCCM		
		for (Object[] reclamation : reclamations) 
	    {
	    	// Slow down a bit so logging is in order				
	    	Sleep(2000L);
	    	
	    	Long reclamationPlanID = (Long) reclamation[0];
	    	this.reclamationPlanName = reclamation[1].toString();
	    	Boolean enabled = (Boolean) reclamation[2];
			this.logDetails = new ReclamationLoggerDetails(reclamationPlanID);   	
	    	ReclamationPlan rec = new ReclamationPlan(reclamationPlanID, dbApi, transportService, logger, logDetails);
	    	logMessage = new StringBuilder();
	    	
			// Is the reclamation plan enabled?
			if (! enabled)
			{
		    	logMessage.append(String.format("Check plan '%s' : The plan will be skipped because it is disabled.", reclamationPlanName)); 
		    	writeToLog(LogLevel.INFO, 
						logMessage.toString(), 
						logDetails,
						logger);
				continue;
			}
			
			// Check Send Email Wait Days. Is it time to send emails?
			if (! rec.sendingEmailsAllowed(reclamationPlanID))
			{
		    	logMessage.append(String.format("Check plan '%s' : The plan will be skipped because it is not yet time to send out email notifications.", reclamationPlanName));  
		    	writeToLog(LogLevel.INFO, 
						logMessage.toString(), 
						logDetails,
						logger);
				continue;
			}
			
			// Did we find any computers for this reclamation plan that have never had an email sent?
		    Boolean a = sendInitialNotifications(rec, dbApi);
		   		    
		    // Did we find any computers for this reclamation plan that have not received a user response? If so, decide what to do.
		    Boolean b = sendSubsequentNotifications(rec, dbApi);
		    
		    if (!a && !b)
		    {
		    	logMessage.append(String.format("The plan will be skipped because there is no work required.", reclamationPlanName));  
		    	writeToLog(LogLevel.INFO, 
						logMessage.toString(), 
						logDetails,
						logger);
		    }
	    }		    
		
		Sleep(2000L);
		writeToLog(LogLevel.INFO, 
				"Completed checking Reclamation plans for pending work.", 
				new ReclamationLoggerDetails(null),
				logger);
	}
	
	
	/**
     * Did we find any computers for this reclamation plan that have never had an email sent?
     */	
	private Boolean sendInitialNotifications(final ReclamationPlan rec, DbApi dbApi) 
	{			
		List<Object[]> computers = rec.getInitialNotifcationComputers();
		Boolean workNeeded = false;
		
	    if (computers.size() > 0)
	    {
	    	workNeeded = true;
	    	
		    for (Object[] computer : computers) 
		    {
		    	this.logMessage = new StringBuilder(String.format("Check plan '%s' : ", 
		    			this.reclamationPlanName));
		    	
		    	Long reclamationComputer = (Long) computer[2];
		    	String primaryUserEmail = (computer[0] != null) ? computer[0].toString() : null;
		    	String primaryPersonEmail = (computer[5] != null) ? computer[5].toString() : null;
		    	String ownerUserEmail = (computer[6] != null) ? computer[6].toString() : null;
		    	String ownerPersonEmail = (computer[7] != null) ? computer[7].toString() : null;
		    	String email = selectEmail(primaryUserEmail, primaryPersonEmail, ownerUserEmail, ownerPersonEmail, dbApi);
		    	String computerName = computer[1].toString();
		    	String signatureName = computer[3].toString();
		    	Long sendAttempts = (Long) computer[4];
		    	
				// Send email
		    	try
		    	{
		    		if (email == null)
		    		{
		    			throw new Exception(String.format("No email was found for computer '%s' on attempt #%d.", 
		    					computerName,
		    					sendAttempts == null ? 1L : sendAttempts+1L));
		    		}
		    			
			    	rec.submitReclamationEmail(reclamationComputer, 
			    			computerName, 
			    			signatureName, 
			    			email, 
			    			1L);
			    	
			    	this.logMessage.append(String.format("Sent email #%d to %s for computer '%s'.", 
							sendAttempts == null ? 1L : sendAttempts+1L,
							email,
							computerName));
			    	
					writeToLog(LogLevel.INFO, 
							this.logMessage.toString(), 
							this.logDetails,
					    	logger);
		    	}
		    	catch(Exception e)
		    	{
		    		logMessage.append(String.format("Reclamation email #%d to %s for computer '%s' has failed. Error: %s",
							sendAttempts == null ? 1L : sendAttempts+1L,
							email,
							computerName,
							e.getMessage()));
		    		
					writeToLog(LogLevel.ERROR, 
							this.logMessage.toString(), 
							this.logDetails,
					    	logger);
		    	}			    	
		    }
	    }
	    return workNeeded;
	}

	
	/**
     * Did we find any computers for this reclamation plan that require a subsequent notification
     * due to user's lack of response?
     */		
	private Boolean sendSubsequentNotifications(final ReclamationPlan rec, DbApi dbApi) 
	{
		List<Object[]> computers = rec.getSubsequentNotifcationComputers();
		Boolean workNeeded = false;
		
    	this.logMessage = new StringBuilder(String.format("Check plan '%s' : ", 
    			this.reclamationPlanName));
		
	    if (computers.size() > 0)
	    {		    	    	
		    for (Object[] computer : computers) 
		    {				    	
		    	Long reclamationComputer = (Long) computer[2];		    	
		    	String primaryUserEmail = (computer[0] != null) ? computer[0].toString() : null;
		    	String primaryPersonEmail = (computer[5] != null) ? computer[5].toString() : null;
		    	String ownerUserEmail = (computer[6] != null) ? computer[6].toString() : null;
		    	String ownerPersonEmail = (computer[7] != null) ? computer[7].toString() : null;
		    	String email = selectEmail(primaryUserEmail, primaryPersonEmail, ownerUserEmail, ownerPersonEmail, dbApi);
		    	String computerName = computer[1].toString();
		    	String signatureName = computer[3].toString();
		    	
				// Do we need to re-send email a second or third time?
				if (rec.resendEmailRequired(reclamationComputer))
				{
			    	workNeeded = true;
			    	
					// Re-send original email
			    	try
			    	{
			    		if (email == null)
			    		{
			    			throw new Exception(String.format("No email was found for computer '%s' on subsequent send attempt.", 
			    					computerName));
			    		}			    		
			    		
						rec.submitReclamationEmail(reclamationComputer, 
								computerName, 
								signatureName, 
								email, 
								-1L);
				        
						this.logMessage.append(String.format("Sending subsequent email notification to '%s' for computer '%s'.", 
						    	email,
						    	computerName));
						
						writeToLog(LogLevel.INFO, 
								this.logMessage.toString(), 
								this.logDetails,
						    	logger);	
			    	}
			    	catch(Exception e)
			    	{
			    		this.logMessage.append(String.format("Sending subsequent email notification to '%s' for computer '%s' has failed. Error:", 
						    	email,
						    	computerName,
						    	e.getMessage()));
			    		
						writeToLog(LogLevel.INFO, 
								this.logMessage.toString(), 
								this.logDetails,
				    			logger);	
			    	}									
				}
			}
	    }	
	    return workNeeded;
	 }
	

	/**
     * Return unsubmitted reclamation plans. (not submitted to SCCM)
     */	
    private List<Object[]> getUnsumbittedReclamationPlans(final DbApi dbApi) 
    {
		SelectQuery query = Query.select(Arrays.asList(
				Query.column(Db.Reclamation.COLUMN_ID),
				Query.column(Db.Reclamation.COLUMN_NAME),
				Query.column(Db.Reclamation.COLUMN_ENABLED)));
		query.from(Db.Reclamation.TABLE_NAME);	
		query.where(Criterion.EQ(Db.Reclamation.COLUMN_APPROVED, false));
		
		return dbApi.getQueryExecutor().executeLA(query);
	}


	/**
     * Check to see if reclamation is enabled. (reclamationConfig.enabled)
     */	
	private boolean reclamationEnabled(final DbApi dbApi)
	{
		SelectQuery query = Query.select(count());
		query.from(Db.ReclamationConfig.TABLE_NAME);	
		query.where(Criterion.AND(
				Criterion.EQ(Db.ReclamationConfig.COLUMN_ENABLED, true),
				Criterion.EQ(Db.ReclamationConfig.COLUMN_NAME, DEFAULT_RECLAMATION_CONFIG)));
		Long recCount = dbApi.getQueryExecutor().execute1(query);
		return recCount == 0 ? false : true;
	}
	
	
	/**
     * Determine which email to use. There is a hierarchy used here. First we check in the config table if use_owner
     * and use_user are set. If both are true, then we look for the emails in this order: 
     * Owner Person email (cmn_person)
     * Owner User email (cmn_user)
     * PrimaryUser Person email (cmn_person)
     * PrimarUser User email (cmn_user)
     * See US3283 for more details.
     */		
	private String selectEmail(String primaryUserEmail, String primaryPersonEmail, String ownerUserEmail, String ownerPersonEmail, DbApi dbApi)
	{
		String email = null;
		Boolean useOwner = false;
		Boolean usePrimaryUser = false;
		
		SelectQuery query = Query.select(Arrays.asList(
				Query.column(Db.ReclamationConfig.COLUMN_USE_OWNER),
				Query.column(Db.ReclamationConfig.COLUMN_USE_USER)));
		query.from(Db.ReclamationConfig.TABLE_NAME);	
		query.where(Criterion.EQ(Db.ReclamationConfig.COLUMN_NAME, DEFAULT_RECLAMATION_CONFIG));
		
		List<Object[]> recs = dbApi.getQueryExecutor().executeLA(query);		
	    if (recs.size() > 0)
	    {   	
	    	useOwner = (recs.get(0)[0] != null) ? (Boolean) recs.get(0)[0] : false;
	    	usePrimaryUser  = (recs.get(0)[1]) != null ? (Boolean) recs.get(0)[1] : false;	    	
	    
		    // When available, first return: owner person email, owner user email, primary_user owner email, primary user_user email
		    if (useOwner)
		    	email = (ownerPersonEmail != null) ? ownerPersonEmail : ownerUserEmail;
		    
		    if (usePrimaryUser && email == null)
		    	email = (primaryPersonEmail != null) ? primaryPersonEmail : primaryUserEmail;
	    }
	    
	    return email;
	}
}
