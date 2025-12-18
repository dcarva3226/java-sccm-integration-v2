package com.organization.live.survey.reclamation.ui.component.scene2;

import java.util.Arrays;
import java.util.List;

import com.organization.live.db.query.Order;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.db.Db;
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
public class Scene2Initializer extends AbstractReclamationAction 
{
	private Long lastSiteServer = null;
	private String lastSiteCode = null;
	
	@Override
	public ComponentModel execute(ComponentModel model,
			ServiceLocator serviceLocator) throws Exception	
	{
		//updateServers(model, serviceLocator);
		setProperties(serviceLocator);
		final MetaModel formMetaModel = getFormMetaModel(serviceLocator);		
		final Data formData = getFormData(serviceLocator);
		FormModel formModel = model.get(SCENE_CONNECTION_FORM);
        final FormDescriptor formDescriptor = model.getDescriptor(SCENE_CONNECTION_FORM);
        
        if (formModel == null) {
            formModel = new FormModel(formDescriptor);
            formModel.setHeading(SCENE_CONNECTION_FORM_HEADING);
        }
        
        formModel.setMetaModel(formMetaModel);        
        model.set(formModel);
        formModel.setDataItem(formData);
		
        return model;
	}
	
	private MetaModel getFormMetaModel(final ServiceLocator serviceLocator) 
	{
		final MetaModel metaModel = new MetaModel();     
        final String style = "margin-left:10px";
        
        // Site server
        final ViewInfo server = new ViewInfo(SCCM_SERVER, ViewType.IP_ADDRESS);
        server.setColumn(2);
        server.setDefaultValue(this.lastSiteServer);
        server.setEmptyText(SCCM_SERVER_LABEL);
        server.setLabel(SCCM_SERVER_LABEL);
        server.setRequired(true);
        server.setStyle(style);
        server.setWidth(325);
        metaModel.add(server);	
		
        // Site code
        final ViewInfo siteCode = new ViewInfo(SITE_CODE, ViewType.TEXT_LINE);
        siteCode.setColumn(2);
        siteCode.setDefaultValue(this.lastSiteCode);
        siteCode.setEditable(true);
        siteCode.setEmptyText(SITE_CODE_LABEL);
        siteCode.setLabel(SITE_CODE_LABEL);
        siteCode.setRequired(true);
        siteCode.setStyle(style);
        siteCode.setWidth(325);
        siteCode.setHint(SITE_CODE_HINT);
        metaModel.add(siteCode);   
        
        // Logged in user
        final ViewInfo sessionUserID = new ViewInfo(SESSION_USERID, ViewType.HIDDEN);
        metaModel.add(sessionUserID);
        
		return metaModel;
	}
	
	private Data getFormData(final ServiceLocator serviceLocator) 
	{
		final Data data = new Data();
		data.set(SESSION_USERID, serviceLocator.getUserSession().getUser().getId());
		return data;
    }
	
	
	private void setProperties(final ServiceLocator serviceLocator) 
	{
		SelectQuery query = Query.select(Arrays.asList(
			Query.column("rc." + Db.SCCMServerConfig.COLUMN_SCCM_SITE_SERVER),
			Query.column("rc." + Db.SCCMServerConfig.COLUMN_SCCM_SITE_CODE)));
        	query.from(Db.Reclamation.TABLE_NAME, "r");
        	query.join(Db.SCCMServerConfig.TABLE_NAME, "rc", "rc." + Db.SCCMServerConfig.COLUMN_ID, "r." + Db.Reclamation.COLUMN_SCCM_CONFIG_ID);
        	query.orderBy("rc." + Db.SCCMServerConfig.COLUMN_CREATED_ON, Order.DESC);
        	query.limit(1L);
        	
		List<Object[]> properties = serviceLocator.getDbApi().getQueryExecutor().executeLA(query);		
		if (properties.size() != 0)
		{
			this.lastSiteServer = (Long) properties.get(0)[0];
			
			if (properties.get(0)[1] != null)
				this.lastSiteCode = properties.get(0)[1].toString();
		}
	}
}
