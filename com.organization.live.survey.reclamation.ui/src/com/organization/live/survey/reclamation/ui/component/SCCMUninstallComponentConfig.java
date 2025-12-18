package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.ui.common.client.widget.component.ComponentWidgetConfig;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetDescriptor;

/**
 * Configuration for the SCCM Uninstall component.
 * 
 * @author Danny Carvajal
 *
 */
public class SCCMUninstallComponentConfig extends ComponentWidgetConfig
{
	public static final String CONFIG_RECLAMATION_ID = "ReclamationPlanID";	
	public static final String CONFIG_RECLAMATION_NAME = "ReclamationName";
	public static final String CONFIG_RECLAMATION_APPROVED = "ReclamationApproved";
	
	/**
	 * @param descriptor
	 */
	public SCCMUninstallComponentConfig(ComponentWidgetDescriptor descriptor)
	{
		super(descriptor, SCCMUninstallComponent.COMPONENT_NAME);
	}

	/**
	 * @param reclamation plan ID
	 */
	public void setReclamationID(Long reclamationPlanID)
	{
		setCustomConfig(CONFIG_RECLAMATION_ID, reclamationPlanID);
	}

	/**
	 * @return
	 */
	public Long getReclamationID()
	{
		return componentConfigs.get(CONFIG_RECLAMATION_ID);
	}	
	
	/**
	 * @param reclamation name
	 */
	public void setReclamationName(String name)
	{
		setCustomConfig(CONFIG_RECLAMATION_NAME, name);
	}

	/**
	 * @return
	 */
	public String getReclamationName()
	{
		return componentConfigs.get(CONFIG_RECLAMATION_NAME);
	}	
	
	/**
	 * @param reclamation approved by
	 */
	public void setReclamationApproved(boolean approved)
	{
		setCustomConfig(CONFIG_RECLAMATION_APPROVED, approved);
	}

	/**
	 * @return
	 */
	public Boolean getReclamationApproved()
	{
		return componentConfigs.get(CONFIG_RECLAMATION_APPROVED);
	}		
}
