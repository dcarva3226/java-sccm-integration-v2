package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.ui.common.client.widget.form.ColumnStyle;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.form.ColumnStyle.ColumnsBuildingStyle;
import com.organization.live.ui.service.component.AbstractComponent;

/**
 * Base Uninstall Connector component provider.
 * 
 * @author Danny Carvajal
 *
 */
public abstract class SCCMProvider<C extends SCCMComponent> extends AbstractComponent
	implements SCCMComponent
{
	/**
	 * Builds the descriptor used for the instructions footer.
	 * 
	 * @param name
	 * @return
	 */
	protected FormDescriptor getFooterFormDescriptor(String name)
	{
        FormDescriptor descriptor = new FormDescriptor(name);

        descriptor.setHasCancel(false);
        descriptor.setColumnStyle(new ColumnStyle(1, 1, ColumnsBuildingStyle.DIVIDE));
        descriptor.getColumnStyle().setLabelsWidth(10);

        return (descriptor);
	}
}
