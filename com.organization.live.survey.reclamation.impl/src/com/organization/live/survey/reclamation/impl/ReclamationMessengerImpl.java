package com.organization.live.survey.reclamation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.meta.Fields;
import com.organization.live.db.meta.Tables;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.notification.transport.service.Email;
import com.organization.live.notification.transport.service.MessageAttachment;
import com.organization.live.notification.transport.service.Transport;
import com.organization.live.notification.transport.service.TransportService;
import com.organization.live.notification.transport.service.exception.SendException;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.messenger.ReclamationMessenger;

/**
 * The ReclamationImpl class is used to send an email. This is a service.
 *
 * @author Danny Carvajal
 *
 */
public class ReclamationMessengerImpl implements ReclamationMessenger
{
    private TransportService transportService = null;
	private static Logger logger = LoggerFactory.getLogger(ReclamationMessengerImpl.class);

	private static final String CONFIG_LOGO = "logo";
    private static final String CONTENT = "{CONTENT}";
    private static final String HOST_ADDRESS = "{HOST_ADDRESS}";
    private static final String TEMPLATE_FILE = "/template.html";
	private ReclamationEmail email;
	private DbApi dbApi;	
    
	public ReclamationMessengerImpl(DbApi dbApi, TransportService transportService, ReclamationEmail email)
	{
		this.dbApi = dbApi;
		this.transportService = transportService;
		this.email = email;
	}
    
	public void setTransportService(final TransportService transportService)
	{
		this.transportService = transportService;
	}		
	
	public void setDbApi(final DbApi dbApi)
	{
		this.dbApi = dbApi;
	}	
	
	
    /** 
     * Send reclamation email
     * 
     * @param fromAddress - the address to send from
     * @param toAddress - the address to send to
     * @param subject - the email subject
     * @param msg - the email body
     * @param dbApi
     */
	@Override
	public Long sendEmail() 
	{		
		SelectQuery query = Query.select(Query.column(Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_GROOM_DAYS));
		query.from(Db.ReclamationConfig.TABLE_NAME);
		Long emailGroomDays = this.dbApi.getQueryExecutor().execute1(query); 
		
		Long emailId = null;
		Email message = new Email(email.getFrom(), 
				this.email.getTo(), 
				this.email.getSubject(),
				getBody(), 
				null, 
				emailGroomDays);
        
        try 
        {
            final Transport transport = transportService.getTransport(dbApi);
            MessageAttachment attachment = new MessageAttachment("logo", getLogo(), true);
            attachment.setContentType("image/png");
            attachment.setDescription("Logo.png");
            message.addAttachment(attachment);            
        	emailId = transport.saveMessage(message);
        	logger.info("Trying to send Reclamation email with id = " + emailId);
            transport.send(emailId);
            logger.info("Reclamation email with id = " + emailId + " sent successfully");
        } 
        catch (final SendException e) 
        {
            logger.error("Reclamation email with id = " + emailId + " sent failed");
        }
        
        return emailId;
	}
	
	
	/**
	 * Reads the template email and replaces with appropriate content.
	 * 
	 * @param addresses the list of addresses
	 * @return the address list
	 */	
	private String getBody() 
    {
        final InputStream tmpl = getClass().getResourceAsStream(TEMPLATE_FILE);
        try 
        {
            String mainSection = this.email.getMsg();          
            String template = IOUtils.toString(tmpl);
            template = template.replace(CONTENT, mainSection);
            template = template.replace(HOST_ADDRESS, getResponseUrl());         
            return template;
        } 
        catch (final IOException e) 
        {
        	logger.error("Can't read Reclamation email template file -> " + e.getMessage());
        } 
        finally 
        {
            IOUtils.closeQuietly(tmpl);
        }
        return "";
    }	
	
	
	/**
	 * Returns the Asset Vision instance name url and formats url like:
	 * https://instancename/reclamation/?rt=y&t=x3344&rci=1
	 * rt = response type of yes or no
	 * t = security token
	 * rci = reclamation computer id
	 * 
	 * @param dbApi - DbApi object
	 * @return Asset Vision instance name
	 */	
	private String getResponseUrl() 
    {
        List<Entity> versions = dbApi.getEntityManager().readAll(Tables.SYS_VERSION);
        StringBuilder url = new StringBuilder();
        if (versions.size() > 0) {
            Entity version = versions.get(0);
            url.append("<br/><br/><a href=\"https://");
            url.append(version.getString(Fields.INSTANCE_NAME));
            url.append("/reclamation/?rt=y&t=");
            url.append(this.email.getSecurityToken());
            url.append("&rci=");
            url.append(this.email.getReclamationComputerID());
            url.append("\">Yes, please remove ");
            url.append(this.email.getSignatureName());
            url.append(" on ");
            url.append(this.email.getComputerName());
            url.append(".</a><br><br/><a href=\"https://");
            url.append(version.getString(Fields.INSTANCE_NAME));
            url.append("/reclamation/?rt=n&t=");
            url.append(this.email.getSecurityToken());
            url.append("&rci=");
            url.append(this.email.getReclamationComputerID());
            url.append("\">No, please do NOT remove ");
            url.append(this.email.getSignatureName());
            url.append(" from ");
            url.append(this.email.getComputerName());
            url.append(".</a><br>");
            return url.toString();
        }
        return null;
    }
	
	
	/**
	 * Returns the referenced logo image for use in email.
	 * @param none
	 * @return byte[] of logo
	 */		
    private byte[] getLogo() 
    {
    	// First read in logo from reclamation config table
    	List<Entity> reclamationConfig = dbApi.getEntityManager().readAll(Db.ReclamationConfig.TABLE_NAME);
    	Entity config = reclamationConfig.get(0);
    	byte[] logo = config.getBinary(CONFIG_LOGO);
    	
    	// If we found no logo (i.e. user has not uploaded one) then use the one from email dashboard config table
    	if (logo == null)
    	{    	
	    	List<Entity> emailConfigs = dbApi.getEntityManager().readAll(Db.UIDashboardEmailConfig.TABLE_NAME);
	        if (emailConfigs.size() <= 0) 
	        {
	            throw new RuntimeException(String.format("%s: Could not read logo from email configs.",
	            		getClass().getSimpleName()));
	        }
	        
	        config = emailConfigs.get(0);
	        logo = config.getBinary(CONFIG_LOGO);
    	}
    	
        return logo;
    }
}
