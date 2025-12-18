package com.organization.live.survey.reclamation.ui.action;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.organization.live.db.DbApi;
import com.organization.live.db.meta.ColumnMetaData;
import com.organization.live.db.meta.Datatype;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.logger.LoggerService;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.survey.reclamation.ui.component.SCCMComponent;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.type.ViewType;
import com.organization.live.ui.common.client.util.Constants;
import com.organization.live.ui.common.client.util.NameUtils;
import com.organization.live.ui.common.client.view.info.MetaModel;
import com.organization.live.ui.common.client.view.info.ViewInfo;

/**
 * Base action for SCCM uninstall component for Reclamation.
 * 
 * @author Danny Carvajal, borrowed code by Bill Somerville
 *
 */
public abstract class BaseInitSCCMUninstallComponentAction<C extends SCCMComponent>
	extends BaseSCCMComponentAction<C>
{
	private static final String HEADER1_HTML_TEMPLATE = "<div style=\"padding: 10px\">%1$s" +
		"<font face=\"Helvetica, Arial, sans-serif\" size=\"5\">%2$s</font></div>";
	private static final String HEADER2_HTML_TEMPLATE = "<div style=\"padding: 10px\">" +
		"<font face=\"Helvetica, Arial, sans-serif\" size=\"3\">%1$s</font></div>";
	private static final String ICON_HTML_TEMPLATE = "<img src=\"" + Constants.IMAGE_SERVLET_URL +
		"%1$s%2$s\" height=\48\" width=\"48\" align=\"left\">&nbsp;&nbsp;&nbsp;";

    protected LoggerFactory loggerFactory;
	
    /**
     * Setter for logger factory.
     * 
     * @param factory the factory to set
     */
    public void setLoggerFactory (final LoggerFactory factory) 
    {
        this.loggerFactory = factory;
    }	
		
    protected LoggerService getLoggerService (final DbApi dbApi) 
    {
        return loggerFactory.createLoggerService(dbApi);
    }    	
			
    protected Logger getLogger(final String type, final LoggerService loggerService) 
    {
        return loggerService.getLogger(ReclamationPlanComponent.LOG_PREFIX + type, 
        		ReclamationPlanComponent.COMPONENT_NAME, 
        		ReclamationPlanComponent.LOG_PREFIX + type );
    }		    
	
	/**
	 * Creates a simple R/O text {@link ViewInfo} object.
	 * 
	 * @param name
	 * @param text
	 * @return
	 */
	protected ViewInfo createHTMLReadOnlyViewInfo(final String name, final String text)
	{
		final ViewInfo viewInfo = new ViewInfo(name, ViewType.HTML);

		viewInfo.setEditable(false);
		viewInfo.setDefaultValue(text);
		viewInfo.setLabel(null);
		return (viewInfo);
	}
	
	/**
	 * Returns HTML for an icon image.
	 * 
	 * @param iconName name of the icon
	 * @param thumb if true, the thumbnail icon will be shown
	 * @return the icon HTML
	 */
	protected String getIconHTML(final String iconName, final boolean thumb)
	{
		final String html = String.format(ICON_HTML_TEMPLATE, thumb ? Constants.THUMB_PREFIX : "",
    		NameUtils.getIconNameFromStyle(iconName));

		return (html);
	}

    /**
     * Returns HTML for a header field.
     * 
     * @param description field description
     * @param iconName field icon, or null for no icon
     * @return the header HTML
     */
    protected String getHeaderHTML(final String description, final String iconName)
    {
    	final String html = String.format(HEADER1_HTML_TEMPLATE,
    		(iconName != null) ? getIconHTML(iconName, true) : "", description);

    	return (html);
    }

    /**
     * Returns HTML for a sub-header field.
     * 
     * @param description field description
     * @return the sub-header HTML
     */
    protected String getSubHeaderHTML(final String description)
    {
    	final String html = String.format(HEADER2_HTML_TEMPLATE, description);

    	return (html);
    }

	/**
	 * @param data
	 * @param map
	 * @param iconName
	 * @param headerText
	 * @param subHeaderText
	 */
	protected void buildHeader(final Data data, final MetaModel map, final String iconName,
		final String headerText, final String subHeaderText)
	{
		final ViewInfo header1 = new ViewInfo(SCCMComponent.VIEW_HEADER1, ViewType.HTML);
		final ViewInfo header2 = new ViewInfo(SCCMComponent.VIEW_HEADER2, ViewType.HTML);

		header1.setLabel(null);
		data.set(SCCMComponent.VIEW_HEADER1, getHeaderHTML(headerText, iconName));
		map.put(SCCMComponent.VIEW_HEADER1, header1);

		if (StringUtils.isNotBlank(subHeaderText))
		{
			header2.setLabel(null);
			header2.setColumn(2);
			data.set(SCCMComponent.VIEW_HEADER2, getSubHeaderHTML(subHeaderText));
			map.put(SCCMComponent.VIEW_HEADER2, header2);
		}
	}

    /**
     * Sets the field size limits for any string fields in the map in the {@link MetaModel}.
     * 
     * @param dbApi
     * @param fieldMap
     * @param metaModel
     * @param planTable
     * @param templateTable
     */
    protected void setFieldLimits(final DbApi dbApi, final Map<String, String> fieldMap, final MetaModel metaModel,
   		final String planTable, final String templateTable)
    {
    	final Map<String, ColumnMetaData> planColumns = dbApi.getMetaDataCache().getTableColumnsMeta(planTable);
    	final Map<String, ColumnMetaData> templateColumns = dbApi.getMetaDataCache().getTableColumnsMeta(templateTable);

    	for (Map.Entry<String, String> field : fieldMap.entrySet())
    	{
    		final String viewName = field.getKey();
    		final String columnName = field.getValue();
    		final ViewInfo viewInfo = metaModel.get(viewName);

    		if (viewInfo != null)
    		{
    			ColumnMetaData column = planColumns.get(columnName);

    			if (column == null)

    				column = templateColumns.get(columnName);

    			if (column != null)
    			{
    				if (column.getType().equals(Datatype.STRING))
    				{
    					Integer size = column.getSize();

    					if ((size != null) && (((int) size) > 0))
    					{
    						viewInfo.setMaxLength(size);
    					}
    				}
    			}
    		}
    	}
    }
}
