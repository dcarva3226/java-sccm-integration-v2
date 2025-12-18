package com.organization.live.survey.reclamation.ui.action;

import static com.organization.live.db.query.Query.count;
import static com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.HAQueryExecutor;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.logger.LogLevel;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerService;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.ui.component.ReclamationLoggerDetails;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent;
import com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponentConfig;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.view.info.MetaModel;
import com.organization.live.ui.common.client.view.info.SelectionViewInfo;
import com.organization.live.ui.common.client.view.info.ViewInfo;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.service.ServiceLocator;

/**
 * Initialize the main form status message.
 * 
 * @author Danny Carvajal, borrowing code from Bill Somerville
 * This code initializes the popup box.
 */
public class InitSCCMUninstallAction extends BaseInitSCCMUninstallComponentAction<SCCMUninstallComponent>
{
	public static final String ACTION_NAME = "InitSCCMUninstallAction";
	private DbApi dbApi = null;
	private boolean showPAD = true;
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.Action#getName()
	 */
	@Override
	public String getName()
	{
		return ACTION_NAME;
	}

	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.Action#execute(com.organization.live.ui.common.client.component.ComponentModel, com.organization.live.ui.service.ServiceLocator)
	 */
	@Override
	public ComponentModel execute(ComponentModel model, ServiceLocator serviceLocator) throws Exception
	{
		final FormDescriptor createFormDescriptor = new FormDescriptor(SCCMUninstallComponent.MODEL_FORM_CREATE_RUN);
		final FormModel mainForm;
		final Data data = new Data();		
		mainForm = buildNewMain(model, data, createFormDescriptor, "", serviceLocator);
		model.set(mainForm);
		return model;
	}

	
	/**
	 * @param model
	 * @param data
	 * @param descriptor
	 * @param serviceID
	 * @param subHeaderText
	 * @param serviceLocator
	 * @return
	 * @throws Exception
	 */
	private FormModel buildNewMain(ComponentModel model, final Data data, final FormDescriptor descriptor,
		final String subHeaderText, final ServiceLocator serviceLocator) throws Exception
	{
		final MetaModel map = new MetaModel();
		FormModel form;

		buildHeader(data, map, SCCMUninstallComponent.HEADER_ICON,
				SCCMUninstallComponent.HEADER_DEFAULT_TEXT, subHeaderText);
		
		buildNewForm(model, data, map, serviceLocator);

		form = new FormModel(descriptor, map, data);

		return form;
	}

	
	/**
	 * @param model
	 * @param data
	 * @param map
	 * @param serviceID
	 * @param serviceLocator
	 * @throws Exception 
	 */
	private void buildNewForm(ComponentModel model, final Data data, final MetaModel map,
		final ServiceLocator serviceLocator) throws Exception
	{
		final ViewInfo instructions = createHTMLReadOnlyViewInfo(SCCMUninstallComponent.VIEW_DETAILS, getStatusMessage(model, serviceLocator));	
		map.add(instructions);

        // Select PAD
		if (this.showPAD)
		{
	        final SelectionViewInfo<String> pad = new SelectionViewInfo<String>(getPads(serviceLocator), PAD_NAME);
	        pad.setHint(PAD_NAME_HINT);
	        pad.setLabel(PAD_NAME_LABEL);
	        pad.setRequired(true);
	        pad.setWidth(325);
	        map.add(pad);
		}
	}
	
	
	/**
	 * 
	 * Find out if machines are still managed
	 *
	 */
	private String getStatusMessage(ComponentModel model, final ServiceLocator serviceLocator) throws Exception
	{
		this.dbApi = serviceLocator.getDbApi();
        LoggerService loggerService = getLoggerService(this.dbApi);
        loggerService.getLogger(ReclamationPlanComponent.LOG_PREFIX, null, ReclamationPlanComponent.LOG_DESCRIPTION).keepFor(LogLevel.DEBUG, 1);
        Logger logger = getLogger(getName(), loggerService);				
        ReclamationLoggerDetails logDetails = null;
		
		HAQueryExecutor queryExecutor = serviceLocator.getDbApi().getQueryExecutor();
		final StringBuilder message = new StringBuilder();
		int unmanagedCount = 0;

		// Get some reclamation details
		final Long reclamationPlanID = model.getParameter(SCCMUninstallComponentConfig.CONFIG_RECLAMATION_ID);
		final String reclamationName = model.getParameter(SCCMUninstallComponentConfig.CONFIG_RECLAMATION_NAME);
		final Boolean approved = model.getParameter(SCCMUninstallComponentConfig.CONFIG_RECLAMATION_APPROVED);
		logDetails = new ReclamationLoggerDetails(reclamationPlanID);

		message.append(String.format("<br/>You have chosen to submit the reclamation plan '%s' to SMS/SCCM. %s<br/>",
				reclamationName,
				approved ? "However, this plan has already been submitted. If you wish to resubmit this plan, click 'Submit' to proceed." : null));
		
		// Get list of machines from reclamation details table where email has been confirmed
		SelectQuery query = Query.select(Arrays.asList(
				Query.column("rc." + Db.ReclamationComputer.COLUMN_COMPUTER),
				Query.column("c." + Db.Computer.COLUMN_NAME)));
		query.from(Db.ReclamationComputer.TABLE_NAME, "rc");
		query.join(Db.Computer.TABLE_NAME, "c", "c." + Db.Computer.COLUMN_PARENT_ID, "rc." + Db.ReclamationComputer.COLUMN_COMPUTER);
		query.join(Db.SoftwarePackagesWindows.TABLE_NAME, "ws", "ws." + Db.SoftwarePackagesWindows.COLUMN_ID, "rc." + Db.ReclamationComputer.COLUMN_SIGNATURE_ID);
	    query.where(Criterion.AND(Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, reclamationPlanID),
	    		                  Criterion.NE("ws." + Db.SoftwarePackagesWindows.COLUMN_OPERATIONAL, false), // <-- make sure signature is not already uninstalled
	    		Criterion.AND(
		    				Criterion.OR(
					    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_USER_RESPONSE, true), // <---yes to send to SCCM
					    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, true))))); // <---yes to send to SCCM
	    
		try		
		{	
			Long sccmPlatformID = getSCCMPlatformId(serviceLocator);
			
		    List<Object[]> computers = queryExecutor.executeLA(query);	    
		    for (Object[] computer : computers) 
		    {
		    	// The computer name is not null, continue
				if (computer[1] != null) 
				{
					// Get computer (SCCM computer GUID) from management id table using computer ID in ReclamationDetail table
					// If computer is not found in management table, then it's considered unmanaged.
					SelectQuery query2 = Query.select(count());
							query2.from(Db.ReclamationComputer.TABLE_NAME, "rd");
							query2.join(Db.Computer.TABLE_NAME, "c", "c." + Db.Computer.COLUMN_PARENT_ID, "rd." + Db.ReclamationComputer.COLUMN_COMPUTER);						
							query2.leftJoin(Db.ManagementID.TABLE_NAME, "m", "m." + Db.ManagementID.COLUMN_DEVICE, "c." + Db.Computer.COLUMN_ID);
							query2.leftJoin(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementID.COLUMN_PLATFORM);
							query2.where(Criterion.AND(Criterion.EQ("c." + Db.Computer.COLUMN_ID, (Long) computer[0]), 
									Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, sccmPlatformID)));
				        	
					Long recCount = queryExecutor.execute1(query2);						
				
					// Create warning message for unmanaged computers
					if (recCount == 0) 
					{
						unmanagedCount++;
						if (unmanagedCount == 1)
						{
							message.append("<br/>However, the following computers can no longer be verified as managed by SMS/SCCM:<br/><br/>");
						}
						
						if (unmanagedCount <= UNMANAGED_THRESHOLD)
						{
							message.append(computer[1].toString() + "<br/><br/>");
						}
					}
				}
		    }	    
		    
		    int computerSize = computers.size();
			if (computerSize == 0)
			{
				message.append("<br/>However, there are no computers available in this reclamation plan. ");
				message.append("This may occur when no users have responded to reclamation email notices. Click 'Cancel' to abort.");
				this.showPAD = false;
			}
			else
			{
			    // If we found unmanaged machines, complete the warning message string					
				if (unmanagedCount > 0)
			    {
					if (unmanagedCount > UNMANAGED_THRESHOLD)
					{
						message.append("... and " + (unmanagedCount - UNMANAGED_THRESHOLD) + " more<br/>");
					}
			
					message.append(String.format("<br/><br/>There %s %d computer%s ready to be submitted.",
			    			(computerSize - unmanagedCount) == 1 ? "is" : "are",
					    	(computerSize - unmanagedCount),
					    	(computerSize - unmanagedCount) == 1 ? "" : "s"));
					message.append("Click 'Submit' to submit plan to SMS/SCCM without the computer(s) above. Click 'Cancel' to abort.</br></br>");
			    }
			    else
			    {
					message.append(String.format("<br/><br/>There %s %d computer%s ready to be submitted.",
			    			computerSize == 1 ? "is" : "are",
					    	computerSize,
					    	computerSize == 1 ? "" : "s"));
			    	message.append(" Please select a PAD and click 'Submit' to submit to SMS/SCCM. Click 'Cancel' to abort.</br></br>");
			    }
				this.showPAD = true;
			 }
		}
		catch (Exception e)
		{
			logger.error(String.format("Could not initialize InitSCCMUninstallAction for reclamation plan ID: {%d} to SMS/SCCM. Exception: %s", 
					reclamationPlanID, 
					e.getMessage()), logDetails);
			
			throw new Exception(String.format("Could not initialize InitSCCMUninstallAction for reclamation plan ID: {%d} to SMS/SCCM. Exception: %s", 
					reclamationPlanID, 
					e.getMessage()));
		}
	    return message.toString();	    		
	}
	
	
	/**
	 * 
	 * Return SCCM Packages read into Management Packages table.
	 *
	 */
	private List<String> getPads(ServiceLocator serviceLocator) 
	{					
		DbApi dbApi = serviceLocator.getDbApi();		
		List<String> dataList = new ArrayList<String>();		
	  
		SelectQuery query = Query.select(Query.column(Db.Pad.COLUMN_NAME));
	    query.from(Db.Pad.TABLE_NAME);
	    query.orderBy(Db.Pad.COLUMN_NAME);
	    
	    List<String> list = dbApi.getQueryExecutor().executeL1(query);
	    
	    for (String value : list) 
	    {
	    	String padName = value.toString();		    	  
			  
			if (padName != null) 
			{
				dataList.add(padName);
			}
	    }	    
	    
	    return dataList;
	}		
}

