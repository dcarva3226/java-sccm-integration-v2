package com.organization.live.survey.reclamation.component;

/**
 * @author Danny Carvajal
 *
 */
public interface ReclamationComponent 
{
	
    String COMPONENT_NAME = "Reclamation Component";
    
    String LOG_DESCRIPTION = "Reclamation Plan Activity Log ";
    String LOG_PREFIX = "reclamation:";
    String PLAN_NO_RESPONSE_ACTION_1 = "Do not uninstall";
    String PLAN_NO_RESPONSE_ACTION_2 = "Proceed to uninstall";
    int PLAN_NO_RESPONSE_ACTION_IDX_1 = 1;
    int PLAN_NO_RESPONSE_ACTION_IDX_2 = 2;  
    String SCCM_PLATFORM = "SMS/SCCM";    
   
	/**
	 * Reclamation logger tables.
	 */
    public static interface LoggerTables 
    {
        final String LOGGER_DETAIL = "opz_reclamation_activity_log";
        final String RECLAMATION_PLAN_ID = "reclamation";
    }
}
