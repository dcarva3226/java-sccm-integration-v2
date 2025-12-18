package com.organization.live.survey.reclamation.ui.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent;
import com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponentConfig;
import com.organization.live.ui.common.client.action.ActionView;
import com.organization.live.ui.common.client.action.ActionViewStyle;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.component.descriptor.PopUpStateDescriptor;
import com.organization.live.ui.common.client.component.descriptor.ResultState;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.type.ModelState;
import com.organization.live.ui.common.client.widget.WidgetModel;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetDescriptor;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetEvent;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetModel;
import com.organization.live.ui.pad.scan.OnDemandScanSubmissionComponent;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.system.action.executionsource.DataViewExecutionSource;
import com.organization.live.ui.system.component.dataview.DataViewAction;
import com.organization.live.ui.system.component.dataview.DataViewComponent;
import com.organization.live.survey.reclamation.db.Db;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class CreateUninstallSCCMPackageAction extends BaseInitSCCMUninstallComponentAction<SCCMUninstallComponent>
{
	private static final ActionView actionViewA = new ActionView(ActionViewStyle.WIDGET_CONTEXT, "Submit to SMS/SCCM", DataViewComponent.DATA_LIST);
	private static final ActionView actionViewB = new ActionView(ActionViewStyle.WIDGET_BUTTON, "Submit to SMS/SCCM", DataViewComponent.DATA_LIST);
	
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.common.AbstractAction#getViews()
	 */
	@Override
	public List<ActionView> getViews()
	{
        actionViewA.setOrder(1);
        actionViewB.setOrder(1);
        actionViewB.setEnablingState(ModelState.SINGLE_SELECTED);
        return (Arrays.asList(actionViewA, actionViewB));
	}	
	
	
	@Override
    public String getName() 
    {
        return getClass().getSimpleName();
    }
	
	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.Action#execute(com.organization.live.ui.common.client.component.ComponentModel, com.organization.live.ui.service.ServiceLocator)
	 */
	public ComponentModel execute(ComponentModel model, ServiceLocator serviceLocator) throws Exception
	{
		final ComponentWidgetDescriptor descriptor = new ComponentWidgetDescriptor(SCCMUninstallComponent.MODEL_PANEL);
		final ComponentWidgetModel widgetModel;
		final PopUpStateDescriptor popupState = DataViewAction.getPopUpState(model);
		final SCCMUninstallComponentConfig config = new SCCMUninstallComponentConfig(descriptor);
			
		// Needed to get data from grid
		final WidgetModel<Data, ?> sourceModel = model.getSourceModel();	
		Long reclamationPlanID = sourceModel.getSelectedItem().getId();
		config.setReclamationID(reclamationPlanID);	
		config.setReclamationName(sourceModel.getSelectedItem().get(Db.Reclamation.COLUMN_NAME).toString());
		Boolean approved = (Boolean) sourceModel.getSelectedItem().get(Db.Reclamation.COLUMN_APPROVED);
		config.setReclamationApproved(approved);
		
		// Has this plan already been submitted?
		//if (approved) throw new UIException("This Reclamation plan has already been approved and submitted to SMS/SCCM.");
		
		// Needed for popup dismissal
		descriptor.setListener(ComponentWidgetEvent.Back, DataViewAction.LOAD_DATA_VIEWS_ACTION);
		popupState.setPercentWidth(35);
		popupState.setPercentHeight(50);
		popupState.setResizable(true);

		// Pop it up
		popupState.setCloseAction(DataViewAction.LOAD_DATA_VIEWS_ACTION);
		popupState.getPanel().addWidget(descriptor);

		widgetModel = config.getComponentWidgetModel();
		widgetModel.setHeading(SCCMUninstallComponent.HEADER_DEFAULT_TEXT);

		model.set(widgetModel);

		return model;
	}	
	
	
	protected boolean showView(ActionView view, Map<String, Object> componentConfigs, ServiceLocator serviceLocator)
	{
		return serviceLocator.getRoleService().getUserRole(serviceLocator)
				.hasAccess(OnDemandScanSubmissionComponent.COMPONENT_NAME);
	}
	
	
	public ExecutionSource getExecutionSource()
	{
		return (new DataViewExecutionSource(Db.Reclamation.TABLE_NAME));
	}	
	
	
	public ResultState getResultState()
	{
		return new ResultState(DataViewComponent.POPUP_STATE);
	}
}
