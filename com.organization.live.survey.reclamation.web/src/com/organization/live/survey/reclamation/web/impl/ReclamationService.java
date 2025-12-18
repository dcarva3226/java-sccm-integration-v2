package com.organization.live.survey.reclamation.web.impl;

import com.organization.live.db.DbApi;
import com.organization.live.logger.Logger;

/**
 * 
 * @author Danny Carvajal
 *
 */
public interface ReclamationService 
{
	void init(final DbApi dbApi, Logger logger, String token);
}
