package com.organization.live.survey.reclamation.ui.action;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.List;

import com.organization.live.converter.IpConverter;
import com.organization.live.db.Criterion;
import com.organization.live.db.query.HAQueryExecutor;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.widget.WidgetModel;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.service.action.common.AbstractAction;
/**
 * 
 * @author Danny Carvajal
 *
 */
public abstract class AbstractReclamationAction extends AbstractAction implements ReclamationPlanComponent 
{   
   
    @Override
    public String getName() 
    {
        return getClass().getSimpleName();
    }

    @Override
    public ExecutionSource getExecutionSource() 
    {
        return wizardExecutionSource;
    }

	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
		return model;
	}
    
    protected String getSiteServerIp(final ComponentModel model) 
    {
    	String ip = model.get(SCENE_CONNECTION_FORM).getDataItem().get(SCCM_SERVER).toString();
		final InetAddress hostAddress = IpConverter.long2InetAddress(IpConverter.IpAddress2Long(ip));		
        return hostAddress.toString().replace("/", "");
    }	

    protected Long getSiteServer(final ComponentModel model) 
    {
    	return model.get(SCENE_CONNECTION_FORM).getDataItem().get(SCCM_SERVER);
    }	    
    
    protected String getSiteCode(final ComponentModel model) 
    {
        return model.get(SCENE_CONNECTION_FORM).getDataItem().get(SITE_CODE);
    }      
       
    protected String getCollectionName(final ComponentModel model) 
    {
        return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(COLLECTION_NAME);
    }  
    
    protected String getCollectionDescription(final ComponentModel model) 
    {
        return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(COLLECTION_DESC);
    }    
    
    protected String getDeploymentName(final ComponentModel model) 
    {
        return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(DEPLOYMENT_NAME);
    }
    
    protected String getDeploymentDescription(final ComponentModel model) 
    {
        return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(DEPLOYMENT_DESC);
    }    

    protected String getPackage(final ComponentModel model) 
    {
        return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(PACKAGE_NAME);
    }	
    
    protected String getSignatureGUID(final ComponentModel model) 
    {
        return model.get(SCENE_SELECT_COMPUTERS_FORM).getDataItem().get(SIGNATURE_GUID);
    }	
        
    protected String getSignatureName(final ComponentModel model) 
    {
        return model.get(SCENE_SELECT_COMPUTERS_FORM).getDataItem().get(SIGNATURE_NAME);
    }	     

    protected String getSignatureSPV(final ComponentModel model) 
    {
        return model.get(SCENE_SELECT_COMPUTERS_FORM).getDataItem().get(SIGNATURE_SPV);
    }	   
    
    protected Boolean getAllowAdvertisements(final ComponentModel model) 
    {
    	Boolean allowAdverts = model.get(ReclamationPlanComponent.SCENE_DEPLOYMENTS_FORM).getDataItem().get(ReclamationPlanComponent.PLAN_ALLOW_ADVERTISEMENTS);
    	return (allowAdverts == null || allowAdverts == false) ? false : true; 
    } 
    
    protected Boolean getRequireAdvertisements(final ComponentModel model) 
    {
    	Boolean requireAdverts = model.get(ReclamationPlanComponent.SCENE_DEPLOYMENTS_FORM).getDataItem().get(ReclamationPlanComponent.PLAN_REQUIRE_ADVERTISEMENTS);
    	return (requireAdverts == null || requireAdverts == false) ? false : true; 
    }     
    
    protected Long getManagementPackageId(final ComponentModel model, final ServiceLocator serviceLocator) 
    {
    	String packageName = model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(PACKAGE_NAME);

		SelectQuery query = Query.select(
			Query.column("m." + Db.ManagementPackage.COLUMN_ID));
        	query.from(Db.ManagementPackage.TABLE_NAME, "m");
        	query.join(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementPackage.COLUMN_PLATFORM);
        	query.where(Criterion.AND(Criterion.EQ("m." + Db.ManagementPackage.COLUMN_NAME, packageName), 
        			Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, getSCCMPlatformId(serviceLocator))));
        	
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);
    }
               
    protected Long getSCCMPlatformId(final ServiceLocator serviceLocator)
    {
		// Get Platform ID
		SelectQuery query = Query.select(Query.column(Db.ManagementPlatform.COLUMN_ID));
			query.from(Db.ManagementPlatform.TABLE_NAME);
			query.where(Criterion.EQ(Db.ManagementPlatform.COLUMN_NAME, ReclamationPlanComponent.SCCM_PLATFORM));
			
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);
    }
    
    protected Long getProgram(final ComponentModel model) 
    {
    	String program = model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(PROGRAM_NAME).toString();
    	if (! isNumeric(program))
    		return 0L;
    	else
    		return model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(PROGRAM_NAME);
    }
    
    protected Boolean getAllowRestarts(final ComponentModel model) 
    {
    	Boolean allowRestarts = model.get(ReclamationPlanComponent.SCENE_DEPLOYMENTS_FORM).getDataItem().get(ReclamationPlanComponent.PLAN_ALLOW_RESTARTS);
    	return (allowRestarts == null || allowRestarts == false) ? false : true; 
    }  
    
    protected boolean isNumeric(String s)
    {
    	return s.matches("\\d+");
    }
    
    protected String getProgram(final Long programID, final ComponentModel model, ServiceLocator serviceLocator) 
    {
    	if (programID == 0L)
    		return "Program will be generated...";

		SelectQuery query = Query.select(
			Query.column(Db.ManagementProgram.COLUMN_NAME));
        	query.from(Db.ManagementProgram.TABLE_NAME);
        	query.where(Criterion.EQ(Db.ManagementProgram.COLUMN_ID, programID));
        	
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);
    }    
    
    protected String getEmailSubject(final ComponentModel model) 
    {
        return model.get(SCENE_EMAIL_FORM).getDataItem().get(EMAIL_SUBJECT);
    } 
   
    protected String getEmailBody(final ComponentModel model) 
    {
    	String body = model.get(SCENE_EMAIL_FORM).getDataItem().get(EMAIL_BODY);
        body = body.replaceAll("(\r\n|\n)", "<br />");
        return body;
    } 

    protected Boolean getSaveEmailProperty(final ComponentModel model) 
    {
        Boolean saveTemplate = model.get(SCENE_EMAIL_FORM).getDataItem().get(EMAIL_SAVE_TEMPLATE);
        return (saveTemplate == null || saveTemplate == false) ? false : true;
    } 
    
    protected BigDecimal getProductCost(final ComponentModel model) 
    {
    	return model.get(SCENE_SELECT_COMPUTERS_FORM).getDataItem().get(PRODUCT_COST);
    }
    
    protected String getReclamationPlanName(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_NAME);
    }
    
    protected String getPlanDescription(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_DESC);
    }    
	
    protected String getPlanEmail(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_EMAIL);
    }
    
	/**
	 * 
	 * Read in a reclamation config table default value.
	 *
	 */
	protected Object getProperty(Integer index, final ServiceLocator serviceLocator) 
	{
	    HAQueryExecutor queryExecutor = serviceLocator.getDbApi().getQueryExecutor();
	    
	    String colName = null;
	    switch (index) {
		    case 0 : colName = Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_EXPIRATION_DAYS;
		    	break;
		    case 1 : colName = Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_GROOM_DAYS;
		    	break;
		    case 2 : colName = Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_SEND_WAIT_DAYS;
		    	break; 
		    case 3 : colName = Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_MAX_SEND_ATTEMPTS;
		    	break;
		    case 4 : colName = Db.ReclamationConfig.COLUMN_DEFAULT_ALLOW_RESTARTS;
	    		break;
	    }
	    
	    Object property = queryExecutor.execute1(Query.select(colName)
				.from(Db.ReclamationConfig.TABLE_NAME));
		
		return property;
	}
    
    protected Long getEmailExpirationDays(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_EMAIL_EXP_DAYS);
    }    
    
    protected Long getGroomEmailDays(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_EMAIL_KEEP_DAYS);
    }    

    protected Long getEmailSendAttempts(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_EMAIL_SEND_ATTEMPTS);
    } 
    
    protected Long getSendEmailWaitDays(final ComponentModel model) 
    {
        return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_EMAIL_SEND_WAIT_DAYS);
    }     
    
    protected int getNoResponseAction(final ComponentModel model) 
    {
    	String actionType = model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_NO_RESPONSE_ACTION);
    	int actionTypeIdx = (! PLAN_NO_RESPONSE_ACTION_2.equals(actionType)) ? PLAN_NO_RESPONSE_ACTION_IDX_1 : PLAN_NO_RESPONSE_ACTION_IDX_2;
        return actionTypeIdx;
    }    
    
    protected String getNoResponseActionText(final ComponentModel model) 
    {
    	return model.get(SCENE_CONFIGURATION_FORM).getDataItem().get(PLAN_NO_RESPONSE_ACTION);
    }   
    
    // Get the total selected computers from grid in wizard scene 1
    protected Integer getTotalMachines(final ComponentModel model) 
    {
    	final WidgetModel<Data, ?> sourceModel = model.getParameter(SOURCE_MODEL);
        List<Data> computers = sourceModel.getSelected();
        return computers.size();
    }
       
    // The flag that determines if we have a non-MSI package that requires that user select package/program from SCCM.
    protected Boolean getRequirePackage(final ComponentModel model)
    {
    	Boolean requirePackage = model.getParameter(REQUIRE_PACKAGE);
    	return requirePackage;
    }
    
    // Get logged on user
    protected Long getSessionUserID(final ComponentModel model)
    {
    	return model.get(SCENE_CONNECTION_FORM).getDataItem().get(SESSION_USERID);
    }
}
