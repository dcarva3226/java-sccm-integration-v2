package com.organization.live.survey.reclamation.db;

import static com.organization.live.db.query.Query.count;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.organization.live.db.Criterion;
import com.organization.live.db.DbApi;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.query.Query;
import com.organization.live.db.query.SelectQuery;
import com.organization.live.db.transaction.Transaction;
import com.organization.live.db.transaction.TransactionCallback;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.db.transaction.TransactionDefinition;
import com.organization.live.init.AbstractInitializer;
import com.organization.live.installer.InstallationContext;
import com.organization.live.installer.InstallationException;
import com.organization.live.plugin.Installable;
/**
 * Installs the Reclamation schema
 *
 * @author Danny Carvajal
 *
 */
public class Installer extends AbstractInitializer implements Installable 
{
	
    private static final Logger logger = LoggerFactory.getLogger(Installer.class);
    private static final int DEFAULT_AGENT_STALE_DAYS = 90;
    private static final boolean DEFAULT_ALLOW_RESTARTS = true;
    private static final int DEFAULT_EMAIL_EXPIRATION_DAYS = 14;
    private static final int DEFAULT_EMAIL_GROOM_DAYS = 90;
    private static final int DEFAULT_EMAIL_MAX_SEND_ATTEMPTS = 3;
    private static final int DEFAULT_EMAIL_SEND_WAIT_DAYS = 5;
    private static final boolean DEFAULT_ENABLED = false;
    private static final String DEFAULT_NAME = "Default Reclamation Configuration";
    private static final boolean DEFAULT_USE_OWNER = true;
    private static final  boolean DEFAULT_USE_USER = true;

    @Override
    public boolean install(final InstallationContext installerContext) throws InstallationException 
    {	
        final DbApi dbApi = installerContext.getDbApi();
		final TransactionDefinition definition = new TransactionDefinition(TransactionDefinition.PROPAGATION_NESTED);
        final TransactionCallback<Void> callback = new TransactionCallback<Void>()
        {
            @Override
            public Void doInTransaction(final Transaction tx) throws Exception
            {
            	/**
            	 * Make sure the opz_reclamation_config table has one record in it. The user may modify this record ...
            	 * because of this, I did not use com.organization.live.installer.utility.XmlDbImportInstaller 
            	 * to import the data row because it will overwrite on upgrade.
            	 */
        		SelectQuery query = Query.select(count());
        		query.from(Db.ReclamationConfig.TABLE_NAME);
        		Long recCount = dbApi.getQueryExecutor().execute1(query);
        		if (recCount == 0)
        		{
        	    	Entity rec = dbApi.getEntityManager().create(Db.ReclamationConfig.TABLE_NAME);
        			rec.set(Db.ReclamationConfig.COLUMN_AGENT_STALE_DAYS, DEFAULT_AGENT_STALE_DAYS);
        			rec.set(Db.ReclamationConfig.COLUMN_NAME, DEFAULT_NAME);
        	    	rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_ALLOW_RESTARTS, DEFAULT_ALLOW_RESTARTS);
        			rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_EXPIRATION_DAYS, DEFAULT_EMAIL_EXPIRATION_DAYS);
        			rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_GROOM_DAYS, DEFAULT_EMAIL_GROOM_DAYS);
        			rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_MAX_SEND_ATTEMPTS, DEFAULT_EMAIL_MAX_SEND_ATTEMPTS);
        			rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_EMAIL_SEND_WAIT_DAYS, DEFAULT_EMAIL_SEND_WAIT_DAYS);
        			rec.set(Db.ReclamationConfig.COLUMN_ENABLED, DEFAULT_ENABLED);
        			rec.set(Db.ReclamationConfig.COLUMN_USE_OWNER, DEFAULT_USE_OWNER);
        			rec.set(Db.ReclamationConfig.COLUMN_USE_USER, DEFAULT_USE_USER);
        			rec.save();
        		}
        		else if (recCount == 1)
        		{
        			// Table already exists
      				Entity rec = dbApi.getEntityManager().readEntity(Db.ReclamationConfig.TABLE_NAME, Criterion.EQ(Db.ReclamationConfig.COLUMN_NAME, DEFAULT_NAME));

      				if (rec.get(Db.ReclamationConfig.COLUMN_AGENT_STALE_DAYS) == null)
       					rec.set(Db.ReclamationConfig.COLUMN_AGENT_STALE_DAYS, DEFAULT_AGENT_STALE_DAYS);
      				
      				if (rec.get(Db.ReclamationConfig.COLUMN_DEFAULT_ALLOW_RESTARTS) == null)
       					rec.set(Db.ReclamationConfig.COLUMN_DEFAULT_ALLOW_RESTARTS, DEFAULT_ALLOW_RESTARTS);
      				      				
      				rec.isModified();
      					rec.save();
        		}        		
            	
        		logger.info("Reclamation configuration table initialized.");
            	return (null);
            }
        };

        dbApi.getThreadContext().transaction("Reclamation Configuration Table Initializer", definition, callback);
        
        return false;
    }
 }
