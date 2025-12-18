package com.organization.live.survey.reclamation;

import com.organization.live.db.DbApi;
import com.organization.live.survey.reclamation.component.ReclamationComponent;

/**
 * @author Danny Carvajal
 */
public interface Reclamation extends ReclamationComponent
{
	public abstract String getName();    

    public abstract void doRun(DbApi dbApi);
}
