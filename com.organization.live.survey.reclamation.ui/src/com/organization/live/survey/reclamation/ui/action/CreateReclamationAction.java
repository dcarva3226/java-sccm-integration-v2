package com.organization.live.survey.reclamation.ui.action;

import static com.organization.live.db.query.Query.count;
import static com.organization.live.ui.system.component.dataview.DataViewComponent.DATA_VIEW;
import static com.organization.live.ui.system.component.dataview.DataViewComponent.DRILL_DOWN_COMPONENT;
import static com.organization.live.ui.system.component.dataview.DataViewComponent.DRILL_DOWN_STATE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.entity.EntityManager;
import com.organization.live.db.query.HAQueryExecutor;
import com.organization.live.db.query.Query;
import com.organization.live.survey.reclamation.ui.Views;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanWizard;
import com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.action.ActionDescriptor;
import com.organization.live.ui.common.client.action.ActionView;
import com.organization.live.ui.common.client.action.ActionViewStyle;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.component.Context;
import com.organization.live.ui.common.client.component.descriptor.ResultState;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.type.ModelState;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetDescriptor;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetModel;
import com.organization.live.ui.common.client.widget.grid.GridModel;
import com.organization.live.ui.common.client.widget.panel.PanelDescriptor;
import com.organization.live.ui.pad.scan.OnDemandScanSubmissionComponent;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.service.exception.UIException;
import com.organization.live.ui.service.user.role.Role;
import com.organization.live.ui.system.action.executionsource.DataViewNameExecutionSource;
import com.organization.live.ui.system.component.dataview.DataViewComponent;

/**
 * @Danny Carvajal - example used CreateImportAction from com.organization.live.impex.plan
 *
 */
public class CreateReclamationAction extends AbstractReclamationAction 
{
	private static final ActionView actionViewA = new ActionView(ActionViewStyle.WIDGET_CONTEXT, "Create Reclamation Plan", DataViewComponent.DATA_LIST);
	private static final ActionView actionViewB = new ActionView(ActionViewStyle.WIDGET_BUTTON, "Create Reclamation Plan", DataViewComponent.DATA_LIST);
	private static final String NAME = CreateReclamationAction.class.getSimpleName();
	private static Logger logger = LoggerFactory.getLogger(CreateReclamationAction.class);
	private boolean requirePackage = false;
	
	@Override
	public String getName() 
	{
		return NAME;
	}   
	
    @Override
	public ComponentModel execute(ComponentModel model, ServiceLocator serviceLocator) throws Exception 
    {
    	invokeWizard(model, serviceLocator);
		return model;
    }
    
    @Override
    public ExecutionSource getExecutionSource() 
    {
    	return (new DataViewNameExecutionSource(Views.Reclamation.RECLAMATION_WIN_SPKG, Views.Reclamation.RECLAMATION_SIGNATURES_BY_CANDIDATE));
    }

	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.common.AbstractAction#getViews()
	 */
	@Override
	public List<ActionView> getViews() 
	{
        actionViewA.setOrder(100);       
        actionViewA.setEnablingState(ModelState.SELECTED);
        actionViewB.setOrder(101);        
        actionViewB.setEnablingState(ModelState.SELECTED);
        return (Arrays.asList(actionViewA, actionViewB));
	}    
	  	
	// Start up the Reclamation wizard
    private void invokeWizard(final ComponentModel model, ServiceLocator serviceLocator) throws Exception 
    {
    	String validateMsg = validateSelections(model, serviceLocator);
    	if (validateMsg != null)
    		throw new UIException(validateMsg);
    	
        final Long currentDataViewId = model.getContext().get(DATA_VIEW);
        final ComponentWidgetModel cmpWidgetModel = new ComponentWidgetModel(
                model.<ComponentWidgetDescriptor> getDescriptor(DRILL_DOWN_COMPONENT));
        cmpWidgetModel.setComponentName(ReclamationPlanWizard.COMPONENT_NAME);
        
        final Context cxt = new Context();
        cxt.setParameter(ReclamationPlanWizard.RECLAMATION_DATAVIEW_ID, currentDataViewId);
        cxt.setParameter(ReclamationPlanWizard.SOURCE_MODEL, model.getSourceModel());
        cxt.setParameter(ReclamationPlanWizard.REQUIRE_PACKAGE, this.requirePackage);
        
        cmpWidgetModel.setComponentConfigs(new HashMap<String, Object>());
        cmpWidgetModel.setContext(cxt);
        cmpWidgetModel.setHeading("Click to go back");
        cmpWidgetModel.setModified(true);
        model.set(cmpWidgetModel);
        final ActionDescriptor actionDescriptor = model.getActions().get(getName());
        actionDescriptor.setResultState(new ResultState(DRILL_DOWN_STATE));
        actionDescriptor.setForwardAction(null);
        PanelDescriptor configuration = model.getComponentView().getConfiguration();
        configuration.setVisible(false);
    }
    
    
	/**
	 * Show or hide the button in the Computers view
	 */
	@Override
	protected boolean showView(final ActionView view, final Map<String, Object> componentConfigs, final ServiceLocator serviceLocator) 
	{
		
		// Are there any SCCM Servers installed? If not, then set them up
		DbApi dbApi = serviceLocator.getDbApi();
        HAQueryExecutor queryExecutor = dbApi.getQueryExecutor();
        Boolean reclamationEnabled = false;
        
		final Role userRole = serviceLocator.getRoleService().getUserRole(serviceLocator);
        
        try 
        {
        	if (userRole.hasAccess(OnDemandScanSubmissionComponent.COMPONENT_NAME) && 
        			userRole.hasAccess(SCCMUninstallComponent.COMPONENT_NAME))
        	{
        		reclamationEnabled = queryExecutor.execute1(
        			Query.select(Db.ReclamationConfig.COLUMN_ENABLED).from(Db.ReclamationConfig.TABLE_NAME));
        	}
        }
        catch(Exception e) 
        {
            logger.error(String.format("CreateReclamation:showView() failed. %s", 
            		e.getMessage()));
        }
        return reclamationEnabled;        
	}		
	
	
	/**
	 * Make sure the a few things are good and proper before invoking wizard
	 */
    private String validateSelections(final ComponentModel model, final ServiceLocator serviceLocator)
    {    
    	
    	// Ensure that one or more items were selected
		final GridModel gridModel = model.getSourceModel();
	    final List<Data> selections = gridModel.getSelected();
	    if (selections.size() == 0) 
	    {
	    	return "Please select at least one software signature.";
	    }   
	    
	    // Make sure user selects similar signatures
		final EntityManager mgr = serviceLocator.getDbApi().getEntityManager();
		String lastPackageGUID = null;
		String lastPackageName = null;
		boolean first = true;

		for (Data windowsPackage : selections)
		{
			String packageGUID = windowsPackage.get(Db.SoftwarePackagesWindows.COLUMN_SIGNATURE_GUID);
			final String packageName = windowsPackage.get(Db.SoftwarePackagesWindows.COLUMN_FRIENDLY_NAME);

			// Handle things if the GUID isn't in the view
			if (StringUtils.isBlank(packageGUID))
			{
				Entity packageEntity = mgr.readById(Db.SoftwarePackagesWindows.TABLE_NAME, windowsPackage.getId());
				packageGUID = packageEntity.getString(Db.SoftwarePackagesWindows.COLUMN_SIGNATURE_GUID);
			}

			if (!first)
			{
				if ((StringUtils.isBlank(packageGUID) && (lastPackageGUID != null)) ||
					(StringUtils.isNotBlank(packageGUID) && (lastPackageGUID == null)))
				{
					throw new UIException("You cannot select a mixture of MSI and non-MSI packages for removal.");
				}

				else if (StringUtils.isNotBlank(packageGUID) && !packageGUID.equals(lastPackageGUID))
				{
					throw new UIException("You cannot select different MSI packages for removal in the same job.");
				}

				else if ((lastPackageName != null) && (lastPackageGUID == null) && !lastPackageName.equals(packageName))
				{
					throw new UIException("You cannot select different non-MSI packages for removal in the same job.");
				}
			}

			lastPackageGUID = packageGUID;
			
			// Keep track of package type (msi or non-msi)
			if (StringUtils.isBlank(packageGUID))
				this.requirePackage = true;
			else
				this.requirePackage = false;
			
			if (StringUtils.isNotBlank(packageName))
				lastPackageName = packageName;

			first = false;
		}	    
    	
	    // Make sure that the SCCM probe has run and found managed machines
	    HAQueryExecutor queryExecutor = serviceLocator.getDbApi().getQueryExecutor();
		Long recCount = queryExecutor.execute1(Query.select(count())
				.from(Db.ManagementID.TABLE_NAME, "nm")
				.join(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "nm." + Db.ManagementID.COLUMN_PLATFORM)
				.where(Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_NAME, SCCM_PLATFORM))
				.limit(1L));
		
		if (recCount == 0) 
		{
			return "There are no computers found that are managed by SCCM. Please run SCCM probe to discover machines.";
		}
	    
	    return null;
    }
}
