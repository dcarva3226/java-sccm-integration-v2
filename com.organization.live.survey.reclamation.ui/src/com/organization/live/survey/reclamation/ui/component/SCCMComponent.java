package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.ui.service.action.ExecutionSource;

/**
 * 
 * @author Danny Carvajal
 *
 */
public abstract interface SCCMComponent 
{
	
	public static final String SOURCE_MODEL = "Source Model";
	public static final String VIEW_HEADER1 = "Header1";
	public static final String VIEW_HEADER2 = "Header2";
	public static final String VIEW_DETAILS = "Details";
	
	
	/**
	 * Returns the {@link ExecutionSource} appropriate for the component's actions.
	 * 
	 * @return the {@link ExecutionSource}
	 */
	public ExecutionSource getExecutionSource();
}
