package com.organization.live.survey.reclamation.ui.component.scene5;

import static com.organization.live.db.query.Query.count;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
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
public class Scene5Initializer extends AbstractReclamationAction 
{	
	private DbApi dbApi = null;
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
	
		this.dbApi = serviceLocator.getDbApi();
		validatePreviousForm(model);		
		
        String heading = null;       
       	heading = SCENE_EMAIL_FORM_HEADING;
		
		final MetaModel formMetaModel = getFormMetaModel(serviceLocator, model);		
		final Data formData = getFormData(model);
		FormModel formModel = model.get(SCENE_EMAIL_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_EMAIL_FORM);
        
        if (formModel == null) formModel = new FormModel(formDescriptor);
        formModel.setHeading(heading);
        formModel.setMetaModel(formMetaModel);
        model.set(formModel);
        formModel.setDataItem(formData);
		return model;
	}
	
	private MetaModel getFormMetaModel(final ServiceLocator serviceLocator, final ComponentModel model) 
	{		
		final MetaModel metaModel = new MetaModel();      
        final String style = "margin-left:10px";

        // Email listbox
        final SelectionViewInfo<String> emailTemplates = new SelectionViewInfo<String>(getEmailSubjectList(), EMAIL_TEMPLATE);
        emailTemplates.setCaller(SET_EMAIL_PROPERTIES_ACTION);
        emailTemplates.setColumn(2);
        emailTemplates.setEmptyText(EMAIL_TEMPLATE_EMPTY_TEXT);          
        emailTemplates.setForceLineEmbed(true);
        emailTemplates.setHint(EMAIL_TEMPLATE_HINT);
        emailTemplates.setLabel(EMAIL_TEMPLATE_LABEL);
        emailTemplates.setStyle(style);
        emailTemplates.setWidth(450); 
        if (!emailTemplatesExist()) emailTemplates.setVisible(false);
        metaModel.add(emailTemplates);        
		
        final ViewInfo emailSubject = new ViewInfo(EMAIL_SUBJECT, ViewType.TEXT_LINE);
        emailSubject.setColumn(2);
        emailSubject.setEmptyText(EMAIL_SUBJECT_LABEL);
        emailSubject.setForceLineEmbed(true);
        emailSubject.setHint(EMAIL_SUBJECT_HINT);
        emailSubject.setMaxLength(5000);
        emailSubject.setRequired(true);
        emailSubject.setStyle(style);
        emailSubject.setWidth(450);       
        metaModel.add(emailSubject);        
        
        final ViewInfo emailBody = new ViewInfo(EMAIL_BODY, ViewType.TEXT);
        emailBody.setColumn(2);
        emailBody.setDefaultValue(getEmailBodyDefaultValue(model));
        emailBody.setEmptyText(EMAIL_BODY_LABEL);
        emailBody.setForceLineEmbed(true);  
        emailBody.setHint(EMAIL_BODY_HINT);
        emailBody.setMaxLength(5000);
        emailBody.setRequired(true);
        emailBody.setStyle(style);
        emailBody.setWidth(450);        
        metaModel.add(emailBody); 
        
        final BooleanViewInfo saveEmail = new BooleanViewInfo(EMAIL_SAVE_TEMPLATE);
        saveEmail.setColumn(2);
        saveEmail.setDefaultValue(false);
        saveEmail.setEnabled(true);
        saveEmail.setForceLineEmbed(true);        
        saveEmail.setHint(EMAIL_SAVE_TEMPLATE_HINT); 
        saveEmail.setLabel(EMAIL_SAVE_TEMPLATE_LABEL);        
        saveEmail.setStyle(style);     
        saveEmail.setWidth(450);  
        metaModel.add(saveEmail);        
        
        final ViewInfo emailTemplateID = new ViewInfo(EMAIL_TEMPLATE_ID, ViewType.HIDDEN);
        metaModel.add(emailTemplateID);      
  
        return metaModel;
	}
	
	
	/**
	 * 
	 * Validate the previous scene and make sure the reclamation plan name doesn't already exist
	 *
	 */
	private void validatePreviousForm(final ComponentModel model) throws Exception 
	{	
		String reclamationPlanName = getReclamationPlanName(model); 
		Long recCount = dbApi.getQueryExecutor().execute1(Query.select(count())
				.from(Db.Reclamation.TABLE_NAME)
				.where(Criterion.EQ(Db.Reclamation.COLUMN_NAME, reclamationPlanName)));
		
		if (recCount != 0) 
		{
			throw new Exception(String.format("The reclamation plan name '%s' already exists. Please enter a different name.", 
					reclamationPlanName));
		}
	}
	
	
	private Data getFormData(final ComponentModel model) 
	{
		final Data data = new Data();
		return data;
    }		
	
	
	/**
	 * 
	 * Return email subjects to pre-fill dropdown.
	 *
	 */
	private List<String> getEmailSubjectList() 
	{
		List<String> dataList = new ArrayList<String>();
		
		Integer counter = 0;
		Integer limit = 2000;
	  
		SelectQuery query = Query.selectDistinct(Arrays.asList(
	                Query.column(Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT)));
	    query.from(Db.ReclamationEmailConfig.TABLE_NAME, "e");
	    query.where(Criterion.EQ(Db.ReclamationEmailConfig.COLUMN_TEMPLATE, true));
	    query.orderBy(Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT);

	    List<String> list = dbApi.getQueryExecutor().executeL1(query);
	    
	    for (String value : list) 
	    {
	    	
	    	if (counter == limit) break;
	    	String subject = value.toString();		    	  			  
			if (subject != null) 
			{
				dataList.add(subject);
				limit++;
			}			
	    }	    

	    dataList.add(EMAIL_TEMPLATE_NONE);
	    return dataList;
	}	
	
	
	/**
	 * 
	 * Check to see if any email template records exist.
	 *
	 */
	private boolean emailTemplatesExist()
	{
		Long recCount = dbApi.getQueryExecutor().execute1(Query.select(count())
				.from(Db.ReclamationEmailConfig.TABLE_NAME).where(Criterion.EQ(Db.ReclamationEmailConfig.COLUMN_TEMPLATE, true)));
		
		if (recCount == 0) 
			return false;
		else
			return true;
	}
	
	
	/**
	 * 
	 * Build the email body default value string.
	 *
	 */	
	private String getEmailBodyDefaultValue(final ComponentModel model)
	{
		String productCost = getProductCost(model).toString();
		String defaultValue = "<ENTER EMAIL BODY TEXT HERE>";
		
		if (productCost != null)
		{
			SelectQuery query = Query.selectDistinct(Query.column("c." + Db.Currency.COLUMN_SYMBOL))
			.from(Db.SystemVersion.TABLE_NAME, "v")
			.join(Db.Currency.TABLE_NAME, "c", "c." + Db.Currency.COLUMN_ID, "v." + Db.SystemVersion.COLUMN_CURRENCY);
			
			String currency = dbApi.getQueryExecutor().execute1(query);			
            currency = StringEscapeUtils.unescapeHtml(currency);			
			
			defaultValue += String.format("\r\n\r\nSoftware Cost: %s%s", 
					currency != null ? currency : "",
					productCost);
		}
		
		return defaultValue;
	}	
}