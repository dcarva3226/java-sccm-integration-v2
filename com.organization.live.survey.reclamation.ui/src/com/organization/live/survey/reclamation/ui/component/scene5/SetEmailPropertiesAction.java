package com.organization.live.survey.reclamation.ui.component.scene5;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.action.AbstractReclamationAction;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.widget.form.FormModel;
import com.organization.live.ui.service.ServiceLocator;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class SetEmailPropertiesAction extends AbstractReclamationAction 
{

	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
				
		FormModel formModel = model.get(SCENE_EMAIL_FORM);
        String subject = model.get(SCENE_EMAIL_FORM).getDataItem().get(EMAIL_TEMPLATE);
        if (EMAIL_TEMPLATE_NONE.equals(subject)) subject = null;               
        String body =  getEmailBody(subject, serviceLocator);
      
        Data data = new Data();    
        data.set(EMAIL_TEMPLATE, null);
        data.set(EMAIL_SUBJECT, subject);
        data.set(EMAIL_BODY, body);
        formModel.setDataItem(data);
        model.setFocusedWidget(SCENE_EMAIL_FORM_HEADING);
        model.set(formModel);
		return model;
	}
	
	
	/**
	 * 
	 * Get email body for the selected email subject.
	 *
	 */
	private String getEmailBody(String subject, ServiceLocator serviceLocator) 
	{
		 
		if (subject == null) return null;
		
		DbApi dbApi = serviceLocator.getDbApi();
		
		SelectQuery query = Query.select(
			Query.column(Db.ReclamationEmailConfig.COLUMN_EMAIL_BODY));
        	query.from(Db.ReclamationEmailConfig.TABLE_NAME, "e");
        	query.where(Criterion.EQ(Db.ReclamationEmailConfig.COLUMN_EMAIL_SUBJECT, subject));
        	query.limit(1L); // <-- in case there are two identical subjects. We give them one.
        	
		String body = dbApi.getQueryExecutor().execute1(query);

        if (body != null)
        	body = body.replaceAll("<br />", "\r\n");		
		
		return body;
	}
}
