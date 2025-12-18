package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.survey.reclamation.ui.action.FinishReclamationAction;
import com.organization.live.survey.reclamation.ui.component.scene1.Scene1Initializer;
import com.organization.live.survey.reclamation.ui.component.scene2.Scene2Initializer;
import com.organization.live.survey.reclamation.ui.component.scene3.Scene3Initializer;
import com.organization.live.survey.reclamation.ui.component.scene4.Scene4Initializer;
import com.organization.live.survey.reclamation.ui.component.scene5.Scene5Initializer;
import com.organization.live.survey.reclamation.ui.component.scene5.SetEmailPropertiesAction;
import com.organization.live.survey.reclamation.ui.component.scene6.Scene6Initializer;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.system.action.executionsource.ComponentExecutionSource;

/**
 * @author Danny Carvajal - example used was com.sclable.live.impex.component.ImportMapping.java
 *
 */
public interface ReclamationPlanComponent 
{
	
    String COMPONENT_NAME = "Reclamation Plan Wizard";
    ExecutionSource wizardExecutionSource = new ComponentExecutionSource(COMPONENT_NAME);
    String SELECTION_CONTEXT = "Selection context";
    
    String FINISH_ACTION = FinishReclamationAction.class.getSimpleName();
    String LOG_DESCRIPTION = "Reclamation Plan Activity Log ";
    String LOG_PREFIX = "reclamation:";
    String RECLAMATION_DATAVIEW_ID 	= "Reclamation Data View Id";
    String REQUIRE_PACKAGE = "require_package";
    String SCCM_PLATFORM = "SMS/SCCM";
    String SCCM_PACKAGE_NAME_PREFIX = "Pseudo-package for uninstall of";
    String SELECTION_CHECK_CHANGED_LISTENER_ACTION = "SelectionCheckChangedListenerAction";
    String SELECTION_DATA_LOAD_LISTENER_ACTION = "SelectionDataLoadListenerAction";
    String SERVERS_UPDATED = "servers_updated";
    String SOURCE_MODEL = "Source Model";

    // Scene titles
    String SCENE_TITLE_CONNECTION = "Connection";
    String SCENE_TITLE_SELECT_COMPUTERS = "Computers";
    String SCENE_TITLE_DEPLOYMENTS = "Deployments";
    String SCENE_TITLE_CONFIGURATION = "Configuration";
    String SCENE_TITLE_EMAIL = "Emails";
    String SCENE_TITLE_REVIEW_SAVE = "Review and Save";    

    // Scenes Initializers
    String SCENE1_INITIALIZER = Scene1Initializer.class.getSimpleName();
    String SCENE2_INITIALIZER = Scene2Initializer.class.getSimpleName();
    String SCENE3_INITIALIZER = Scene3Initializer.class.getSimpleName();
    String SCENE4_INITIALIZER = Scene4Initializer.class.getSimpleName();
    String SCENE5_INITIALIZER = Scene5Initializer.class.getSimpleName();
    String SCENE6_INITIALIZER = Scene6Initializer.class.getSimpleName();

    // Scene Icons
    String SCENE1_ICON = "laptop_docking_station";
    String SCENE2_ICON = "connection_string";
    String SCENE3_ICON = "network_software";
    String SCENE4_ICON = "configuration";
    String SCENE5_ICON = "mail";
    String SCENE6_ICON = "register";
    
    // Scene 1
    String SCENE_SELECT_COMPUTERS_FORM = "scene_select_computers_form";
    String SCENE_SELECT_COMPUTERS_FORM_HEADING = "Users of computers, that are reclamation candidates, will receive notification emails.";
    String DATA_PREVIEW = "Reclamation Data Preview";
    String COLUMN_COMPUTER = "Computer";
    String COLUMN_EMAIL = "Email";
    String COLUMN_COMPUTER_ID = "ComputerId";
    String COMPUTER_PRIMARY_EMAIL = "Reclamation Computer Status";
    String SIGNATURE_GUID = "signature_guid";
    String SIGNATURE_ID = "signature";
    String SIGNATURE_NAME = "signature_name";
    String SIGNATURE_SPV = "signature_spv";
    String PRODUCT_COST = "cost";
    
    // Scene 2
    String SCENE_CONNECTION_FORM = "scene_connection_form" ;
    String SCENE_CONNECTION_FORM_HEADING = "Specify the SMS/SCCM Connection Details";
    String SCCM_CONFIG_ID = "SCCM Config ID";
    String SCCM_CONFIG_SERVER_ID = "server_id";
    String SCCM_SERVER = "Site Server";
    String SCCM_SERVER_LABEL = "Specify site server";
    String SCCM_SERVER_HINT = "Specify the site server IP address...";
    String SESSION_USERID = "session_user_id";
    String SITE_CODE = "Specify site code";
    String SITE_CODE_LABEL = "Specify site code";
    String SITE_CODE_HINT = "The site code for the site server...";
    
    // Scene 3 - in SCCM 2012, deployments are advertisements
    String SCENE_DEPLOYMENTS_FORM = "scene_deployment_form";
    String SCENE_DEPLOYMENTS_FORM_HEADING = "Specify the Advertisement (Deployment) Details";
    String COLLECTION_NAME = "Device collection name";
    String COLLECTION_NAME_HINT = "The name device collection to submit to SMS/SCCM.";
    String COLLECTION_NAME_LABEL = "Enter device collection name...";
    String COLLECTION_DESC = "Device collection description";
    String COLLECTION_DESC_LABEL = "Enter optional device collection description...";    
    String PLAN_ALLOW_ADVERTISEMENTS = "Allow Advertisments";   
    Boolean PLAN_ALLOW_ADVERTISEMENTS_DEFAULT = true;
    String PLAN_ALLOW_ADVERTISEMENTS_LABEL = "Allow advertisments";
    String PLAN_ALLOW_ADVERTISEMENTS_HINT = "Allow Advertisments (Deployments) to be created...";  
    String PLAN_REQUIRE_ADVERTISEMENTS = "Require Advertisment";
    Boolean PLAN_REQUIRE_ADVERTISEMENTS_DEFAULT = false;
    String PLAN_REQUIRE_ADVERTISEMENTS_LABEL = "Mandatory advertisments";
    String PLAN_REQUIRE_ADVERTISEMENTS_HINT = "Make advertisement mandatory. For SCCM 2012 only (and up)";      
    String DEPLOYMENT_NAME = "Advertisement name";
    String DEPLOYMENT_NAME_HINT = "The name of the advertisement (deployment) to submit to SMS/SCCM...";
    String DEPLOYMENT_NAME_LABEL = "Enter advertisement name...";
    String DEPLOYMENT_DESC = "Advertisement description";
    String DEPLOYMENT_DESC_LABEL = "Enter optional advertisement description...";
    String PACKAGE_NAME = "Package name";
    String PACKAGE_NAME_LABEL = "Select SMS/SCCM package";
    String PACKAGE_NAME_GENERATED_LABEL = "Package will be generated...";    
    String PACKAGE_NAME_HINT = "Select from a list of packages available on the SMS/SCCM site...";
    String PROGRAM_NAME = "Program name";
    String PROGRAM_NAME_LABEL = "Select SMS/SCCM program";
    String PROGRAM_NAME_GENERATED_LABEL = "Program will be generated...";
    String PROGRAM_NAME_HINT = "Select from a list of programs, within a package, available on the SCCM site...";
    String PROGRAM_DATA_LOADER = "SetProgramsDataLoader";
    String RECLAMATION_GENERATED_PACKAGE_DESC = "Pseudo-package for uninstall of%";
    String PLAN_ALLOW_RESTARTS = "Allow computer restarts";   
    String PLAN_ALLOW_RESTARTS_LABEL = "Allow computer restarts";
    String PLAN_ALLOW_RESTARTS_HINT = "Check to allow computer restarts when advertisement is run on target computer";
    
    // Scene 4
    String SCENE_CONFIGURATION_FORM = "scene_configuration_form";
    String SCENE_CONFIGURATION_FORM_HEADING = "Specify Reclamation Plan Configuration Details"; 
    String PLAN_NAME = "Reclamation plan name";
    String PLAN_NAME_LABEL = "Enter reclamation plan name...";
    String PLAN_DESC = "Plan description";
    String PLAN_DESC_LABEL = "Enter reclamation plan description...";
    String PLAN_EMAIL = "Plan email";
    String PLAN_EMAIL_LABEL = "Enter email for sender...";
    String PLAN_EMAIL_EXP_DAYS = "Email expiration days";
    String PLAN_EMAIL_EXP_DAYS_LABEL = "Enter email expiration days...";
    String PLAN_EMAIL_EXP_DAYS_HINT = "This is the number of days to consider an email request as expired...";
    String PLAN_EMAIL_KEEP_DAYS = "Keep email days";
    String PLAN_EMAIL_KEEP_DAYS_LABEL = "Enter email keep days...";    
    String PLAN_EMAIL_KEEP_DAYS_HINT = "Enter the number of days to keep processed email record before grooming them...";
    String PLAN_EMAIL_SEND_ATTEMPTS = "Max email attempts";
    String PLAN_EMAIL_SEND_ATTEMPTS_LABEL = "Enter max email attempts...";    
    String PLAN_EMAIL_SEND_ATTEMPTS_HINT = "Enter the number of email attempts to obtain a reponse from a user...";
    String PLAN_EMAIL_SEND_WAIT_DAYS = "Send email wait days";    
    String PLAN_EMAIL_SEND_WAIT_DAYS_LABEL = "Enter send email wait days...";    
    String PLAN_EMAIL_SEND_WAIT_DAYS_HINT = "Enter the number of days to wait before we send emails...";    
    String PLAN_NO_RESPONSE_ACTION = "No Response Action";   
    String PLAN_NO_RESPONSE_ACTION_LABEL = "No response action";
    String PLAN_NO_RESPONSE_ACTION_HINT = "What action to take if user never responds to uninstall request...";  
    String PLAN_NO_RESPONSE_ACTION_1 = "Do not uninstall";
    String PLAN_NO_RESPONSE_ACTION_2 = "Proceed to uninstall";
    int PLAN_NO_RESPONSE_ACTION_IDX_1 = 1;
    int PLAN_NO_RESPONSE_ACTION_IDX_2 = 2;    
        
    // Scene 5
    String SCENE_EMAIL_FORM = "scene_email_form";
    String SCENE_EMAIL_FORM_MSG = "scene_email_form_msg";
    String SCENE_EMAIL_FORM_HEADING = "Specify Reclamation Email Properties";
    String SET_EMAIL_PROPERTIES_ACTION = SetEmailPropertiesAction.class.getSimpleName();
    String EMAIL_TEMPLATE = "Email Templates";
    String EMAIL_TEMPLATE_HINT = "A previously saved email template.";
    String EMAIL_TEMPLATE_LABEL = "Select existing template";
    String EMAIL_TEMPLATE_EMPTY_TEXT = "Select existing email template if desired...";
    String EMAIL_SUBJECT = "Email subject";
    String EMAIL_SUBJECT_HINT = "The subject used for the reclamation email process. Select a previous email subject/body or enter a new one.";
    String EMAIL_SUBJECT_LABEL = "Enter email subject";
    String EMAIL_SUBJECT_EMPTY_TEXT = "Enter New Subject or Choose Existing...";
    String EMAIL_BODY = "Email body text";
    String EMAIL_BODY_HINT = "The body text used for the reclamation email process.";
    String EMAIL_BODY_LABEL = "   Enter email body text...";
    String EMAIL_LABEL = "Enter admin email for plan...";
    String EMAIL_HTML = "HTML Format";
    String EMAIL_HTML_LABEL = "Use HTML format";
    String EMAIL_HTML_HINT = "Use HTML format vs text Format...";
    String EMAIL_SAVE_TEMPLATE = "Save Email Template";
    String EMAIL_SAVE_TEMPLATE_LABEL = "Save email template";
    String EMAIL_SAVE_TEMPLATE_HINT = "Save this email as a template...";
    String EMAIL_TEMPLATE_ID = "email_template_id";
    String EMAIL_TEMPLATE_NONE = "none";
    
    // Scene 6
    String SCENE_REVIEW_SAVE_FORM = "scene_review_save_form";
    String SCENE_REVIEW_SAVE_FORM_HEADING = "Review your reclamation plan settings and save.";  
    
    // Logger tables
    public static interface LoggerTables 
    {
        final String LOGGER_DETAIL = "opz_reclamation_activity_log";
        final String RECLAMATION_PLAN_ID = "reclamation";
    }
}
