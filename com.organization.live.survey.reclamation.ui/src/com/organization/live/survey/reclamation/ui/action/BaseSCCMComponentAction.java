package com.organization.live.survey.reclamation.ui.action;

import static com.organization.live.survey.reclamation.ui.component.SCCMUninstallComponent.SCCM_PLATFORM;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.meta.ColumnMetaData;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.logger.Logger;
import com.organization.live.logger.LoggerFactory;
import com.organization.live.logger.LoggerService;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.survey.reclamation.ui.component.SCCMComponent;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.service.action.common.AbstractAction;

/**
 * Base action for all SCCM component-related actions.
 * 
 * @author Danny Carvajal, borrowed code from Bill Somerville
 *
 */
public abstract class BaseSCCMComponentAction<C extends SCCMComponent> extends AbstractAction
{	
	private C component;
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
	 * @param component
	 *            the component to set
	 */
	public void setComponent(final C component)
	{
		this.component = component;
	}

	protected C getComponent()
	{
		return component;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.organization.live.ui.service.action.common.AbstractExecutional#getExecutionSource()
	 */
	@Override
	public ExecutionSource getExecutionSource()
	{
		return component.getExecutionSource();
	}

	/**
	 * Truncates a string potentially longer than the DB column if necessary.
	 * 
	 * @param dbApi
	 * @param tableName
	 * @param columnName
	 * @param string
	 * @return
	 */
	protected String truncateDBString(final DbApi dbApi, final String tableName, final String columnName, final String string)
	{
		String truncatedString = string;

		if (StringUtils.isNotBlank(string))
		{
			ColumnMetaData column = dbApi.getMetaDataCache().getTableColumnsMeta(tableName).get(columnName);

			if (column != null)
			{
				final int size = (column.getSize() != null) ? column.getSize() : 0;

				if ((size > 0) && (size < string.length()))
				{
					truncatedString = string.substring(0, size);
				}
			}
		}

		return truncatedString;
	}
	
	/**
	 * Returns the specified file, read from the resource path, as a string.
	 * 
	 * @param fileName file to read
	 * @return the file contents, or an error message
	 */
	protected String getFileAsString(final String fileName)
	{
		String file = "";
		final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);

		if (is != null)
		{
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			final byte[] buffer = new byte[2048];

			try
			{
				int read;

				while ((read = is.read(buffer)) > 0)
				{
					os.write(buffer, 0, read);
				}

				file = new String(os.toByteArray(), "UTF-8");
			}

			catch (final IOException e)
			{
				file = e.getMessage();
			}
		}

		else
		{
			file = "Error: input file " + fileName + " not found";
		}

		return file;
	}	
	
	/**
	 * 
	 * Grab platform ID from ManagementPlatform table.
	 *
	 */	
    protected Long getSCCMPlatformId(final ServiceLocator serviceLocator)
    {
		// Get Platform ID
		SelectQuery query = Query.select(Query.column(Db.ManagementPlatform.COLUMN_ID));
			query.from(Db.ManagementPlatform.TABLE_NAME);
			query.where(Criterion.EQ(Db.ManagementPlatform.COLUMN_NAME, SCCM_PLATFORM));
			
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);
    }
}
