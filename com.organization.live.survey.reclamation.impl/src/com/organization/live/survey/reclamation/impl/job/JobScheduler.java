package com.organization.live.survey.reclamation.impl.job;

import java.util.Date;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.entity.EntityManager;
import com.organization.live.db.meta.Fields;
import com.organization.live.db.meta.Tables;
import com.organization.live.db.transaction.Transaction;
import com.organization.live.db.transaction.TransactionCallback;
import com.organization.live.db.transaction.TransactionDefinition;
import com.organization.live.init.AbstractInitializer;
import com.organization.live.installer.InstallationContext;
import com.organization.live.installer.InstallationException;
import com.organization.live.scheduler.db.Db.DailySchedule;
import com.organization.live.scheduler.db.Db.Schedule;

/**
 * Reclamation Job Scheduler. This is no longer being used. Remember to delete.
 * 
 * @author Danny Carvajal
 */
public class JobScheduler extends AbstractInitializer 
{   
    public boolean install(final InstallationContext installerContext) throws InstallationException 
    {
        final DbApi dbApi = installerContext.getDbApi();
        TransactionDefinition definition = new TransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        TransactionCallback<Void> callback = new TransactionCallback<Void>() 
        {
            @SuppressWarnings("deprecation")
            @Override
            public Void doInTransaction(final Transaction tx) throws Exception 
            {
            	EntityManager mgr = dbApi.getEntityManager();
            	
            	// Get Job Id
            	Entity entity = mgr.readEntity(SchedDb.ReclamationJob.TABLE_NAME, Criterion.EQ(SchedDb.ReclamationJob.COLUMN_NAME, ReclamationJob.SERVICE));
            	Long jobId = entity.getId();
            			
                entity = mgr.readEntity(Schedule.TABLE_NAME, Criterion.EQ(Schedule.JOB_ID, jobId));
                if (entity == null) 
                {
                    Date time = new Date(0);                       
                    entity = mgr.create(DailySchedule.TABLE_NAME);
                    entity.set(DailySchedule.REPEAT_DAY, 1);
                    time.setHours(8);
                    entity.set(DailySchedule.TIME, time);
                    entity.set(Schedule.ACTIVE, true);
                    entity.set(Schedule.JOB_ID, jobId);
                    entity.set(Schedule.NAME, "Daily Reclamation check job schedule");
                    entity.set(Schedule.SYS_USER_ID, getAdminId(mgr));
                    entity.save();
                }
				return null;
            }                
        };
        
		dbApi.getThreadContext().transaction("Reclamation Job Install", definition,
				callback);
		
        return false;       
    }
    
    
    /**
     * Get id of admin user
     */
    private Long getAdminId(final EntityManager manager) 
    {
        Entity entity = manager.readEntity(Tables.SYS_USER, Criterion.EQ(Fields.USER_NAME, "admin"));
        if (entity != null) 
        {
            return entity.getId();
        }
        throw new IllegalStateException("User admin was not created");
    }

}
