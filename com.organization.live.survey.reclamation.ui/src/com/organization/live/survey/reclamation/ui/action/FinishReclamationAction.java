package com.organization.live.survey.reclamation.ui.action;

import java.util.ArrayList;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.transaction.Transaction;
import com.organization.live.db.transaction.TransactionCallback;
import com.organization.live.db.transaction.TransactionDefinition;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.logger.LoggerService;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.ui.component.ReclamationLoggerDetails;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.widget.WidgetModel;
import com.organization.live.ui.service.ServiceLocator;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class FinishReclamationAction extends AbstractReclamationAction 
{
    protected LoggerFactory loggerFactory;
	
    /**
     * Setter for logger factory.
     * 
     * @param factory the factory to set
     */
    public void setLoggerFactory (final LoggerFactory factory) 
    {
        this.loggerFactory = factory;
    }	
		
    protected LoggerService getLoggerService (final DbApi dbApi) 
    {
        return loggerFactory.createLoggerService(dbApi);
    }    	
	
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{	
		DbApi dbApi = serviceLocator.getDbApi();
		saveReclamationData(dbApi, model, serviceLocator);		
		return model;
	}    
    
	private void saveReclamationData(final DbApi dbApi, final ComponentModel model, final ServiceLocator serviceLocator) 
	{ 
		final TransactionDefinition definition = new TransactionDefinition(
				TransactionDefinition.PROPAGATION_NESTED);
		
		final TransactionCallback<Void> callback = new TransactionCallback<Void>() 
		{
			@Override
			public Void doInTransaction(final Transaction tx) throws Exception 
			{			
		        LoggerService loggerService = getLoggerService(dbApi);
		        loggerService.getLogger(ReclamationPlanComponent.LOG_PREFIX, null, ReclamationPlanComponent.LOG_DESCRIPTION).keepFor(LogLevel.DEBUG, 1);
		        Logger logger = getLogger(getName(), loggerService);				
		        ReclamationLoggerDetails logDetails = null;
				
		        Long emailRecID = null;
		    	Long sccm_config_id =  null;
		    	
				try 
				{					
					// Get server configuration id from scene 2
				    Long siteServer = getSiteServer(model);
			    	String siteCode = getSiteCode(model);
			    	
			    	// Update sccm config table
			    	Entity sccmConfig = dbApi.getEntityManager().readEntity(Db.SCCMServerConfig.TABLE_NAME, 
			    			Criterion.AND(Criterion.EQ(Db.SCCMServerConfig.COLUMN_SCCM_SITE_SERVER, siteServer), 
			    			Criterion.EQ(Db.SCCMServerConfig.COLUMN_SCCM_SITE_CODE, siteCode)));
			    	
			    	if (sccmConfig == null)
			    		sccmConfig = dbApi.getEntityManager().create(Db.SCCMServerConfig.TABLE_NAME);
			    	
			    	sccmConfig.set(Db.SCCMServerConfig.COLUMN_SCCM_SITE_SERVER, siteServer);
			    	sccmConfig.set(Db.SCCMServerConfig.COLUMN_SCCM_SITE_CODE, siteCode);
			    	sccmConfig.save();
			    	sccm_config_id = sccmConfig.getId();
					    				    	
			    	// Save email text we are sending out with this reclamation object		    	
		    		Entity emailRec = dbApi.getEntityManager().readEntity(Db.ReclamationEmailConfig.TABLE_NAME, Criterion.AND(
							Criterion.EQ(Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT, getEmailSubject(model)), 
							Criterion.EQ(Db.ReclamationEmailConfig.COLUMN_EMAIL_BODY, getEmailBody(model))));
		    				
		    		if (emailRec == null) 
		    		{
				    	emailRec = dbApi.getEntityManager().create(Db.ReclamationEmailConfig.TABLE_NAME);
				    	emailRec.set(Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT, getEmailSubject(model));
				    	emailRec.set(Db.ReclamationEmailConfig.COLUMN_EMAIL_BODY, getEmailBody(model));
				    	emailRec.set(Db.ReclamationEmailConfig.COLUMN_TEMPLATE, getSaveEmailProperty(model));
				    	emailRec.save();
		    		}
			    	emailRecID = emailRec.getId();
			    	
					// Update other reclamation records
			    	Entity planRec = dbApi.getEntityManager().create(Db.Reclamation.TABLE_NAME);
			    	planRec.set(Db.Reclamation.COLUMN_EMAIL_CONFIG, emailRecID);
			    	planRec.set(Db.Reclamation.COLUMN_SCCM_CONFIG_ID, sccm_config_id);
			    	planRec.set(Db.Reclamation.COLUMN_SCCM_COLLECTION_NAME, getCollectionName(model));
			    	planRec.set(Db.Reclamation.COLUMN_SCCM_COLLECTION_DESC, getCollectionDescription(model));
			    	
			    	Boolean allowAdverts = getAllowAdvertisements(model);
			    	planRec.set(Db.Reclamation.COLUMN_ALLOW_ADVERTISEMENTS, allowAdverts);
			    	if (allowAdverts)
			    	{
			    		planRec.set(Db.Reclamation.COLUMN_REQUIRE_ADVERTISEMENTS, getRequireAdvertisements(model));
			    		planRec.set(Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_NAME, getDeploymentName(model));
			    		planRec.set(Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_DESC, getDeploymentDescription(model));
			    	}
			    	
			    	if (getRequirePackage(model))
			    	{
			    		planRec.set(Db.Reclamation.COLUMN_SCCM_PACKAGE, getManagementPackageId(model, serviceLocator));
			    		planRec.set(Db.Reclamation.COLUMN_SCCM_PROGRAM, getProgram(model));
			    	}
			    	else
			    	{
			    		planRec.set(Db.Reclamation.COLUMN_SCCM_ALLOW_RESTARTS, allowAdverts ? getAllowRestarts(model) : false); // no need to set if adverts ! allowed
			    	}
			    	
			    	planRec.set(Db.Reclamation.COLUMN_NAME, getReclamationPlanName(model));
			    	planRec.set(Db.Reclamation.COLUMN_DESC, getPlanDescription(model));
			    	planRec.set(Db.Reclamation.COLUMN_ADMIN_EMAIL, getPlanEmail(model));
			    	planRec.set(Db.Reclamation.COLUMN_EMAIL_EXPIRATION_DAYS, getEmailExpirationDays(model));
			    	planRec.set(Db.Reclamation.COLUMN_EMAIL_MAX_SEND_ATTEMPTS, getEmailSendAttempts(model));
			    	planRec.set(Db.Reclamation.COLUMN_EMAIL_GROOM_DAYS, getGroomEmailDays(model));   	
			    	planRec.set(Db.Reclamation.COLUMN_SEND_EMAIL_WAIT_DAYS, getSendEmailWaitDays(model));
			    	planRec.set(Db.Reclamation.COLUMN_SIGNATURE_GUID, getSignatureGUID(model));
			    	planRec.set(Db.Reclamation.COLUMN_SIGNATURE_NAME, getSignatureName(model));
			    	planRec.set(Db.Reclamation.COLUMN_SIGNATURE_SPV, getSignatureSPV(model));
			    	planRec.set(Db.Reclamation.COLUMN_APPROVED, false);
			    	planRec.set(Db.Reclamation.COLUMN_ENABLED, true);
			    	planRec.set(Db.Reclamation.COLUMN_NO_RESPONSE_ACTION, getNoResponseAction(model));
			    	planRec.save();
			    	Long planRecID = planRec.getId();		 
			    	logDetails = new ReclamationLoggerDetails(planRecID);
			  	
			    	// Save computers that require the sccm action
			    	List<List<Long>> computers = getComputerIDs(model, serviceLocator);
			    	for (final List<Long> computer : computers) 
			    	{
				    	Entity detailRec = dbApi.getEntityManager().create(Db.ReclamationComputer.TABLE_NAME);
			    		detailRec.set(Db.ReclamationComputer.COLUMN_COMPUTER, computer.get(0));
			    		detailRec.set(Db.ReclamationComputer.COLUMN_SIGNATURE_ID, computer.get(1));
			    		detailRec.set(Db.ReclamationComputer.COLUMN_RECLAMATION_ID, planRecID);
			    		detailRec.set(Db.ReclamationComputer.COLUMN_EMAIL_CONFIRMED, false);
			    		detailRec.save();
			    	}
			    	
			    	// Log success
					logger.info(String.format("Reclamation Plan: '%s' was created successfully.", 
							getReclamationPlanName(model)), logDetails);					
				}
				catch(Exception e) 
				{
					logger.error(String.format("Could not save Reclamation Plan: '%s'. Exception: %s", 
							getReclamationPlanName(model), 
							e.getMessage()), logDetails);
					
					throw new Exception(String.format("Could not save Reclamation Plan: '%s'. Exception: %s", 
							getReclamationPlanName(model), 
							e.getMessage()));
				}
		    	return null;
			}
			
	        private Logger getLogger(final String type, final LoggerService loggerService) 
	        {
	            return loggerService.getLogger(ReclamationPlanComponent.LOG_PREFIX + type, 
	            		ReclamationPlanComponent.COMPONENT_NAME, 
	            		ReclamationPlanComponent.LOG_PREFIX + type );
	        }	
	        
	        // Get the selected computers from source grid 
	        private List<List<Long>> getComputerIDs(final ComponentModel model, final ServiceLocator serviceLocator) 
	        {
	        	final WidgetModel<Data, ?> sourceModel = model.getParameter(SOURCE_MODEL); 
	            List<Data> signatures = sourceModel.getSelected();
	    		List<List<Long>> selectedSignatures = new ArrayList<List<Long>>();
	    		
	    		for (Data signature : signatures)
	    		{			        	
	    			Long computerID = signature.get("installed_on");
	    			Long signatureID = signature.getId();
	    			List<Long> combo = new ArrayList<Long>();
	    			combo.add(computerID);
	    			combo.add(signatureID);
	    			if (computerID != null) selectedSignatures.add(combo);
	    			
	    		}
	    		return selectedSignatures;    	
	        }	        
		};
		
		dbApi.getThreadContext().transaction("Reclamation Save Action", definition, 
				callback);
    }
}
