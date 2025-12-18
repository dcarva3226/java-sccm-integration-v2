package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.survey.reclamation.ui.action.InitSCCMUninstallAction;
import com.organization.live.ui.common.client.component.descriptor.ComponentView;
import com.organization.live.ui.common.client.component.descriptor.StateDescriptor;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.system.action.executionsource.ComponentExecutionSource;

/**
 * SCCM Uninstall component provider. This code adds widgets to the panel.
 * 
 * @author Danny Carvajal
 *
 */
public class SCCMUninstallProvider extends SCCMProvider<SCCMUninstallComponent>
	implements SCCMUninstallComponent
{
	private final ExecutionSource executionSource = new ComponentExecutionSource(getName());

	@Override
	public ExecutionSource getExecutionSource()
	{
		return executionSource;
	}

	@Override
	public String getName()
	{
		return COMPONENT_NAME;
	}
	
	@Override
	protected ComponentView getView()
	{
		final ComponentView view = new ComponentView();
		final StateDescriptor panelState = new StateDescriptor(STATE_PANEL);
		final FormDescriptor mainFormRun = new FormDescriptor(MODEL_FORM_MAIN_RUN);
		final FormDescriptor createFormRun = new FormDescriptor(MODEL_FORM_CREATE_RUN);

		mainFormRun.setPercentHeight(55);
		mainFormRun.setHasCancel(true);

		createFormRun.setPercentHeight(55);
		createFormRun.setHasCancel(true);

		view.addState(panelState);
		view.setInitialState(STATE_PANEL);
		view.setInitialAction(InitSCCMUninstallAction.ACTION_NAME);

		panelState.getPanel().setHasCancel(false);
		panelState.getPanel().addWidget(mainFormRun);
		panelState.getPanel().addWidget(createFormRun);

		return view;
	}

	@Override
	public boolean isSystem()
	{
		return true;
	}
}
