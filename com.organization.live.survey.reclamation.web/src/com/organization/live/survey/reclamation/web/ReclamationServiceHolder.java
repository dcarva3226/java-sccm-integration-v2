package com.organization.live.survey.reclamation.web;

import com.organization.live.survey.reclamation.web.impl.ReclamationServiceImpl;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class ReclamationServiceHolder 
{
	private static ReclamationServiceImpl reclamationService;

	public static ReclamationServiceImpl getReclamationService() 
	{
		return reclamationService;
	}

	public static void setReclamationService(final ReclamationServiceImpl reclamationService) 
	{
		ReclamationServiceHolder.reclamationService = reclamationService;
	}
}
