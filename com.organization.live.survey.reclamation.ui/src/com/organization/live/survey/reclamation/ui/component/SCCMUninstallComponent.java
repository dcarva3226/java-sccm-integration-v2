package com.organization.live.survey.reclamation.ui.component;

/**
 * 
 * @author Danny Carvajal
 *
 */
public interface SCCMUninstallComponent extends SCCMComponent
{
	public static final String ADVERTISEMENT_NAME = "advertisement_name";
	public static final String ADVERTISEMENT_DESC = "advertisement_description";	
	public static final String CNAME_PACKAGE_NAME = "package_name";
	public static final String COLLECTION_NAME = "collection_name";
	public static final String COLLECTION_DESC = "collection_description";
	public static final String COMPONENT_NAME = "SCCM Reclamation Uninstall Component";
	public static final String CONFIG_PACKAGE_NAME = "SCCMPackageName";
	public static final String FOLDER_NAME = "Asset Vision";
	public static final String PACKAGE_ID = "package_id";
	public static final String PACKAGE_GUID = "package_guid";
	public static final String PACKAGE_NAME = "package_name";
	public static final String PROGRAM_NAME = "program_name";
	public static final String READY_FOR_SCCM = "ready_for_sccm";
	public static final String SCCM_PLATFORM = "SMS/SCCM";
	public static final String SITE_CODE = "site_code";
	public static final String SUBCOLLETION_NAME = "Asset Vision";
	public static final String SUBCOLLETION_DESC = "Collection used for Asset Vision operations";
	public static final int UNMANAGED_THRESHOLD = 5;
	
	public static final String STATE_PANEL = "SCCMUninstallerPanel";
	public static final String MODEL_PANEL = "SCCMUninstallerComponent";
	public static final String MODEL_FORM_MAIN_RUN = "SCCMUninstallerRunFormMain";
	public static final String MODEL_FORM_CREATE_RUN = "SCCMUninstallerCreateForm";

	public static final String HEADER_ICON = "icon-commercial_software";
	public static final String HEADER_DEFAULT_TEXT = "Submit Reclamation Plan to SMS/SCCM";
	    
    public static String PAD_NAME = "PAD name";
    public static String PAD_NAME_HINT = "Select a PAD...";
    public static String PAD_NAME_LABEL = "Select a PAD";
}
