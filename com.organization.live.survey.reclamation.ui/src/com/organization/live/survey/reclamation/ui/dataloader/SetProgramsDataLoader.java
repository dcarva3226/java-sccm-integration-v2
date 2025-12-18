package com.organization.live.survey.reclamation.ui.dataloader;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.query.HAQueryExecutor;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.data.BaseData;
import com.organization.live.ui.common.client.data.Option;
import com.organization.live.ui.common.client.view.info.AbstractSelectionViewInfo;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.common.AbstractDataLoader;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class SetProgramsDataLoader extends AbstractDataLoader<Long, Long>
{
	public static final String LOADER_NAME = "SetProgramsDataLoader";

	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.DataLoader#getName()
	 */
	@Override
	public String getName()
	{
		return LOADER_NAME;
	}

	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.DataLoader#loadData(com.organization.live.ui.common.client.data.BaseData, com.organization.live.ui.common.client.view.info.AbstractSelectionViewInfo, com.organization.live.ui.service.ServiceLocator)
	 */
	@Override
	public List<BaseData> loadData(BaseData value, AbstractSelectionViewInfo info, ServiceLocator serviceLocator)
	{
		final DbApi dbApi = serviceLocator.getDbApi();
		final String packageName = value.get(info.getDependsOn());
        List<BaseData> options = new LinkedList<BaseData>();
        
        Long packageID = null;        
        if (packageName != null) 
        {
        	packageID =  getManagementPackageId(packageName, serviceLocator);
        }
        
        List<Entity> programs = dbApi.getEntityManager()
        	.query(Db.ManagementProgram.TABLE_NAME)
        	.where(Criterion.EQ(Db.ManagementProgram.COLUMN_PACKAGE, packageID))
        	.orderBy(Db.ManagementProgram.COLUMN_NAME)
        	.execute();

        for (Entity program : programs)
        {
        	options.add(new Option<Long>(program.getId(), program.getString(Db.ManagementProgram.COLUMN_NAME)));
        }

        return (options);
	}
	

	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.DataLoader#getType()
	 */
	@Override
	public String getType()
	{
		return null;
	}

	
	/* (non-Javadoc)
	 * @see com.organization.live.ui.service.action.DataLoader#getData(java.lang.Object, java.lang.Object, java.lang.String, com.organization.live.ui.service.ServiceLocator)
	 */
	@Override
	public BaseData getData(Long dependsOnValue, Long value, String property, ServiceLocator serviceLocator)
	{
		BaseData option;
		final DbApi dbApi = serviceLocator.getDbApi();
		Entity program = dbApi.getEntityManager().readById(Db.ManagementProgram.TABLE_NAME, value);

		option = new Option<Long>(value, program.getString(Db.ManagementProgram.COLUMN_NAME));

		return (option);
	}
	
	
	/**
	 * 
	 * Get the SCCM package ID based on package name that is passed.
	 *
	 */
	private Long getManagementPackageId(final String packageName, final ServiceLocator serviceLocator) 
	{
		DbApi dbApi = serviceLocator.getDbApi();
		HAQueryExecutor queryExecutor = dbApi.getQueryExecutor();

		SelectQuery query = Query.select(Arrays.asList(
			Query.column("m." + Db.ManagementPackage.COLUMN_ID)));
	    	query.from(Db.ManagementPackage.TABLE_NAME, "m");
	    	query.join(Db.ManagementPlatform.TABLE_NAME, "p", "p." + Db.ManagementPlatform.COLUMN_ID, "m." + Db.ManagementPackage.COLUMN_PLATFORM);
	    	query.where(Criterion.AND(Criterion.EQ("m." + Db.ManagementPackage.COLUMN_NAME, packageName), 
	    			Criterion.EQ("p." + Db.ManagementPlatform.COLUMN_ID, getSCCMPlatformId(serviceLocator))));
	    	
		Long packageID = queryExecutor.execute1(query);
	    return packageID;
	}   	
	
	
	/**
	 * 
	 * Grab platform ID from ManagementPlatform table.
	 *
	 */	
    private Long getSCCMPlatformId(final ServiceLocator serviceLocator)
    {
		// Get Platform ID
		SelectQuery query = Query.select(Query.column(Db.ManagementPlatform.COLUMN_ID));
			query.from(Db.ManagementPlatform.TABLE_NAME);
			query.where(Criterion.EQ(Db.ManagementPlatform.COLUMN_NAME, ReclamationPlanComponent.SCCM_PLATFORM));
			
		return serviceLocator.getDbApi().getQueryExecutor().execute1(query);
    }
}