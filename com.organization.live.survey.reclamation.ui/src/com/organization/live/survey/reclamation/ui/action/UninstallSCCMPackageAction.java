package com.organization.live.survey.reclamation.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent.*;
import static com.organization.live.db.query.Query.count;

import com.organization.live.common.probe.sccm.impl.UninstallScanTargetImpl;
import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.entity.EntityManager;
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
import com.organization.live.ui.common.client.action.ActionView;
import com.organization.live.ui.common.client.action.ActionViewStyle;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.event.ComponentEvent;
import com.organization.live.ui.common.client.event.Event;
import com.organization.live.ui.common.client.event.Event.EventExecution;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.pad.scan.OnDemandScanSubmissionComponent;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.exception.UIException;


/**
 * 
 * @author Danny Carvajal
 * This code handles the event when the "Submit" button is clicked in the popup form.
 *
 */
public class UninstallSCCMPackageAction extends BaseSCCMComponentAction<SCCMUninstallComponent> 
{
	private static final ActionView actionView = new ActionView(ActionViewStyle.WIDGET_BUTTON, "Submit", SCCMUninstallComponent.MODEL_FORM_CREATE_RUN);
	public static final String ACTION_NAME = "UninstallerSCCMPackageAction";
	private UninstallSCCMPackageListener uninstallSCCMPackageListener;
	private DbApi dbApi = null;

	
	@Override
	public String getName() 
	{
		return ACTION_NAME;
	}
	
    
	public void setUninstallSCCMPackageListener(final UninstallSCCMPackageListener uninstallSCCMPackageListener) 
    {
        this.uninstallSCCMPackageListener = uninstallSCCMPackageListener;
    }
	
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.common.AbstractAction#getViews()
	 */
	@Override
	public ActionView getView()
	{
        actionView.setOrder(50);
        return (actionView);
	}
	
	
	// Do we show Create button? Make sure we have at least one computer that can be submitted to SCCM
	protected boolean showView(ActionView view, Map<String, Object> componentConfigs, ServiceLocator serviceLocator)
	{		
		Long reclamationPlanID = (Long) componentConfigs.get(SCCMUninstallComponentConfig.CONFIG_RECLAMATION_ID);

		SelectQuery query = Query.select(count());
		query.from(Db.Reclamation.TABLE_NAME, "r");
		query.join(Db.ReclamationComputer.TABLE_NAME, "rc", "rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, "r." + Db.Reclamation.COLUMN_ID);
	    query.where(Criterion.AND(Criterion.EQ("r." + Db.Reclamation.COLUMN_ID, reclamationPlanID),
	    		Criterion.AND(
		    				Criterion.OR(
					    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_USER_RESPONSE, true),
					    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, true)))));
		query.limit(1L);
    	
		Long recCount = serviceLocator.getDbApi().getQueryExecutor().execute1(query);
		return recCount == 0 ? false : true;
	}
	
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.common.AbstractAction#getEvent()
	 */
	@Override
	public Event getEvent()
	{
		final Event cancel = ComponentEvent.CANCEL();

		// Needed for popup dismissal
		cancel.setExecution(EventExecution.POST_EXECUTE);
		return cancel;
	}	
	
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.Action#execute(com.organization.live.ui.common.client.component.ComponentModel, com.organization.live.ui.service.ServiceLocator)
	 */
	@Override
	public ComponentModel execute(ComponentModel model, final ServiceLocator serviceLocator) throws Exception
	{
		this.dbApi = serviceLocator.getDbApi();
		LoggerService loggerService = getLoggerService(dbApi);
        loggerService.getLogger(ReclamationPlanComponent.LOG_PREFIX, null, ReclamationPlanComponent.LOG_DESCRIPTION).keepFor(LogLevel.DEBUG, 1);
        Logger logger = getLogger(getName(), loggerService);				
        ReclamationLoggerDetails logDetails = null;
		final EntityManager mgr = this.dbApi.getEntityManager();	
		
		// Get current reclamation plan Id and pad
		Long reclamationPlanID = model.getParameter(SCCMUninstallComponentConfig.CONFIG_RECLAMATION_ID);		
		final FormModel formModel = model.get(SCCMUninstallComponent.MODEL_FORM_CREATE_RUN);
		final Data data = formModel.getDataItem();
		final Long padID = getPadID(data.get(SCCMUninstallComponent.PAD_NAME).toString(), serviceLocator);
		String planName = null;
		
		try
		{					
			// Get machineIDs to put into the target
			final List<String> computerIDs = getComputerIDs(reclamationPlanID, serviceLocator);
			if (computerIDs.size() == 0)
			{
				throw new UIException("There are no computers in this reclamation plan that are approved for submittal to SMS/SCCM.");
			}
					
			// Get reclamation plan data here
			final List<Object[]> reclamationProperties = getReclamationProperties(reclamationPlanID);
			
			final String packageID;
			final String programName;
			final String packageGUID;
			final String packageName;	
			
			String advertisementName = null;
			String advertisementDesc = null;
			String collectionDesc = null;
			Boolean requireAdvertisements = null;
			Boolean allowRestarts = null;
			
			
			final boolean allowAdvertisements = reclamationProperties.get(0)[9].toString() == "true" ? true : false;			
			if (allowAdvertisements) 
			{
				advertisementName = reclamationProperties.get(0)[4].toString();
				advertisementDesc = reclamationProperties.get(0)[5] != null ? reclamationProperties.get(0)[5].toString() : null;
				requireAdvertisements = reclamationProperties.get(0)[12].toString() == "true" ? true : false;
			}
			
			final String siteServer = reclamationProperties.get(0)[0].toString();
			final String siteCode = reclamationProperties.get(0)[1].toString();
			final String collectionName = reclamationProperties.get(0)[2].toString();
			collectionDesc = reclamationProperties.get(0)[3] != null ? reclamationProperties.get(0)[3].toString() : null;
			final String folderName = FOLDER_NAME;
			final String subCollectionName = SUBCOLLETION_NAME;
			final String subCollectionDesc = SUBCOLLETION_DESC;
			final Entity plan = JobRelatedHelper.getOrCreateUninstallPlan(mgr, siteServer, siteCode);
			final UninstallScanTargetImpl target = new UninstallScanTargetImpl();
			planName = reclamationProperties.get(0)[11].toString();
			
			// Does user have permissions to kick off this job?
			if (!serviceLocator.getRoleService().getUserRole(serviceLocator)
					.hasAccess(OnDemandScanSubmissionComponent.COMPONENT_NAME))
			{
				throw new UIException("Security permissions denied your access to execute the uninstall...");
			}
	
			// Figure out if we have existing sccm package/program selections
			if (reclamationProperties.get(0)[6] instanceof Long)
			{
				final Long packageRowID = (Long) reclamationProperties.get(0)[6];
				final Long programRowID = (Long) reclamationProperties.get(0)[7];
	
				packageID = mgr.readById(Db.ManagementPackage.TABLE_NAME, packageRowID)
					.getString(Db.ManagementPackage.COLUMN_PACKAGE_ID);
				
				programName = mgr.readById(Db.ManagementProgram.TABLE_NAME, programRowID)
					.getString(Db.ManagementProgram.COLUMN_NAME);
				
				packageGUID = null;
				packageName = null;
			}
	
			else
			{
				// No existing sccm package/program selections, we are creating the msiexec command line
				packageID = null;
				programName = null;
				packageGUID = reclamationProperties.get(0)[8].toString();
				packageName = truncateDBString(serviceLocator.getDbApi(), Db.PadProbeSCCMUninstall.TABLE_NAME,
					CNAME_PACKAGE_NAME,
					reclamationProperties.get(0)[10].toString());
				allowRestarts = reclamationProperties.get(0)[13].toString() == "true" ? true : false;
			}

			// Copy stuff into the plan
			plan.set(SITE_CODE, siteCode);
			plan.set(PACKAGE_ID, packageID);
			plan.set(PACKAGE_GUID, packageGUID);
			plan.set(PACKAGE_NAME, packageName);
			plan.set(PROGRAM_NAME, programName);
			plan.set(COLLECTION_NAME, collectionName);
			plan.set(COLLECTION_DESC, collectionDesc);
			if (allowAdvertisements) plan.set(ADVERTISEMENT_NAME, advertisementName);
			if (allowAdvertisements) plan.set(ADVERTISEMENT_DESC, advertisementDesc);			
			plan.save();
	
			// Copy stuff into the target
			target.setSiteCode(siteCode);
			target.setTargetMachines(computerIDs);
			target.setPackageID(packageID);
			target.setPackageGUID(packageGUID);
			target.setPackageName(packageName);
			target.setProgramName(programName);
			target.setCollectionName(collectionName);
			target.setCollectionDescription(collectionDesc);
			target.setFolderName(folderName);
			target.setAllowAdvertisements(allowAdvertisements);
			target.setRequireAdvertisements(requireAdvertisements);
			if (allowRestarts != null) target.setAllowRestarts(allowRestarts);
			if (allowAdvertisements) target.setAdvertisementName(advertisementName);
			if (allowAdvertisements) target.setAdvertisementDescription(advertisementDesc);
			target.setSubCollectionName(subCollectionName);
			target.setSubCollectionDescription(subCollectionDesc);
	
			// Run the job
			JobRelatedHelper.runJob(target, plan, padID, this.uninstallSCCMPackageListener, new Date(),
				serviceLocator);
			
			// Update reclamation plan
			updateReclamationPlan(reclamationPlanID, serviceLocator);
			
	    	// Log success
			logDetails = new ReclamationLoggerDetails(reclamationPlanID);
			logger.info(String.format("Reclamation plan '%s' was successfully sent to SMS/SCCM.", planName), logDetails);
		}
		catch (Exception e)
		{
			logger.error(String.format("Could not submit reclamation plan '%s' to SMS/SCCM. Exception: %s", 
					planName, 
					e.getMessage()), logDetails);
			
			throw new Exception(String.format("Could not submit reclamation plan '%s' to SMS/SCCM. Exception: %s", 
					planName, 
					e.getMessage()));
		}
		finally
		{
			setSessionParameter(SCCMUninstallComponent.READY_FOR_SCCM, true, serviceLocator);
		}
		return model;
	}

	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.Action#getName()
	 */

	
	/**
	 * 
	 * Get all SCCM machine IDs (GUIDS) associated with reclamation plan
	 *
	 */
	private List<String> getComputerIDs(final Long reclamationPlanID, final ServiceLocator serviceLocator)
	{
		HAQueryExecutor queryExecutor = this.dbApi.getQueryExecutor();
		List<String> managedComputerIDs = new ArrayList<String>();
		List<Object[]> computerDetails = new ArrayList<Object[]>();
		
		// Get list of machines from reclamation details table where email has been confirmed as YES to send to SCCM OR take_no_response_action = true
		SelectQuery query = Query.selectDistinct(
				Query.column("rc." + Db.ReclamationComputer.COLUMN_COMPUTER));
		query.from(Db.ReclamationComputer.TABLE_NAME, "rc");
		query.join(Db.ReclamationEmail.TABLE_NAME, "re", "re." + Db.ReclamationEmail.COLUMN_RECLAMATION_COMPUTER, "rc." + Db.ReclamationComputer.COLUMN_ID);
		query.join(Db.SoftwarePackagesWindows.TABLE_NAME, "ws", "ws." + Db.SoftwarePackagesWindows.COLUMN_ID, "rc." + Db.ReclamationComputer.COLUMN_SIGNATURE_ID);
	    query.where(Criterion.AND(Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_RECLAMATION_ID, reclamationPlanID),
	    		                  Criterion.NE("ws." + Db.SoftwarePackagesWindows.COLUMN_OPERATIONAL, false), // <-- make sure signature is not already uninstalled
	    		Criterion.AND(
	    				Criterion.OR(
				    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_USER_RESPONSE, true), // <---yes to send to SCCM
				    		Criterion.EQ("rc." + Db.ReclamationComputer.COLUMN_TAKE_NO_RESPONE_ACTION, true))))); // <---yes to send to SCCM
	    
	    List<Long> computers = queryExecutor.executeL1(query);
	    
	    // Grab platform ID one time
	    Long sccmPlatformID = getSCCMPlatformId(serviceLocator);
	    
	    for (Long computer : computers) 
	    {
			if (computer != null) 
			{
				// Get computer (SCCM computer GUID) from management id table using computer ID in ReclamationDetail table
				// If computer is not found in management table, then it's considered unmanaged.
				SelectQuery query2 = Query.select(Arrays.asList(
						Query.column("m." + Db.ManagementID.COLUMN_PLATFORM_ID),
						Query.column("p." + Db.ManagementPlatform.COLUMN_NAME),
						Query.column("c." + Db.Computer.COLUMN_NAME)));
						query2.from(Db.ReclamationComputer.TABLE_NAME, "rd");
						query2.join(Db.Computer.TABLE_NAME, "c", "c." + Db.Computer.COLUMN_PARENT_ID, "rd." + Db.ReclamationComputer.COLUMN_COMPUTER);						
						query2.leftJoin(Db.ManagementID.TABLE_NAME, "m", "m." + Db.ManagementID.COLUMN_DEVICE, "c." + Db.Computer.COLUMN_ID);
						query2.leftJoin(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementID.COLUMN_PLATFORM);
						query2.where(Criterion.AND(Criterion.EQ("c." + Db.Computer.COLUMN_ID, computer),
								Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, sccmPlatformID)));
			        	
				computerDetails = queryExecutor.executeLA(query2);
				
				// Create warning message for unmanaged computers
				if (computerDetails.size() > 0) 
				{
					if (computerDetails.get(0)[0] != null && SCCM_PLATFORM.equals(computerDetails.get(0)[1]))
					{
						managedComputerIDs.add(computerDetails.get(0)[0].toString());
					}
				}
			}
	    }	    
	    
	    return managedComputerIDs;	    
	}
	
	
	/**
	 * 
	 * Get all needed reclamation properties
	 *
	 */
	private List<Object[]> getReclamationProperties(final Long reclamationPlanID)
	{
		HAQueryExecutor queryExecutor = this.dbApi.getQueryExecutor();

		SelectQuery query = Query.select(Arrays.asList(
			Query.column("rc." + Db.SCCMServerConfig.COLUMN_SCCM_SITE_SERVER),
			Query.column("rc." + Db.SCCMServerConfig.COLUMN_SCCM_SITE_CODE),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_COLLECTION_NAME),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_COLLECTION_DESC),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_NAME),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_DEPLOYMENT_DESC),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_PACKAGE),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_PROGRAM),
			Query.column("r." + Db.Reclamation.COLUMN_SIGNATURE_GUID),
			Query.column("r." + Db.Reclamation.COLUMN_ALLOW_ADVERTISEMENTS),
			Query.column("r." + Db.Reclamation.COLUMN_SIGNATURE_NAME),
			Query.column("r." + Db.Reclamation.COLUMN_NAME),
			Query.column("r." + Db.Reclamation.COLUMN_REQUIRE_ADVERTISEMENTS),
			Query.column("r." + Db.Reclamation.COLUMN_SCCM_ALLOW_RESTARTS)));
        	query.from(Db.Reclamation.TABLE_NAME, "r");
        	query.join(Db.SCCMServerConfig.TABLE_NAME, "rc", "rc." + Db.SCCMServerConfig.COLUMN_ID, "r." + Db.Reclamation.COLUMN_SCCM_CONFIG_ID);
        	query.where(Criterion.EQ("r." + Db.Reclamation.COLUMN_ID, reclamationPlanID));
        	
		List<Object[]> serverProperties = queryExecutor.executeLA(query);
		return serverProperties;
	}
	
	
	/**
	 * 
	 * Update reclamation plan details after plan has been checked for work.
	 *
	 */
	private void updateReclamationPlan(Long reclamationPlanID, ServiceLocator serviceLocator)
	{
		Calendar cal = Calendar.getInstance();
    	Entity detailRec = this.dbApi.getEntityManager().readEntity(Db.Reclamation.TABLE_NAME, Criterion.EQ(Db.Reclamation.COLUMN_ID, reclamationPlanID));
		detailRec.set(Db.Reclamation.COLUMN_ENABLED, false);
    	detailRec.set(Db.Reclamation.COLUMN_APPROVED, true);
		detailRec.set(Db.Reclamation.COLUMN_APPROVED_BY, serviceLocator.getUserSession().getUser().getId());
		detailRec.set(Db.Reclamation.COLUMN_SCCM_SENT_DATE, cal.getTime());
		detailRec.save();
	}
	
	
	private Long getPadID(final String padName, final ServiceLocator serviceLocator)
    {
		SelectQuery query = Query.select(
			Query.column(Db.Pad.COLUMN_ID));
        	query.from(Db.Pad.TABLE_NAME);
        	query.where(Criterion.EQ(Db.Pad.COLUMN_NAME, padName));
        	
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);    	    	
    }
}
