/**
 *
 */
package com.organization.live.survey.reclamation.ui.installer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;

import com.organization.live.db.DbApi;
import com.organization.live.db.transaction.Transaction;
import com.organization.live.db.transaction.TransactionCallback;
import com.organization.live.db.transaction.TransactionDefinition;
import com.organization.live.init.AbstractInitializer;
import com.organization.live.installer.InstallationContext;
import com.organization.live.installer.InstallationException;
import com.organization.live.plugin.Installable;
import com.organization.live.survey.reclamation.ui.Views;
import com.organization.live.survey.reclamation.ui.Menus;
import com.organization.live.ui.service.ServiceFactory;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.customization.CustomizationContext;
import com.organization.live.ui.service.dataview.info.AggregatedColumn;
import com.organization.live.ui.service.dataview.info.CalculatedColumn;
import com.organization.live.ui.service.dataview.info.DataViewInfo;
import com.organization.live.ui.service.dataview.info.FieldInfo;
import com.organization.live.ui.service.entity.UIEntity;
import com.organization.live.ui.service.menu.MenuItem;
import com.organization.live.ui.service.menu.MenuService;
import com.organization.live.ui.service.presentation.PresentationService;
import com.organization.live.ui.service.user.role.MenuAccess;
import com.organization.live.ui.service.user.role.PermissionType;
import com.organization.live.ui.service.user.role.RoleService;
import com.organization.live.ui.service.validation.Validator;
import com.organization.live.ui.service.validation.ValidatorType;
import com.organization.live.ui.service.xml.XmlService;
import com.organization.live.ui.system.component.dataview.DataViewAction;


/**
 * Installs the UI for Reclamatiom
 *
 * @author Danny Carvajal
 *
 */
public class Installer extends AbstractInitializer implements Installable, BundleContextAware
{
	private static BundleContext bundleContext;
	private static Logger logger = LoggerFactory.getLogger(Installer.class);
    private ServiceFactory serviceFactory = null;
	private static final String ROLE_USER = "user";
	
	private static final String RECLAMATION_MENU = "/res/menus/menus.xml";
	private static final String RECLAMATION_MENU_SETUP = "/res/menus/menus_setup.xml";
	private static final String RECLAMATION_VIEW = "/res/views";

	
    /**
	 * Overrides {@link ServiceFactory}
	 *
	 * @param serviceFactory
	 */
	public void setServiceFactory(final ServiceFactory serviceFactory)
	{
		this.serviceFactory = serviceFactory;
	}	
	
	
    @Override
    public boolean install(final InstallationContext installerContext) 
    throws InstallationException  
    {
        final DbApi dbApi = installerContext.getDbApi();
		final TransactionDefinition definition = new TransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        final ServiceLocator serviceLocator = serviceFactory.getServiceLocator(installerContext.getSecurityTokenString(), dbApi);		
		
		
        final TransactionCallback<Void> callback = new TransactionCallback<Void>() 
        		{
            @Override
            public Void doInTransaction(final Transaction tx) throws Exception 
            {           	
				logger.info("Importing Reclamation Menus and Views");

				doUpgradePreProcessing(dbApi, serviceLocator);
				
				// Import menus from XML
				createAppAdminMenu(serviceLocator);
				importMenus(RECLAMATION_MENU, serviceLocator);
				importMenus(RECLAMATION_MENU_SETUP, serviceLocator);
            	
				// Create stand-alone views
				importUIXML(bundleContext, this.getClass(), RECLAMATION_VIEW,
					DataViewInfo.class, true, serviceLocator);
				
				// Perform post processing
				doUpgradePostProcessing(dbApi, serviceLocator);
				
            	return (null);
            }
        };

        dbApi.getThreadContext().transaction("LiveSurvey Reclamation Views installation...", definition, callback);
        
        return false;
    }


	/**
	 * Create setup>app administration menu
	 *
	 * @param serviceFactory
	 */    
    protected void createAppAdminMenu(final ServiceLocator serviceLocator) 
    {
        final PresentationService presentationService = serviceLocator.getPresentationService();
        final Long adminID = presentationService.getAdministrationMenuItem(serviceLocator).getId();
        presentationService.getInnerHTMLMenuItem("App Administration", adminID,
            "<div id=\"appadministration_hype_container\" style=\"position:relative;overflow:hidden;width:1035px;height:550px\"><script type=\"text/javascript\" charset=\"utf-8\" src=\"https://s3.amazonaws.com/helpresources/Setup.hyperesources/appadministration_hype_generated_script.js\"></script></div>",
            5l, "icon-admin", serviceLocator);
    }
    
    
	/**
	 * Perform some pre-upgrade tasks
	 *
	 * @param dbApi
	 * @param serviceFactory
	 */
	private void doUpgradePreProcessing(final DbApi dbApi, final ServiceLocator serviceLocator) throws InstallationException
	{
		// Remove managed calculated column from two views. These are now to be aggregated columns.
		List<String> viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_COMPUTER_PRIMARY_EMAIL);
		viewList.add(Views.Reclamation.RECLAMATION_WIN_SPKG);
		
		for (String viewName : viewList)
		{
			DataViewInfo dataViewInfo = serviceLocator.getDataViewInfoService().getDataViewInfo(viewName, serviceLocator);
			if (dataViewInfo != null)
			{
		        FieldInfo calcFieldInfo = dataViewInfo.getCalculatedFieldInfo("managed");
		        if (calcFieldInfo != null )
		        {
		        	CalculatedColumn calcColumn = calcFieldInfo.getCalculatedColumn();
		        	if (calcColumn != null) calcColumn.delete();
		        }
			}
		}
	}
	
    
	/**
	 * Perform some post-upgrade tasks
	 *
	 * @param dbApi
	 * @param serviceFactory
	 */
	private void doUpgradePostProcessing(final DbApi dbApi, final ServiceLocator serviceLocator) throws InstallationException
	{
		// Remove actions from most reclamation views
		List<String> actionList = new ArrayList<String>();
		actionList.add(DataViewAction.CREATE_NEW_DATA_VIEW_ACTION);	
		actionList.add(DataViewAction.EDIT_DATA_VIEW_ACTION);
		actionList.add(DataViewAction.DELETE_DATA_VIEWS_ACTION);
		actionList.add(DataViewAction.SAVE_DATA_VIEWS_ACTION);
		actionList.add("CreateImportAction");
		
		List<String> viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_ACTIVITY_LOGS);
		viewList.add(Views.Reclamation.RECLAMATION_COMPUTERS);
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_COLLECTIONS);
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_COMPUTER_PRIMARY_EMAIL);
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_COMPUTERS);
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_PACKAGES);
		viewList.add(Views.Reclamation.RECLAMATION_MANAGED_PROGRAMS);
		viewList.add(Views.Reclamation.RECLAMATION_PLAN_VERIFICATION);
		viewList.add(Views.Reclamation.RECLAMATION_SIGNATURES_BY_CANDIDATE);
		viewList.add(Views.Reclamation.RECLAMATION_SOFTWARE_BY_SPV);
		viewList.add(Views.Reclamation.RECLAMATION_SUMMARY);
		viewList.add(Views.Reclamation.RECLAMATION_SUMMARY_BY_SPV);
		viewList.add(Views.Reclamation.RECLAMATION_USAGE_PROGRAM_INSTANCE);
		viewList.add(Views.Reclamation.RECLAMATION_WIN_SPKG);
		
		excludeActions(actionList, viewList, serviceLocator);
				
		// Remove the delete action from the configuration and reclamation plan views
		actionList =  new ArrayList<String>();
		actionList.add(DataViewAction.CREATE_NEW_DATA_VIEW_ACTION);
		actionList.add(DataViewAction.DELETE_DATA_VIEWS_ACTION);
		
		viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_CONFIGURATION);
		viewList.add(Views.Reclamation.RECLAMATIONS);
		
		excludeActions(actionList, viewList, serviceLocator);
				
		// Remove save, edit, new from Reclamation Plans user view
		actionList.add(DataViewAction.EDIT_DATA_VIEW_ACTION);
		actionList.add(DataViewAction.SAVE_DATA_VIEWS_ACTION);
		
		viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_PLANS);
		
		excludeActions(actionList, viewList, serviceLocator);
		
		// Remove ReNormalize button from Reclamation Summary by Product Version
		actionList =  new ArrayList<String>();
		actionList.add("ReNormalizeAction");
		
		viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_SUMMARY_BY_SPV);
		viewList.add(Views.Reclamation.RECLAMATION_SOFTWARE_BY_SPV);
		
		excludeActions(actionList, viewList, serviceLocator);
		
		// Remove Uninstall Packages from Reclamation Signatures by Candidate
		actionList = new ArrayList<String>();
		actionList.add("PreUninstallWindowsPackageAction");
		
		viewList = new ArrayList<String>();
		viewList.add(Views.Reclamation.RECLAMATION_SIGNATURES_BY_CANDIDATE);
		viewList.add(Views.Reclamation.RECLAMATION_WIN_SPKG);
		
		excludeActions(actionList, viewList, serviceLocator);			
		
		// Add menu access to reclamation menu
		String menuName = String.format("%s>%s", Menus.Optimize.MENU_TITLE,
				Menus.Optimize.SoftwareReclamation.MENU_TITLE);
		
		final MenuItem menuOptimizeRenamed = serviceLocator.getMenuService().getMenuItem(menuName, serviceLocator);
		addMenuAccess(menuOptimizeRenamed.getId(), ROLE_USER, PermissionType.ALLOW,
				serviceLocator);
		
		// Remove Reclamation Windows Software Signatures.programs_used agg. column
		DataViewInfo dataViewInfo = serviceLocator.getDataViewInfoService().getDataViewInfo(Views.Reclamation.RECLAMATION_WIN_SPKG, serviceLocator);
		if (dataViewInfo != null)
		{
	        FieldInfo aggFieldInfo = dataViewInfo.getAggregatedFieldInfo("program_used");
	        if (aggFieldInfo != null )
	        {
	        	AggregatedColumn aggColumn = aggFieldInfo.getAggregatedColumn();
	        	if (aggColumn != null) aggColumn.delete();
	        }
		}
		
		// Install table validator scripts for config table
		final ValidatorType validatorType =
                serviceLocator.getMetaDataService().getValidatorType(ValidatorType.SCRIPT, serviceLocator);
        if (validatorType == null)        	
        {
            logger.error("Script validator not installed");
        } 
        else 
        {		
			Validator validator =
	                new Validator(
	                        validatorType.getId(),
	                        "var other = 'base.opz_reclamation_config.use_owner'; "
										+ "if($ctx.property == 'base.opz_reclamation_config.use_owner'){ "
										+ "other = 'base.opz_reclamation_config.use_user';} "
										+ "var otherValue = $ctx.values[other]; "
										+ "var thisValue = $ctx.value; "
										+ "if (thisValue == 'false' && otherValue == 'false'){ "
										+ "$ctx.setFieldValue($ctx.property, 'true'); "
										+ "var m = 'Either Use Owner or Use Primary User must be selected.'; "
										+ "$wnd.SL.message('errorMessage', 'ERROR', m); "
										+ "m;} "
										+ "else {true;}");
			
	        serviceLocator.getMetaDataService().addValidator("opz_reclamation_config", "use_owner", validator,
	                serviceLocator);		
	        
	        serviceLocator.getMetaDataService().addValidator("opz_reclamation_config", "use_user", validator,
	                serviceLocator);
        }
	}
	
    

	private void excludeActions(List<String> actionList, List<String> viewList, ServiceLocator serviceLocator)
	{
		for (String viewName : viewList)
		{
			Long viewID = serviceLocator.getDataViewInfoService().getDataViewInfoId(viewName, serviceLocator);
			serviceLocator.getDataViewInfoService().excludeActions(viewID, actionList, serviceLocator);
		}
		
	}
	
	
    /*
     * Imports a set of UI XML files within the specified path from the bundle.
     *
     * @param path {@link path} of xml resource
     * @param serviceLocator {@link ServiceLocator} object
     * @throws IOException
     */
    private void importMenus(String menuResource, final ServiceLocator serviceLocator)
    throws InstallationException 
    {
		InputStream in = null;
		try 
		{
		    in = getClass().getClassLoader().getResourceAsStream(menuResource);
		    MenuItem menu = serviceLocator.getXmlService().toEntity(in, MenuItem.class, serviceLocator);
		    serviceLocator.getMenuService().saveMenuItem(menu, serviceLocator);
		} 
		catch (IOException e) 
		{
		    throw new InstallationException(e);
		} 
		finally 
		{
		    IOUtils.closeQuietly(in);
		}
    }
    
    
    /**
     * Adds a permission to the specified menu.
     * 
     * @param menuID
     * @param role
     * @param permission
     * @param serviceLocator
     * @return
     */
    public static MenuAccess addMenuAccess(long menuID, String role, PermissionType permission, ServiceLocator serviceLocator)
    {
    	final RoleService roleService = serviceLocator.getRoleService();
    	final MenuService menuService = serviceLocator.getMenuService();
    	final MenuItem menuItem = menuService.getMenuItem(menuID, CustomizationContext.All(), serviceLocator);
    	MenuAccess access = roleService.addMenuAccess(menuItem, role, permission, serviceLocator);

    	return (access);
    }  
    
    
    /*
     * Imports a set of UI XML files within the specified path from the bundle.
     *
     * @param bundleContext {@link BundleContext} of the calling bundle
     * @param clazzBundle class of the bundle containing the path
     * @param path path to the files to be imported
     * @param clazzUI UI object class being imported
     * @param goDeep if true, sub-folders will be imported as well
     * @param serviceLocator {@link ServiceLocator} object
     * @throws IOException
     */
	public static <T extends UIEntity> void importUIXML(final BundleContext bundleContext, 
			final Class<?> clazzBundle, 
			final String path,
			final Class<T> clazzUI, 
			final boolean goDeep, 
			final ServiceLocator serviceLocator) 
	throws IOException
    {
		Enumeration<String> paths = bundleContext.getBundle().getEntryPaths(path);
    	XmlService xmlService = serviceLocator.getXmlService();

    	if (paths == null) 
    	{
    		logger.info("paths is null");
    	}

    	if (xmlService == null) 
    	{
    		logger.info("xmlService is null");
    	}

    	while (paths.hasMoreElements())
    	{
    		String fileName = "/" + paths.nextElement();

    		if (fileName.charAt(fileName.length() - 1) == '/')
    		{
    			if (goDeep) 
    			{
                    importUIXML(bundleContext, clazzBundle, fileName, clazzUI, goDeep, serviceLocator);
                }
    		}

    		else
    		{
    			logger.info("Beginning import of " + fileName);

    			xmlService.toEntity(clazzBundle.getResourceAsStream(fileName), clazzUI, serviceLocator);

    			logger.info("Completed import of " + fileName);
    		}
    	}
    }

	
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.osgi.context.BundleContextAware#setBundleContext(
	 * org.osgi.framework.BundleContext)
	 */
	@Override
	public void setBundleContext(final BundleContext bundleContext)
	{
		Installer.bundleContext = bundleContext;
	}	 
}
