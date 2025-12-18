package com.organization.live.survey.reclamation.db;

/**
 * Database intefaces
 *
 * @author Danny Carvajal
 *
 */
public interface Db 
{
	
	public static interface Configuration 
	{
		public static final String TABLE_NAME = "cmdb_ci";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";	
		public static final String COLUMN_OPERATIONAL = "operational";
		public static final String COLUMN_OWNER = "owner";
		public static final String COLUMN_AGENT_STALE_DAYS = "agent_stale_days";
	}
	
	public static interface Computer 
	{
		public static final String TABLE_NAME = "cmdb_ci_computer";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PARENT_ID = "sys_parent_id";
		public static final String COLUMN_PRIMARY_USER = "primary_user";
	}
	
	public static interface Currency
	{
		public static final String TABLE_NAME = "cmn_currency";
		public static final String COLUMN_ID = "id";		
		public static final String COLUMN_SYMBOL = "symbol";
	}
	
	public static interface ManagementID 
	{
		public static final String TABLE_NAME = "cmdb_network_management_id";
		public static final String COLUMN_DEVICE = "device";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_PLATFORM = "platform";	
		public static final String COLUMN_PLATFORM_ID = "platform_id";
	}
	
	public static interface ManagementPackage 
	{
		public static final String TABLE_NAME = "cmdb_management_package";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_DESCRIPTION = "description";
		public static final String COLUMN_OPERATIONAL = "operational";
		public static final String COLUMN_PACKAGE_ID = "package_id";
		public static final String COLUMN_PLATFORM = "platform";
	}		

	public static interface ManagementProgram 
	{
		public static final String TABLE_NAME = "cmdb_management_program";
		public static final String COLUMN_ID = "id";		
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PACKAGE = "package";		
	}	
	
	public static interface ManagementPlatform 
	{
		public static final String TABLE_NAME = "cmdb_management_platform";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";		
	}	
	
	public static interface NetworkDevice 
	{
		public static final String TABLE_NAME = "cmdb_ci_network_device";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_PARENT_ID = "sys_parent_id";		
	}
	
	public static interface Pad 
	{
		public static final String TABLE_NAME = "pad_server";
		public static final String COLUMN_HOST_NAME = "host_name";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_NAME = "name";
	}

	public static interface PadProbeSCCMUninstall
	{
		public static final String TABLE_NAME = "pad_probe_sccm_uninstall";
	}
	
	public static interface Person
	{
		public static final String TABLE_NAME = "cmn_person";
		public static final String COLUMN_ID = "id";		
		public static final String COLUMN_EMAIL = "email";
	}	
	
	public static interface Reclamation 
	{
		public static final String TABLE_NAME = "opz_reclamation";
		public static final String COLUMN_ADMIN_EMAIL = "admin_email";
		public static final String COLUMN_ALLOW_ADVERTISEMENTS = "allow_advertisements";
		public static final String COLUMN_APPROVED = "approved";
		public static final String COLUMN_APPROVED_BY = "approved_by";
		public static final String COLUMN_CREATED_ON = "created_on";
		public static final String COLUMN_DESC = "description";		
		public static final String COLUMN_EMAIL_CONFIG = "email_config";
		public static final String COLUMN_EMAIL_EXPIRATION_DAYS = "email_expiration_days";
		public static final String COLUMN_EMAIL_GROOM_DAYS = "email_groom_days";
		public static final String COLUMN_EMAIL_MAX_SEND_ATTEMPTS = "email_max_send_attempts";
		public static final String COLUMN_ENABLED = "enabled";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_LICENSABLE = "licensable";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_NO_RESPONSE_ACTION = "no_response_action";
		public static final String COLUMN_PAD = "pad";
 		public static final String COLUMN_REQUIRE_ADVERTISEMENTS = "require_advertisements";
		public static final String COLUMN_SEND_EMAIL_WAIT_DAYS = "email_send_wait_days";
		public static final String COLUMN_SCCM_CONFIG_ID = "sccm_config";
		public static final String COLUMN_SCCM_COLLECTION_DESC = "sccm_collection_desc";		
		public static final String COLUMN_SCCM_COLLECTION_NAME = "sccm_collection_name";		
		public static final String COLUMN_SCCM_DEPLOYMENT_DESC = "sccm_deployment_desc";
		public static final String COLUMN_SCCM_DEPLOYMENT_NAME = "sccm_deployment_name";
		public static final String COLUMN_SCCM_PACKAGE = "sccm_package";
		public static final String COLUMN_SCCM_PROGRAM = "sccm_program";
		public static final String COLUMN_SCCM_ALLOW_RESTARTS = "sccm_allow_restarts";
		public static final String COLUMN_SCCM_SIMULATION_MODE = "sccm_simulation_mode";
		public static final String COLUMN_SCCM_SENT_DATE = "sccm_sent_date";
		public static final String COLUMN_SIGNATURE_GUID = "signature_guid";
		public static final String COLUMN_SIGNATURE_NAME = "signature_name";
		public static final String COLUMN_SIGNATURE_SPV = "signature_spv";
	}	
	
	public static interface ReclamationActivityLog
	{
		public static final String TABLE_NAME = "opz_reclamation_activity_log";
	}
	
	public static interface ReclamationConfig 
	{
		public static final String TABLE_NAME = "opz_reclamation_config";		
		public static final String COLUMN_AGENT_STALE_DAYS = "agent_stale_days";
		public static final String COLUMN_DEFAULT_ALLOW_RESTARTS = "default_allow_restarts";
		public static final String COLUMN_DEFAULT_EMAIL_EXPIRATION_DAYS = "default_email_expiration_days";
		public static final String COLUMN_DEFAULT_EMAIL_GROOM_DAYS = "default_email_groom_days";
		public static final String COLUMN_DEFAULT_EMAIL_MAX_SEND_ATTEMPTS = "default_max_email_attempts";
		public static final String COLUMN_DEFAULT_EMAIL_SEND_WAIT_DAYS = "default_email_send_wait_days";
		public static final String COLUMN_ENABLED = "is_enabled";
		public static final String COLUMN_LOGO = "logo";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_USE_OWNER = "use_owner";		
		public static final String COLUMN_USE_USER = "use_user";
	}
	
	public static interface ReclamationComputer 
	{
		public static final String TABLE_NAME = "opz_reclamation_computer";
		public static final String COLUMN_COMPUTER = "computer";
		public static final String COLUMN_EMAIL_CONFIRMED = "email_confirmed"; // Confirmed to submit to sccm
		public static final String COLUMN_EMAIL_SEND_ATTEMPTS = "email_send_attempts";		
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_RECLAMATION_ID = "reclamation";	
		public static final String COLUMN_SIGNATURE_ID = "signature";
		public static final String COLUMN_TAKE_NO_RESPONE_ACTION = "take_no_response_action";
		public static final String COLUMN_USER_RESPONSE = "user_response";
	}
	
	public static interface ReclamationEmail
	{
		public static final String TABLE_NAME = "opz_reclamation_email";
		public static final String COLUMN_CONTACT_ATTEMPT = "contact_attempt";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_RECLAMATION_COMPUTER = "reclamation_computer";
		public static final String COLUMN_EMAIL_RESPONSE_TIMESTAMP = "response_time";
		public static final String COLUMN_EMAIL_SENT_TIMESTAMP = "email_sent_time";
		public static final String COLUMN_SYSTEM_EMAIL = "system_email";
		public static final String COLUMN_TOKEN = "token";
	}
	
	public static interface ReclamationEmailConfig 
	{
		public static final String TABLE_NAME = "opz_reclamation_email_config";
		public static final String COLUMN_EMAIL_BODY = "body";
		public static final String COLUMN_ID = "id";		
		public static final String COLUMN_EMAIL_SUBJECT = "subject";
		public static final String COLUMN_TEMPLATE = "template";
	}
    
	public static interface ReclamationJob 
    {
        public static final String TABLE_NAME = "sys_sch_reclamation_job";        
    }
	
	public static interface ReclamationServletInitJob
	{
		final String TABLE_NAME = "sys_sch_reclam_svc_init_job"; 
	}
	
	public static interface SCCMServerConfig 
	{
		public static final String TABLE_NAME = "opz_reclamation_sccm_config";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_CREATED_ON = "created_on";
		public static final String COLUMN_SCCM_CONFIG_ID = "id";
		public static final String COLUMN_SCCM_PAD = "pad";
		public static final String COLUMN_SCCM_SITE_SERVER = "site_server";
		public static final String COLUMN_SCCM_SITE_CODE = "site_code";		
	}
	
	public static interface SoftwarePackages 
	{
		public static final String TABLE_NAME = "cmdb_ci_spkg";
		public static final String COLUMN_FRIENDLY_NAME = "friendly_name";		
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_INSTALLED_ON = "installed_on";
		public static final String COLUMN_SPV = "software";
		public static final String COLUMN_SYS_PARENT_ID = "sys_parent_id";		
		public static final String COLUMN_VERSION = "version";		
	}
	
	public static interface SoftwarePackagesWindows
	{
		public static final String TABLE_NAME = "cmdb_ci_spkg_windows";
		public static final String COLUMN_FRIENDLY_NAME = "friendly_name";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_INSTALLED_ON = "installed_on";
		public static final String COLUMN_OPERATIONAL = "operational";
		public static final String COLUMN_SIGNATURE_GUID = "package_guid";
		public static final String COLUMN_SOFTWARE = "software";
		public static final String COLUMN_SYS_PARENT_ID = "sys_parent_id";		
	}	
	
	public static interface SoftwareProductVersion
	{
		public static final String TABLE_NAME = "cmdb_software_product_version";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_FRIENDLY_NAME = "friendly_name";
	}
	
	public static interface UIDashboardEmailConfig
	{
		public static final String TABLE_NAME = "ui_dashboard_email_config";
	}
	
	public static interface User
	{
		public static final String TABLE_NAME = "cmn_user";
		public static final String COLUMN_ID = "id";		
		public static final String COLUMN_EMAIL = "email";
	}
	
	public static interface SystemVersion
	{
		public static final String TABLE_NAME = "sys_version";
		public static final String COLUMN_CURRENCY = "local_currency";
	}	
}
