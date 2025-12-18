/**
 * 
 */
package com.organization.live.survey.reclamation.ui.action;

import java.util.HashMap;
import java.util.Map;

import com.organization.live.pad.jms.DiscoveryListener;
import com.organization.live.pad.server.core.RunContext;
import com.organization.live.ui.scheduler.job.JobStatusListener;
import com.organization.live.ui.scheduler.job.JobStatus.JobStatusType;

/**
 * Listener to handle when the job status.
 * 
 * @author Danny Carvajal
 *
 */
public class UninstallSCCMPackageListener implements DiscoveryListener
{
	private Map<String, JobStatusListener> listeners = new HashMap<String, JobStatusListener>();   

    /**
     * Adds listener that will react on job status change.
     * 
     * @param runContext
     * @param listener
     */
    public void addJobStatusListener(final String runContext, final JobStatusListener listener) 
    {
        listeners.put(runContext, listener);
    }	
	
	public void onCancel(final RunContext ctx) 
	{
        JobStatusListener listener = listeners.remove(ctx.getId());
        if (listener != null) 
        {
            listener.onJobStatus(JobStatusType.CANCELED);
        }		
	}

	public void onFinish(RunContext ctx) 
	{
        JobStatusListener listener = listeners.remove(ctx.getId());
        if (listener != null) 
        {
            listener.onJobStatus(JobStatusType.FINISHED);
        }
	}

	@Override
	public void onRun(RunContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQueue(RunContext ctx) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSuspend(RunContext ctx) {
		// TODO Auto-generated method stub
		
	}
}
