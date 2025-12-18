package com.organization.live.survey.reclamation.component;

import com.organization.live.logger.LogDetails;
import com.organization.live.logger.LogDetailsColumn;
import com.organization.live.survey.reclamation.component.ReclamationComponent.LoggerTables;
import com.organization.live.logger.LogDetailsTable;

/**
 * Reclamation Logger object
 *
 * @author Danny Carvajal
 */
@LogDetailsTable(LoggerTables.LOGGER_DETAIL)
public class ReclamationLoggerDetails implements LogDetails 
{

    @LogDetailsColumn(LoggerTables.RECLAMATION_PLAN_ID)
    private final Long reclamation;

    /**
     * Constructs ReclamationLoggerDetails.
     * 
     * @param reclamation
     */
    public ReclamationLoggerDetails(final Long reclamation) 
    {
        this.reclamation = reclamation;
    }

    /**
     * Returns the reclamation Long.
     *
     * @return the reclamation Long.
     */
    public Long getReclamation() 
    {
        return reclamation;
    }
}
