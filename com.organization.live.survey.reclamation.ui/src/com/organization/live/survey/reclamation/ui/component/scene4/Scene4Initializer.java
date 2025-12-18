package com.organization.live.survey.reclamation.ui.component.scene4;

import static com.organization.live.db.query.Query.count;

import java.util.Arrays;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.Order;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.TypeProcessor;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.type.ViewType;
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
public class Scene4Initializer extends AbstractReclamationAction 
{
	private DbApi dbApi;
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
			
		this.dbApi = serviceLocator.getDbApi();
		validatePreviousForm(model);
		final MetaModel formMetaModel = getFormMetaModel(model, serviceLocator);		
		final Data formData = getFormData(model);
		FormModel formModel = model.get(SCENE_CONFIGURATION_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_CONFIGURATION_FORM);
        
        if (formModel == null) {
            formModel = new FormModel(formDescriptor);
            formModel.setHeading(SCENE_CONFIGURATION_FORM_HEADING);
        }
        formModel.setMetaModel(formMetaModel);
        model.set(formModel);
        formModel.setDataItem(formData);
		return model;
	}
	
	private MetaModel getFormMetaModel(final ComponentModel model, final ServiceLocator serviceLocator) 
	{
		
		final MetaModel metaModel = new MetaModel();
        final String style = "margin-left:10px";
        
        final ViewInfo planName = new ViewInfo(PLAN_NAME, ViewType.TEXT_LINE);        
        planName.setEmptyText(PLAN_NAME_LABEL);
        planName.setMaxLength(255);
        planName.setRequired(true);
        planName.setStyle(style);
        planName.setWidth(325); 
        metaModel.add(planName);
        
        final ViewInfo planDesc = new ViewInfo(PLAN_DESC, ViewType.TEXT);
        String defaultValue = model.get(SCENE_DEPLOYMENTS_FORM).getDataItem().get(DEPLOYMENT_DESC);
        if (defaultValue != null) planDesc.setDefaultValue(defaultValue.toString());
        planDesc.setEmptyText(PLAN_DESC_LABEL);
        planDesc.setHeight(60);
        planDesc.setMaxLength(1000);
        planDesc.setRequired(false);
        planDesc.setForceLineEmbed(true);
        planDesc.setStyle(style);
        planDesc.setWidth(325); 
        metaModel.add(planDesc);
        
        final ViewInfo planEmail = new ViewInfo(PLAN_EMAIL, ViewType.TEXT_LINE);        
        planEmail.setEmptyText(PLAN_EMAIL_LABEL);
        planEmail.setMaxLength(255);        
        planEmail.setRequired(true);
        TypeProcessor.addTypeFormat(planEmail, ViewType.EMAIL);
        planEmail.setRegex("(" + planEmail.getRegex() + ")|(^%[A-Za-z_|/\\s]+%$)");
        planEmail.setRegexHint("Invalid value. Should be a valid email address.");
        planEmail.setDefaultValue(getLastPlanEmail(serviceLocator));
        planEmail.setStyle(style);
        planEmail.setWidth(325);      
        metaModel.add(planEmail);   
        
        final ViewInfo emailExpDays = new ViewInfo(PLAN_EMAIL_EXP_DAYS, ViewType.INTEGER);  
        emailExpDays.setDefaultValue((Long)getProperty(0, serviceLocator));
        emailExpDays.setEmptyText(PLAN_EMAIL_EXP_DAYS_LABEL);
        emailExpDays.setHint(PLAN_EMAIL_EXP_DAYS_HINT);
        emailExpDays.setMaxLength(3);
        emailExpDays.setRegex("^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$");    
        emailExpDays.setRegexHint("Invalid value. Should be a value between 1 and 999.");        
        emailExpDays.setRequired(true);
        emailExpDays.setStyle(style);
        emailExpDays.setWidth(325);
        metaModel.add(emailExpDays);     

        final ViewInfo keepEmailRecsDays = new ViewInfo(PLAN_EMAIL_KEEP_DAYS, ViewType.INTEGER);  
        keepEmailRecsDays.setDefaultValue((Long)getProperty(1, serviceLocator));
        keepEmailRecsDays.setEmptyText(PLAN_EMAIL_KEEP_DAYS_LABEL);
        keepEmailRecsDays.setHint(PLAN_EMAIL_KEEP_DAYS_HINT);
        keepEmailRecsDays.setMaxLength(3);  
        keepEmailRecsDays.setRegex("^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$");    
        keepEmailRecsDays.setRegexHint("Invalid value. Should be a value between 1 and 999.");
        keepEmailRecsDays.setRequired(true);
        keepEmailRecsDays.setStyle(style);
        keepEmailRecsDays.setWidth(325);       
        metaModel.add(keepEmailRecsDays);
               
        final ViewInfo emailAttempts = new ViewInfo(PLAN_EMAIL_SEND_ATTEMPTS, ViewType.INTEGER);  
        emailAttempts.setDefaultValue((Long)getProperty(3, serviceLocator));
        emailAttempts.setEmptyText(PLAN_EMAIL_SEND_ATTEMPTS_LABEL);
        emailAttempts.setHint(PLAN_EMAIL_SEND_ATTEMPTS_HINT);
        emailAttempts.setMaxLength(3);
        emailAttempts.setRegex("^([1-9]|[1-9][0-9]|[1-9][0-9][0-9])$");    
        emailAttempts.setRegexHint("Invalid value. Should be a value between 1 and 999.");              
        emailAttempts.setRequired(true);
        emailAttempts.setStyle(style);
        emailAttempts.setWidth(325);
        metaModel.add(emailAttempts);            
        
        final ViewInfo sendEmailWaitDays = new ViewInfo(PLAN_EMAIL_SEND_WAIT_DAYS, ViewType.INTEGER);  
        sendEmailWaitDays.setDefaultValue((Long)getProperty(2, serviceLocator));
        sendEmailWaitDays.setEmptyText(PLAN_EMAIL_SEND_WAIT_DAYS_LABEL);
        sendEmailWaitDays.setHint(PLAN_EMAIL_SEND_WAIT_DAYS_HINT);
        sendEmailWaitDays.setMaxLength(3);  
        sendEmailWaitDays.setRequired(true);
        sendEmailWaitDays.setRegex("^([0-9]|[0-9][0-9]|[0-9][0-9][0-9])$");    
        sendEmailWaitDays.setRegexHint("Invalid value. Should be a value between 1 and 999.");
        sendEmailWaitDays.setStyle(style);
        sendEmailWaitDays.setWidth(325);             
        metaModel.add(sendEmailWaitDays);  
        
        final SelectionViewInfo<String> noResponseAction = new SelectionViewInfo<String>(Arrays.asList(PLAN_NO_RESPONSE_ACTION_1, PLAN_NO_RESPONSE_ACTION_2), PLAN_NO_RESPONSE_ACTION);
        noResponseAction.setHint(PLAN_NO_RESPONSE_ACTION_HINT);
        noResponseAction.setLabel(PLAN_NO_RESPONSE_ACTION_LABEL);
        noResponseAction.setRequired(true);
        noResponseAction.setStyle(style);
        noResponseAction.setWidth(325);
        metaModel.add(noResponseAction);	   
        
		return metaModel;
	}
	
	private Data getFormData(final ComponentModel model) 
	{
		final Data data = new Data();
		return data;
    }		
	
		
	/**
	 * 
	 * Validate the previous scene and make sure the SCCM collection name doesn't already exist
	 *
	 */
	private void validatePreviousForm(final ComponentModel model) throws Exception 
	{	
		String collectionName = getCollectionName(model); 
		Long recCount = dbApi.getQueryExecutor().execute1(Query.select(count())
				.from(Db.Reclamation.TABLE_NAME)
				.where(Criterion.EQ(Db.Reclamation.COLUMN_SCCM_COLLECTION_NAME, collectionName)));
		
		if (recCount != 0) 
		{
			throw new Exception(String.format("The collection name '%s' has already been used. Please enter a different name.", 
					collectionName));
		}
		
		// If advertisements are allowed, check for advert name
		if (getAllowAdvertisements(model))
		{
			if (getDeploymentName(model) == null)
			{
				throw new Exception("You have selected to allow advertisements. Please enter an advertisement name.");
			}
		}
	}
	
	
	/**
	 * 
	 * Return the last email used for a reclamation plan
	 *
	 */
	private String getLastPlanEmail(final ServiceLocator serviceLocator)
	{
		SelectQuery query = Query.select(Query.column(Db.Reclamation.COLUMN_ADMIN_EMAIL));
				query.from(Db.Reclamation.TABLE_NAME);
	        	query.orderBy(Db.Reclamation.COLUMN_CREATED_ON, Order.DESC);
	        	query.limit(1L);
	        	
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);		
	}
}