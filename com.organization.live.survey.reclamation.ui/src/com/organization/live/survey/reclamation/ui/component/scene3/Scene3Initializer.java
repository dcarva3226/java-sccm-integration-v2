package com.organization.live.survey.reclamation.ui.component.scene3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.event.widget.FieldValue;
import com.organization.live.ui.common.client.type.ViewType;
import com.organization.live.ui.common.client.view.info.BooleanViewInfo;
import com.organization.live.ui.common.client.view.info.MetaModel;
import com.organization.live.ui.common.client.view.info.SelectionViewInfo;
import com.organization.live.ui.common.client.view.info.ViewInfo;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.service.ServiceLocator;

/**
 * @author Danny Carvajal
 *
 */
public class Scene3Initializer extends AbstractReclamationAction 
{
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
				
		final MetaModel formMetaModel = getFormMetaModel(serviceLocator, model);		
		final Data formData = getFormData(model);
		FormModel formModel = model.get(SCENE_DEPLOYMENTS_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_DEPLOYMENTS_FORM);
        
        if (formModel == null) 
        {
            formModel = new FormModel(formDescriptor);
            formModel.setHeading(SCENE_DEPLOYMENTS_FORM_HEADING);
        }
        formModel.setMetaModel(formMetaModel);
        model.set(formModel);
        formModel.setDataItem(formData);
		return model;
	}
	
	private MetaModel getFormMetaModel(final ServiceLocator serviceLocator, final ComponentModel model) 
	{
		
		final MetaModel metaModel = new MetaModel();      
        final String style = "margin-left:10px";
		final boolean requirePackage = getRequirePackage(model);
        
        final ViewInfo collectionName = new ViewInfo(COLLECTION_NAME, ViewType.TEXT_LINE);        
        collectionName.setEmptyText(COLLECTION_NAME_LABEL);
        collectionName.setHint(COLLECTION_NAME_HINT);
        collectionName.setMaxLength(255);
        collectionName.setRequired(true);
        collectionName.setStyle(style);
        collectionName.setWidth(325);
        metaModel.add(collectionName);    		
		
        final ViewInfo collectionDesc = new ViewInfo(COLLECTION_DESC, ViewType.TEXT);        
        collectionDesc.setEmptyText(COLLECTION_DESC_LABEL);
        collectionDesc.setForceLineEmbed(true);
        collectionDesc.setHeight(60);
        collectionDesc.setMaxLength(1000);        
        collectionDesc.setRequired(false);
        collectionDesc.setStyle(style);      
        collectionDesc.setWidth(325);
        metaModel.add(collectionDesc);  
               
        final BooleanViewInfo allowAdvertisements = new BooleanViewInfo(PLAN_ALLOW_ADVERTISEMENTS);
        allowAdvertisements.setDefaultValue(PLAN_ALLOW_ADVERTISEMENTS_DEFAULT);
        allowAdvertisements.setEnabled(true);
        allowAdvertisements.setForceLineEmbed(true);        
        allowAdvertisements.setHint(PLAN_ALLOW_ADVERTISEMENTS_HINT); 
        allowAdvertisements.setLabel(PLAN_ALLOW_ADVERTISEMENTS_LABEL);        
        allowAdvertisements.setStyle(style);     
        allowAdvertisements.setWidth(325);
        metaModel.add(allowAdvertisements);
        
        final BooleanViewInfo requireAdvertisements = new BooleanViewInfo(PLAN_REQUIRE_ADVERTISEMENTS);
        requireAdvertisements.setDefaultValue(PLAN_REQUIRE_ADVERTISEMENTS_DEFAULT);
        requireAdvertisements.setEnabled(true);
        requireAdvertisements.setForceLineEmbed(true);        
        requireAdvertisements.setHint(PLAN_REQUIRE_ADVERTISEMENTS_HINT); 
        requireAdvertisements.setLabel(PLAN_REQUIRE_ADVERTISEMENTS_LABEL);        
        requireAdvertisements.setStyle(style);     
        requireAdvertisements.setWidth(325);
        requireAdvertisements.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).hide();
        requireAdvertisements.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(true)).show();       
        metaModel.add(requireAdvertisements);        
        
        final ViewInfo deploymentName = new ViewInfo(DEPLOYMENT_NAME, ViewType.TEXT_LINE);
        deploymentName.setEmptyText(DEPLOYMENT_NAME_LABEL);
        deploymentName.setHint(DEPLOYMENT_NAME_HINT);        
        deploymentName.setMaxLength(255);
        deploymentName.setStyle(style);
        deploymentName.setWidth(325);
        deploymentName.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).hide();
        deploymentName.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(true)).show();
        deploymentName.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).setValue("");
        metaModel.add(deploymentName);    		
		
        final ViewInfo deploymentDesc = new ViewInfo(DEPLOYMENT_DESC, ViewType.TEXT);        
        deploymentDesc.setEmptyText(DEPLOYMENT_DESC_LABEL);
        deploymentDesc.setForceLineEmbed(true);
        deploymentDesc.setHeight(60);
        deploymentDesc.setMaxLength(1000);        
        deploymentDesc.setRequired(false);
        deploymentDesc.setStyle(style);    
        deploymentDesc.setWidth(325);
        deploymentDesc.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).hide();
        deploymentDesc.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(true)).show();
        deploymentDesc.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).setValue("");
        metaModel.add(deploymentDesc);  
        
        if (requirePackage)
        {
	        final SelectionViewInfo<String> packageName = new SelectionViewInfo<String>(getPackages(serviceLocator), PACKAGE_NAME);
	        packageName.setColumn(2);
	        packageName.setHint(PACKAGE_NAME_HINT);
	        packageName.setLabel(PACKAGE_NAME_LABEL);
	        packageName.setRequired(true);
	        packageName.setStyle(style);
	        packageName.setWidth(325);
	        metaModel.add(packageName);	        
	                      
	        final SelectionViewInfo<Long> programName = new SelectionViewInfo<Long>(PROGRAM_NAME, PROGRAM_DATA_LOADER);
	        programName.setColumn(2);	        
	        programName.setHint(PROGRAM_NAME_HINT);
	        programName.setLabel(PROGRAM_NAME_LABEL);
	        programName.setRequired(true);
	        programName.setStyle(style);      
	        programName.setWidth(325);
	        programName.setDependsOn(PACKAGE_NAME);
	        metaModel.add(programName);
        }
        else
        {
        	final ViewInfo packageName = new ViewInfo(PACKAGE_NAME, ViewType.TEXT_LINE);
	        packageName.setColumn(2);
	        packageName.setDefaultValue(PACKAGE_NAME_GENERATED_LABEL);
	        packageName.setEditable(false);
	        packageName.setLabel(PACKAGE_NAME_LABEL);
	        packageName.setStyle(style);
	        packageName.setWidth(325);
	        metaModel.add(packageName);	        	
        	
        	final ViewInfo programName = new ViewInfo(PROGRAM_NAME, ViewType.TEXT_LINE);
	        programName.setColumn(2);
        	programName.setDefaultValue(PROGRAM_NAME_GENERATED_LABEL);
	        programName.setEditable(false);
	        programName.setLabel(PROGRAM_NAME_LABEL);
	        programName.setStyle(style);      
	        programName.setWidth(325);
	        metaModel.add(programName); 
	        
	        final BooleanViewInfo allowRestarts = new BooleanViewInfo(PLAN_ALLOW_RESTARTS);
	        allowRestarts.setDefaultValue((Boolean) getProperty(4, serviceLocator));
	        allowRestarts.setEnabled(true);
	        allowRestarts.setForceLineEmbed(true);        
	        allowRestarts.setHint(PLAN_ALLOW_RESTARTS_HINT); 
	        allowRestarts.setLabel(PLAN_ALLOW_RESTARTS_LABEL);        
	        allowRestarts.setStyle(style);     
	        allowRestarts.setWidth(325);      
	        allowRestarts.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).hide();
	        allowRestarts.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(true)).show();
	        allowRestarts.when(PLAN_ALLOW_ADVERTISEMENTS, FieldValue.equals(false)).setValue("");	        
	        metaModel.add(allowRestarts);     	        
        }        
        
        return metaModel;
	}
	
	
	private Data getFormData(final ComponentModel model) 
	{
		final Data data = new Data();
		return data;
    }
	
	
	/**
	 * 
	 * Return SCCM Packages read into Management Packages table.
	 *
	 */
	private List<String> getPackages(ServiceLocator serviceLocator) 
	{					
		DbApi dbApi = serviceLocator.getDbApi();		
		List<String> dataList = new ArrayList<String>();
		
		Criterion critOperational = Criterion.EQ("m." + Db.ManagementPackage.COLUMN_OPERATIONAL, true);
		Criterion critPackageDesc1 = Criterion.NOT_LIKE("m." + Db.ManagementPackage.COLUMN_DESCRIPTION, RECLAMATION_GENERATED_PACKAGE_DESC);
		Criterion critPackageDesc2 = Criterion.EQ("m." + Db.ManagementPackage.COLUMN_DESCRIPTION, null);
		Criterion critPackageDescOr = Criterion.OR(critPackageDesc1, critPackageDesc2);
		Criterion critPlatform = Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, getSCCMPlatformId(serviceLocator));
		Criterion critAll = Criterion.AND(critOperational, critPackageDescOr, critPlatform);
	  
		SelectQuery query = Query.selectDistinct(Arrays.asList(
	                Query.column("m." + Db.ManagementPackage.COLUMN_NAME)));
	    query.from(Db.ManagementPackage.TABLE_NAME, "m");
	    query.join(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementPackage.COLUMN_PLATFORM);
	    query.where(critAll);
	    query.orderBy("m." + Db.ManagementPackage.COLUMN_NAME);
	    
	    List<String> list = dbApi.getQueryExecutor().executeL1(query);
	    
	    for (String value : list) 
	    {
	    	String packageName = value.toString();		    	  
			  
			if (packageName != null) 
			{
				dataList.add(packageName);
			}
	    }	    
	    
	    return dataList;
	}
}