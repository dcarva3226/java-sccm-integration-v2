package com.organization.live.survey.reclamation.ui.component.scene6;

import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.type.ViewType;
import com.organization.live.ui.common.client.view.info.MetaModel;
import com.organization.live.ui.common.client.view.info.ViewInfo;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.service.ServiceLocator;

/**
 * @author Danny Carvajal
 *
 */
public class Scene6Initializer extends AbstractReclamationAction 
{
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
			
		final MetaModel formMetaModel = getFormMetaModel(model);		
		final Data formData = getFormData(model, serviceLocator);
		FormModel formModel = model.get(SCENE_REVIEW_SAVE_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_REVIEW_SAVE_FORM);
        
        if (formModel == null) 
        {
            formModel = new FormModel(formDescriptor);
            formModel.setHeading(SCENE_REVIEW_SAVE_FORM_HEADING);
        }
        formModel.setMetaModel(formMetaModel);
        model.set(formModel);
        formModel.setDataItem(formData);
		return model;
	}
	
	private MetaModel getFormMetaModel(final ComponentModel model) 
	{
		
		final MetaModel metaModel = new MetaModel();
		if (getSignatureSPV(model) != null)
		{
			metaModel.add(new ViewInfo("Software", ViewType.LABEL));
		}
		
		metaModel.add(new ViewInfo("Signature", ViewType.LABEL));
		metaModel.add(new ViewInfo("Site Server", ViewType.LABEL));
		metaModel.add(new ViewInfo("Site Code", ViewType.LABEL));
		metaModel.add(new ViewInfo("PAD Server", ViewType.LABEL));
		metaModel.add(new ViewInfo("Collection Name", ViewType.LABEL));
		metaModel.add(new ViewInfo("Collection Description", ViewType.LABEL));
		metaModel.add(new ViewInfo("Allow Advertisement Creation", ViewType.LABEL));
		metaModel.add(new ViewInfo("Mandatory Advertisement", ViewType.LABEL));
		metaModel.add(new ViewInfo("Advertisement Name", ViewType.LABEL));
		metaModel.add(new ViewInfo("Advertisement Description", ViewType.LABEL));
		
		ViewInfo packageGUID = new ViewInfo("Signature GUID", ViewType.LABEL);
		if (getSignatureGUID(model) == null)
		{
			packageGUID.setVisible(false);
		}
			
		metaModel.add(packageGUID);
		metaModel.add(new ViewInfo("Package", ViewType.LABEL));
		metaModel.add(new ViewInfo("Program", ViewType.LABEL));
		
		ViewInfo allowRestarts = new ViewInfo("Allow Computer Restarts", ViewType.LABEL);
		if (getSignatureGUID(model) == null)
		{
			allowRestarts.setVisible(false);
		}
		metaModel.add(allowRestarts);
		
		metaModel.add(new ViewInfo("Reclamation Plan Name", ViewType.LABEL));
		metaModel.add(new ViewInfo("Plan Description", ViewType.LABEL));
		metaModel.add(new ViewInfo("Plan Email", ViewType.LABEL));
		metaModel.add(new ViewInfo("Email Expiration Days", ViewType.LABEL));		
		metaModel.add(new ViewInfo("Keep Email Days", ViewType.LABEL));
		metaModel.add(new ViewInfo("Email Send Attempts", ViewType.LABEL));
		metaModel.add(new ViewInfo("Email Send Wait Days", ViewType.LABEL));
		metaModel.add(new ViewInfo("No Response Action", ViewType.LABEL));
		metaModel.add(new ViewInfo("Total Machines to Process", ViewType.LABEL));
		return metaModel;
	}
	
	private Data getFormData(final ComponentModel model, final ServiceLocator serviceLocator) 
	{
		final Data data = new Data();
		Boolean allowAdverts = getAllowAdvertisements(model);
		Boolean allowRestarts = null;
		allowRestarts = getAllowRestarts(model);
		
		data.set("Software", getSignatureSPV(model));
		data.set("Signature", getSignatureName(model));		
		data.set("Site Server", getSiteServerIp(model).toString());
		data.set("Site Code", getSiteCode(model));
		data.set("Collection Name", getCollectionName(model));
		data.set("Collection Description", getCollectionDescription(model));
		data.set("Allow Advertisement Creation", allowAdverts);
		data.set("Advertisement Name", allowAdverts.equals(true) ? getDeploymentName(model) : "N/A");
		data.set("Mandatory Advertisement", allowAdverts.equals(true) ? getRequireAdvertisements(model) : "N/A");		
		data.set("Advertisement Description", allowAdverts.equals(true) ? getDeploymentDescription(model) : "N/A");			
		data.set("Package", allowAdverts.equals(true) ? getPackage(model) : "N/A");
		data.set("Program", allowAdverts.equals(true) ? getProgram(getProgram(model), model, serviceLocator) : "N/A");
		data.set("Allow Computer Restarts", allowAdverts.equals(true) && !allowRestarts.equals(null) ? getAllowRestarts(model) : "N/A");
		data.set("Signature GUID", getSignatureGUID(model));
		data.set("Reclamation Plan Name", getReclamationPlanName(model));
		data.set("Plan Description", getPlanDescription(model));
		data.set("Plan Email", getPlanEmail(model));
		data.set("Email Expiration Days", getEmailExpirationDays(model));
		data.set("Keep Email Days", getGroomEmailDays(model));
		data.set("Email Send Attempts", getEmailSendAttempts(model));
		data.set("Email Send Wait Days", getSendEmailWaitDays(model));
		data.set("No Response Action", getNoResponseActionText(model));
		data.set("Total Machines to Process", getTotalMachines(model));
		return data;
    }		
}