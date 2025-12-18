package com.organization.live.survey.reclamation.impl.job;

/**
 * @author Danny Carvajal
 */
public interface SchedDb 
{
    public static interface ReclamationJob
    {
        public static final String TABLE_NAME = "sys_sch_reclamation_job";
        public static final String COLUMN_NAME = "name";
    }
}
