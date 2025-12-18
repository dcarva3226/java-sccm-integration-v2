package com.organization.live.survey.reclamation.ui.component.scene1;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
//import com.organization.live.survey.reclamation.messenger.ReclamationMessenger;
import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanWizard;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.data.criteria.Clause;
import com.organization.live.ui.common.client.data.criteria.Criteria;
import com.organization.live.ui.common.client.data.criteria.Restriction;
import com.organization.live.ui.common.client.type.ViewType;
import com.organization.live.ui.common.client.view.info.MetaModel;
import com.organization.live.ui.common.client.view.info.ViewInfo;
import com.organization.live.ui.common.client.widget.WidgetModel;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetDescriptor;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.dataview.info.DataViewInfo;
import com.organization.live.ui.service.entity.UIEntity;
import com.organization.live.ui.system.component.dataview.DataViewComponentConfig;

/**
 * @author Danny Carvajal
 *
 */
public class Scene1Initializer extends AbstractReclamationAction 
{
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
		
		// Set up the main form
		final MetaModel formMetaModel = getFormMetaModel();		
		final Data formData = getFormData(model, serviceLocator);
		FormModel formModel = model.get(SCENE_SELECT_COMPUTERS_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_SELECT_COMPUTERS_FORM);        
        formDescriptor.setPixelHeight(50);       
        formDescriptor.setVisible(true);
        
        if (formModel == null) 
        {
            formModel = new FormModel(formDescriptor);
            formModel.setHeading(SCENE_SELECT_COMPUTERS_FORM_HEADING);
        }
        formModel.setMetaModel(formMetaModel);
        model.set(formModel);
        formModel.setDataItem(formData);       
        
        // Get selected items from Grid 	
		final WidgetModel<Data, ?> sourceModel = model.getParameter(ReclamationPlanWizard.SOURCE_MODEL); 
		final List<Data> selectedPackages = sourceModel.getSelected();		
		final List<Data> selectedPackageIds = new ArrayList<Data>(selectedPackages.size());
		for (final Data pkg : selectedPackages) 
		{
			selectedPackageIds.add(pkg);
		}
        
        // Setup the data view grid
        ComponentWidgetDescriptor componentDescriptor = model.getDescriptor(DATA_PREVIEW);
        DataViewComponentConfig componentConfig = new DataViewComponentConfig(componentDescriptor);
        DataViewInfo dataViewInfo = serviceLocator.getDataViewInfoService().getDataViewInfo(COMPUTER_PRIMARY_EMAIL, serviceLocator);
        componentConfig.setDataViewId(dataViewInfo.getId());

        Criteria criteria = new Criteria();
        Clause clause = new Clause(UIEntity.FIELD_ID, Restriction.IN, null);
        clause.setValues(getComputerIds(selectedPackageIds));
        criteria.and(clause);

        componentConfig.setImplicitCriteria(criteria);
        componentConfig.setHideGridButtons(true);
        componentConfig.setWrapperComponent(DATA_PREVIEW);
        model.set(formModel);      
        model.set(componentConfig.getComponentWidgetModel());
		return model;
	}
	
	private MetaModel getFormMetaModel() 
	{
		final MetaModel metaModel = new MetaModel();		
        final ViewInfo signatureGUID = new ViewInfo(SIGNATURE_GUID, ViewType.HIDDEN);
        metaModel.add(signatureGUID);
        final ViewInfo signatureName = new ViewInfo(SIGNATURE_NAME, ViewType.HIDDEN);
        metaModel.add(signatureName);
        final ViewInfo signatureSPV = new ViewInfo(SIGNATURE_SPV, ViewType.HIDDEN);
        metaModel.add(signatureSPV);
		return metaModel;
	}
	
	private Data getFormData(final ComponentModel model, final ServiceLocator serviceLocator) 
	{
		final Data data = new Data();
		final WidgetModel<Data, ?> sourceModel = model.getParameter(ReclamationPlanWizard.SOURCE_MODEL);
		final Long packageID = sourceModel.getSelected().get(0).getId();
		
		// Grab guid from database in case it's not available in the view grid for source model
		SelectQuery query = Query.select(Arrays.asList(
				Query.column("sw." + Db.SoftwarePackagesWindows.COLUMN_SIGNATURE_GUID),
				Query.column("sw." + Db.SoftwarePackagesWindows.COLUMN_FRIENDLY_NAME),
				Query.column("spv." + Db.SoftwareProductVersion.COLUMN_FRIENDLY_NAME)));
		query.from(Db.SoftwarePackagesWindows.TABLE_NAME, "sw");
		query.leftJoin(Db.SoftwareProductVersion.TABLE_NAME, "spv", "spv." + Db.SoftwareProductVersion.COLUMN_ID, "sw." + Db.SoftwarePackagesWindows.COLUMN_SOFTWARE);
		query.where(Criterion.EQ("sw." + Db.SoftwarePackagesWindows.COLUMN_ID, packageID));
		
		List<Object[]> signatures = serviceLocator.getDbApi().getQueryExecutor().executeLA(query);
		
		String signatureGUID  = signatures.get(0)[0] != null ? signatures.get(0)[0].toString() : null; 
		String signatureName = signatures.get(0)[1].toString();
		String spv = signatures.get(0)[2] != null ? signatures.get(0)[2].toString() : null;
		
		data.set(SIGNATURE_GUID, signatureGUID);
		data.set(SIGNATURE_NAME, signatureName);
		data.set(SIGNATURE_SPV, spv);
		data.set(PRODUCT_COST, sourceModel.getSelected().get(0).get("cost"));
		return data;
    }
	
	
	/**
	 * 
	 * Return a list of computers that have the given software product version id
	 *
	 */
	private List<Long> getComputerIds(final List<Data> windowsPackages)
	{
		List<Long> dataList = new ArrayList<Long>();

		// Collect the selected computer row IDs
		for (Data windowsPackage : windowsPackages)
		{
			final Long computerID = (Long) windowsPackage.get(Db.SoftwarePackagesWindows.COLUMN_INSTALLED_ON);
			dataList.add(computerID);
		}
		
	    return dataList;
	}
}