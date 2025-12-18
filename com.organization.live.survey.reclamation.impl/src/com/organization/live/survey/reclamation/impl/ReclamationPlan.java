package com.organization.live.survey.reclamation.impl;

import static com.organization.live.db.query.Query.count;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.entity.EntityDesc;
import com.organization.live.db.query.Order;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.notification.transport.service.TransportService;
import com.organization.live.survey.reclamation.component.ReclamationComponent;
import com.organization.live.survey.reclamation.component.ReclamationLoggerDetails;
import com.organization.live.survey.reclamation.db.Db;

/**
 * ReclamationPlan class. Represents an individual reclamation record in the Reclamation table.
 * 
 * @author Danny Carvajal
 */
public class ReclamationPlan extends BaseReclamationPlan implements ReclamationComponent
{
	protected String adminEmail;
	protected boolean approved;
	protected Long approvedBy;
	protected String description;		
	protected Long emailConfig;
	protected Long emailExpirationDays;
	protected Long emailGroomDays;
	protected Long id;
	protected String name;
	protected Long sendEmailWaitDays;
	protected Long sccmConfig;
	protected String sccmCollectionDesc;		
	protected String sccmCollectionName;		
	protected String sccmDeploymentDesc;
	protected String sccmDeploymentName;
	protected Long sccmPackage;
	protected Long sccmProgram;
	protected String signatureGuid;		
	protected String signatureName;
	protected Date startTime;
	protected String emailTemplateSubject;
	protected String emailTemplateBody;
	protected Long sccmPlatformId;
	
	protected DbApi dbApi;
	protected Logger logger;
	protected ReclamationLoggerDetails logDetails;
	protected TransportService transportService;
	
	
    /**
     * Constructor
     */	
	public ReclamationPlan(final Long reclamationPlanID, 
			final DbApi dbApi,
			TransportService transportService,
			Logger logger,
			ReclamationLoggerDetails logDetails)
	{
		this.dbApi = dbApi;
		this.transportService = transportService;
		
		// Populate reclamation record
		EntityDesc reclamation = new EntityDesc(Db.Reclamation.TABLE_NAME);
		Iterator<Entity> reclamations = this.dbApi.getEntityManager().readLazily(reclamation, 
				Criterion.EQ(Db.Reclamation.COLUMN_ID, reclamationPlanID));
		
		if (reclamations.hasNext()) 
		{
			Entity rec = reclamations.next();
			adminEmail = rec.getString(Db.Reclamation.COLUMN_ADMIN_EMAIL);
			approved = rec.getBoolean(Db.Reclamation.COLUMN_APPROVED);
			approvedBy = rec.getLong(Db.Reclamation.COLUMN_APPROVED_BY);
			description = rec.getString(Db.Reclamation.COLUMN_DESC);		
			emailConfig = rec.getLong(Db.Reclamation.COLUMN_EMAIL_CONFIG);
			emailExpirationDays = rec.getLong(Db.Reclamation.COLUMN_EMAIL_EXPIRATION_DAYS);
			emailGroomDays = rec.getLong(Db.Reclamation.COLUMN_EMAIL_GROOM_DAYS);
			id = reclamationPlanID;
			name = rec.getString(Db.Reclamation.COLUMN_NAME);
			sendEmailWaitDays = rec.getLong(Db.Reclamation.COLUMN_SEND_EMAIL_WAIT_DAYS);
			sccmConfig = rec.getLong(Db.Reclamation.COLUMN_SCCM_CONFIG_ID);
			sccmCollectionDesc = rec.getString(Db.Reclamation.COLUMN_SCCM_COLLECTION_DESC);		
			sccmCollectionName = rec.getString(Db.Reclamation.COLUMN_SCCM_COLLECTION_NAME);		
			sccmDeploymentDesc = rec.getString(Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_DESC);
			sccmDeploymentName = rec.getString(Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_NAME);
			sccmPackage = rec.getLong(Db.Reclamation.COLUMN_SCCM_PACKAGE);
			sccmProgram = rec.getLong(Db.Reclamation.COLUMN_SCCM_PROGRAM);
			signatureGuid = rec.getString(Db.Reclamation.COLUMN_SIGNATURE_GUID);
			signatureName = rec.getString(Db.Reclamation.COLUMN_SIGNATURE_NAME);
			startTime = rec.getDate(Db.Reclamation.COLUMN_SCCM_SENT_DATE);
			this.logger = logger;
			this.logDetails = logDetails;
			
			// Get reclamation email template
			SelectQuery query = Query.select(Arrays.asList(
					Query.column("re." + Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT),
					Query.column("re." + Db.ReclamationEmailConfig.COLUMN_EMAIL_BODY)));
			query.from(Db.Reclamation.TABLE_NAME, "r");
			query.join(Db.ReclamationEmailConfig.TABLE_NAME, "re", "re." + Db.ReclamationEmailConfig.COLUMN_ID, "r." + Db.Reclamation.COLUMN_EMAIL_CONFIG);
			query.where(Criterion.EQ("r." + Db.Reclamation.COLUMN_ID, this.id));
			
			List<Object[]> emailItems = dbApi.getQueryExecutor().executeLA(query);
			emailTemplateSubject = emailItems.get(0)[0].toString();
			emailTemplateBody = emailItems.get(0)[1].toString();		
			
			// Get Platform ID
			query = Query.select(Query.column(Db.ManagementPlatform.COLUMN_ID));
			query.from(Db.ManagementPlatform.TABLE_NAME);
			query.where(Criterion.EQ(Db.ManagementPlatform.COLUMN_NAME, SCCM_PLATFORM));
			this.sccmPlatformId = dbApi.getQueryExecutor().execute1(query);
		}
	}
		
	
    /**
     * Query for SCCM managed reclamation computers where there has been no email has ever been sent. 
	 * Also return package name so we can submit package name to the email. Also, make sure this
	 * package has not already been uninstalled and that user email exists.
     */		
	protected List<Object[]> getInitialNotifcationComputers()
	{
		SelectQuery query = prepareQuery();
		query.where(prepareWhereClause(true));  // <-- 0 send attempts
    	
	    return this.dbApi.getQueryExecutor().executeLA(query);		
	}


    /**
     * Query for SCCM Managed reclamation computers where there has been no email response and has a primary user. 
	 * Also return package name so we can submit package name to the email.  Also, make sure this
	 * package has not already been uninstalled and that user email exists.
     */			
	protected List<Object[]> getSubsequentNotifcationComputers()
	{
		SelectQuery query = prepareQuery();		
		query.where(prepareWhereClause(false));
    	
	    return this.dbApi.getQueryExecutor().executeLA(query);		
	}	


    /**
     * Re-use sql queries. Used in determining if there are notifications that need to be sent.
     */		
	private SelectQuery prepareQuery() 
	{
		SelectQuery query = Query.select(Arrays.asList(
				Query.column("u." + Db.User.COLUMN_EMAIL, "primary_user_email"),
				Query.column("c." + Db.Computer.COLUMN_NAME),
				Query.column("rc." + Db.ReclamationComputer.COLUMN_ID),
				Query.column("ws." + Db.SoftwarePackagesWindows.COLUMN_FRIENDLY_NAME),
				Query.column("rc." + Db.ReclamationComputer.COLUMN_EMAIL_SEND_ATTEMPTS),
				Query.column("per." + Db.Person.COLUMN_EMAIL, "primary_person_email"),
				Query.column("u2." + Db.User.COLUMN_EMAIL, "owner_user_email"),
				Query.column("p2." + Db.Person.COLUMN_EMAIL, "owner_person_email")));
		query.from(Db.ReclamationComputer.TABLE_NAME, "rc");
		query.join(Db.Computer.TABLE_NAME, "c", "c." + Db.Computer.COLUMN_ID, "rc." + Db.ReclamationComputer.COLUMN_COMPUTER);
		query.join(Db.ManagementID.TABLE_NAME, "m", "m." + Db.ManagementID.COLUMN_DEVICE, "c." + Db.Computer.COLUMN_ID);
		query.join(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementID.COLUMN_PLATFORM);		
		query.join(Db.SoftwarePackagesWindows.TABLE_NAME, "ws", "ws." + Db.SoftwarePackagesWindows.COLUMN_ID, "rc." + Db.ReclamationComputer.COLUMN_SIGNATURE_ID);
		query.leftJoin(Db.User.TABLE_NAME, "u", "u." + Db.User.COLUMN_ID, "c." + Db.Computer.COLUMN_PRIMARY_USER);
		query.leftJoin(Db.Person.TABLE_NAME, "per", "per.id", "u.person");
		query.leftJoin(Db.User.TABLE_NAME, "u2", "u2.id", "c.owner");
		query.leftJoin(Db.Person.TABLE_NAME, "p2", "p2.id", "u2.person");
		
		return query;
	}
	
	
    /**
     * Format the where clauses.
     */		
	private Criterion prepareWhereClause(final boolean firstEmail)
	{
		Criterion crit;
		
		if (firstEmail)
		{
			crit = Criterion.AND(
					Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, this.id),
					Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, this.sccmPlatformId),
					//Criterion.NE("u." + Db.User.COLUMN_EMAIL, null), // <-- check for email, otherwise we cannot send email!
					Criterion.NE("ws." + Db.SoftwarePackagesWindows.COLUMN_OPERATIONAL, false), // <-- make sure signature is not already uninstalled
					Criterion.AND(
								Criterion.OR(
										Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_EMAIL_SEND_ATTEMPTS, 0),        // <-- 0 send attempts
										Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_EMAIL_SEND_ATTEMPTS, null))));  // <-- 0 send attempts
		}
		else
		{
			crit = Criterion.AND(
					Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, this.id),
					Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, this.sccmPlatformId),
					//Criterion.NE("u." + Db.User.COLUMN_EMAIL, null), // <-- check for email, otherwise we cannot send email!
					Criterion.NE("ws." + Db.SoftwarePackagesWindows.COLUMN_OPERATIONAL, false), // <-- make sure signature is not already uninstalled
					Criterion.AND(
							Criterion.OR(
									Criterion.NE("rc." + Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, true), 
									Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, null))), // <-- 0 email confirmations
					Criterion.GT("rc." + Db.ReclamationComputer.COLUMN_EMAIL_SEND_ATTEMPTS, 0)); // <-- > 0 send attempts;
		}
		
		return crit;
	}
	
	
    /**
     * Do we need to re-send email to computer user? No need to check for empty email or if signature is already uninstalled.
     * That is handled in queries above.
     */		
	protected boolean resendEmailRequired(final Long reclamationComputerID)
	{
		SelectQuery query;
		boolean emailResponseNeeded = false;
		boolean userHasResponded = false;
			
		// Did we get a response first or second (etc.) email for this computer?
		query = Query.select(count());
		query.from(Db.ReclamationComputer.TABLE_NAME);
		query.where(Criterion.AND(
				Criterion.EQ(Db.ReclamationComputer.COLUMN_ID, reclamationComputerID),
				Criterion.EQ(Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, true)));
		
		final Long recCount = this.dbApi.getQueryExecutor().execute1(query);
		userHasResponded = recCount == 0 ? false : true;
		
		if (!userHasResponded)
		{
			query = Query.select(count());
			query.from(Db.ReclamationEmail.TABLE_NAME);
			query.where(Criterion.EQ(Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, reclamationComputerID));
			
			final Long emailsSent = this.dbApi.getQueryExecutor().execute1(query);

			// Have any computers reached max # of emails and haven't already had no response action set?
			query = Query.select("r." + Db.Reclamation.COLUMN_EMAIL_MAX_SEND_ATTEMPTS);
			query.from(Db.Reclamation.TABLE_NAME, "r");
			query.join(Db.ReclamationComputer.TABLE_NAME, "rc", "rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, "r." + Db.Reclamation.COLUMN_ID);
			query.where(Criterion.AND(
					Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_ID, reclamationComputerID),
					Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, null)));
			
			final Long maxAllowedSendAttempts = this.dbApi.getQueryExecutor().execute1(query);
			
			// Have we already sent the max # of emails specified by opz_reclamation.email_send_attempts?
			if (maxAllowedSendAttempts != null)
			{
				if (emailsSent.equals(maxAllowedSendAttempts))
				{
					// No need to send email as we reached max send attempts, however, what do we do with this computer? (check no_response_action)
					query = Query.select(Db.Reclamation.COLUMN_NO_RESPONSE_ACTION);
					query.from(Db.Reclamation.TABLE_NAME);
					query.where(Criterion.EQ(Db.Reclamation.COLUMN_ID, this.id));
					
					final Long noResponseAction = this.dbApi.getQueryExecutor().execute1(query);

					Entity rec = dbApi.getEntityManager().create(Db.ReclamationComputer.TABLE_NAME);
					rec = dbApi.getEntityManager().readEntity(Db.ReclamationComputer.TABLE_NAME, Criterion.EQ(Db.ReclamationComputer.COLUMN_ID, reclamationComputerID));
					rec.set(Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, noResponseAction == PLAN_NO_RESPONSE_ACTION_IDX_2 ? true : false);
					rec.save();
					
					writeToLog(LogLevel.INFO, 
							String.format("Emails sent exceeded max email send count limit. Setting no response action to '%s' for reclamation computer record id: '%d' ...",
							noResponseAction == PLAN_NO_RESPONSE_ACTION_IDX_2 ? "Uninstall" : "Do Not Uninstall",
							reclamationComputerID), 
							logDetails,
							logger);

					emailResponseNeeded = false;
				}
				// Has email time expired yet for the last email? If so, it's time for a new email
				else if(emailsSent < maxAllowedSendAttempts)
				{				
					// Grab time stamp of LAST email sent for given computer
					query = Query.select("re." + Db.ReclamationEmail.COLUMN_EMAIL_SENT_TIMESTAMP);
					query.from(Db.ReclamationEmail.TABLE_NAME, "re");
					query.join(Db.ReclamationComputer.TABLE_NAME, "rc", "rc." + Db.ReclamationComputer.COLUMN_ID, "re." + Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER);
					query.where(Criterion.AND(
							Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_ID, reclamationComputerID),
							Criterion.AND(
									Criterion.OR(
												Criterion.NE("rc." + Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, true), 
												Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, null))))); // <-- 0 email confirmations							
					query.orderBy("re." + Db.ReclamationEmail.COLUMN_EMAIL_SENT_TIMESTAMP, Order.DESC);
					query.limit(1L);
											
					Date emailSentTimeStamp = this.dbApi.getQueryExecutor().execute1(query);				
					emailResponseNeeded = getDiffDaysFromNow(emailSentTimeStamp) >= this.emailExpirationDays ? true : false;
				}
			}
		}
		
		return emailResponseNeeded;
	}
	
	
    /**
     * Can we proceed to send out email notifications? (i.e. check today's date vs email send result wait days. 
     */		
	protected boolean sendingEmailsAllowed(final Long reclamationPlanID)
	{
		SelectQuery query = Query.select(Arrays.asList(
				Query.column(Db.Reclamation.COLUMN_SEND_EMAIL_WAIT_DAYS),
				Query.column(Db.Reclamation.COLUMN_CREATED_ON)));
		query.from(Db.Reclamation.TABLE_NAME);
		query.where(Criterion.EQ(Db.Reclamation.COLUMN_ID, reclamationPlanID));
		
		final List<Object[]> reclamationDetails = this.dbApi.getQueryExecutor().executeLA(query);
		Long sendEmailWaitDays = (Long) reclamationDetails.get(0)[0];
		Date createdOn = (Date) reclamationDetails.get(0)[1];
		Long dateDiff = getDiffDaysFromNow(createdOn);
		
		return dateDiff >= sendEmailWaitDays ? true : false;
	}	
	
	
    /**
     * Send email to user and insert record into ReclamationEmail table to keep track of it. 
     */			
	protected void submitReclamationEmail(final Long reclamationComputerID, 
			final String computerName, 
			final String signatureName,
			final String emailAddress, 
			final Long contactAttempt) throws Exception
	{
		String securityToken = super.getRandomString(20);
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		Timestamp nowTimestamp = new Timestamp(now.getTime());
		Long attempt = contactAttempt;

		// Not first attempt, so which email attempt is this?
		if (attempt == -1)
		{
			SelectQuery query = Query.select(count());
			query.from(Db.ReclamationEmail.TABLE_NAME);
			query.where(Criterion.EQ(Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, reclamationComputerID));
			
			final Long recCount = this.dbApi.getQueryExecutor().execute1(query);
			attempt = recCount+1L;
		}
		
		// Send email
		final String to = emailAddress;
		final String from = this.adminEmail;
		ReclamationEmail email = new ReclamationEmail();
		email.setComputerName(computerName);
		email.setFrom(from);
		email.setMsg(this.emailTemplateBody);
		email.setSecurityToken(securityToken);
		email.setSubject(this.emailTemplateSubject);
		email.setSignatureName(signatureName);
		email.setTo(to);
		email.setReclamationComputerID(reclamationComputerID);
		ReclamationMessengerImpl reclamationMessengerImpl = new ReclamationMessengerImpl(this.dbApi, this.transportService, email);
		Long emailID = reclamationMessengerImpl.sendEmail();
        
        // Now insert a reclamation email record
        if (emailID != null)
        {
	    	Entity rec = dbApi.getEntityManager().create(Db.ReclamationEmail.TABLE_NAME);
			rec.set(Db.ReclamationEmail.COLUMN_CONTACT_ATTEMPT, attempt);
			rec.set(Db.ReclamationEmail.COLUMN_EMAIL_SENT_TIMESTAMP, nowTimestamp);
			rec.set(Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, reclamationComputerID);
			rec.set(Db.ReclamationEmail.COLUMN_SYSTEM_EMAIL, emailID);
			rec.set(Db.ReclamationEmail.COLUMN_TOKEN, securityToken);
			rec.save();
				
			// Now update Reclamation Computer record with an attempt
	    	rec = dbApi.getEntityManager().readEntity(Db.ReclamationComputer.TABLE_NAME, Criterion.EQ(Db.ReclamationComputer.COLUMN_ID, reclamationComputerID));
			rec.set(Db.ReclamationComputer.COLUMN_EMAIL_SEND_ATTEMPTS, attempt);
			rec.save();
        }
	}
}
