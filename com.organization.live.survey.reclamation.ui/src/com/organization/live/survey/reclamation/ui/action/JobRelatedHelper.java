/**
 * 
 */
package com.organization.live.survey.reclamation.ui.action;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.web.util.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.organization.live.auth.SecurityToken;
import com.organization.live.common.exceptions.PadException;
import com.organization.live.converter.IpConverter;
import com.organization.live.db.Criterion;
import com.organization.live.db.entity.Entity;
import com.organization.live.db.entity.EntityManager;
import com.organization.live.pad.common.scan.ScanTarget;
import com.organization.live.pad.common.scan.ScanTargetInfoImpl;
import com.organization.live.pad.server.core.PadServerManager;
import com.organization.live.pad.server.core.RunContext;
import com.organization.live.ui.common.client.event.MessageEvent;
import com.organization.live.ui.pad.service.PadService;
import com.organization.live.ui.scheduler.job.JobStatusListener;
import com.organization.live.ui.scheduler.job.JobStatus.JobStatusType;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.exception.UIException;
import com.organization.live.ui.service.util.ClientMessagingService;

/**
 * 
 * @author Danny Carvajal, borrowed code from Bill Sommerville.
 *
 */
public class JobRelatedHelper
{
    private static final Logger logger = LoggerFactory.getLogger(UninstallSCCMPackageAction.class);	
    
	/**
	 * Returns a Discovery plan to use for the job.
	 * 
	 * @param mgr
	 * @param siteServer
	 * @param siteCode
	 * @return
	 */
	protected static Entity getOrCreateUninstallPlan(final EntityManager mgr, final String siteServer, final String siteCode)
	{
		Entity plan;
		List<Entity> plans = mgr.read("pad_probe_sccm_uninstall",
			Criterion.AND(Criterion.EQ("host", siteServer),
				Criterion.EQ("site_code", siteCode)));

		// Create a new plan if we didn't find one
		if (plans.isEmpty())
		{
			Entity template = getDefaultUninstallPlan(mgr);

			plan = mgr.create("pad_probe_sccm_uninstall");
			plan.set("name",
				template.getString("name") + " - " + siteServer + "\\" + siteCode);
			plan.set("seq_set", template.getLong("seq_set"));
			plan.set("template", template.getLong("template"));
			plan.set("host", siteServer);
			plan.set("site_code", siteCode);
		}

		else

			plan = plans.get(0);

		// (Re-)initialize the plan

		return plan;
	}

	
	/**
	 * Builds the job started message.
	 *
	 * @param jobName
	 * @param intentTime
	 * @param startTime
	 * @return
	 */
	protected static String getTraceMessage(final String jobName, final Date intentTime, final Date startTime)
	{
		final StringBuilder msgBuilder = new StringBuilder();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss");

		msgBuilder.append("Job: <b>");
		msgBuilder.append(HtmlUtils.htmlEscape(jobName));
		msgBuilder.append("</b> manually started on: ");
		msgBuilder.append(HtmlUtils.htmlEscape(dateFormat.format(intentTime)));
		msgBuilder.append(" ran on: ");
		msgBuilder.append(HtmlUtils.htmlEscape(dateFormat.format(startTime)));

		return msgBuilder.toString();
	}


	/**
	 * @param serviceLocator
	 * @return
	 */
    private static PadServerManager getPadServerManager(final ServiceLocator serviceLocator) 
    {
        PadService service = serviceLocator.getService(PadService.SERVICE_NAME);
        return service.getServerManager();
    }	
       
	
	/**
	 * @param target
	 * @param plan
	 * @param serviceLocator
	 * @throws Exception
	 */
	protected static void runJob(final ScanTargetInfoImpl target, final Entity plan, final Long padID,
		final UninstallSCCMPackageListener listener, final Date startDate, final ServiceLocator serviceLocator) throws Exception
	{
		final ClientMessagingService msgService = serviceLocator.getClientMessagingService();
		final String hostIPAddress = plan.getString("host");
		final InetAddress hostAddress = IpConverter.long2InetAddress(IpConverter.IpAddress2Long(hostIPAddress));
		SecurityToken token = serviceLocator.getDbApi().getUserToken();
		final Date intentTime = new Date();
		final String msgID = "ReclamationRunJob";
		Date startTime;

		try
		{
			target.setAddress(hostAddress);

			msgService.showAutoProgress("Checking PAD status...", serviceLocator);

			// Load targets into serializable object
			ArrayList<ScanTarget> scanTargets = new ArrayList<ScanTarget>();	
			for (ScanTarget scanTarget : Arrays.asList((ScanTarget) target))
				scanTargets.add(scanTarget);
					
			PadServerManager padServerManager = getPadServerManager(serviceLocator);
			Boolean isAlive = padServerManager.isAlive(padID, token);
			if (isAlive)
			{				
				final RunContext runContext = padServerManager.runProbe(padID, plan.getId(), scanTargets, token);
				listener.addJobStatusListener(runContext.getId(), new JobStatusListener()
				{
					@Override
					public void onJobStatus(JobStatusType statusType) 
					{
						if (JobStatusType.FINISHED.equals(statusType) || JobStatusType.CANCELED.equals(statusType)) 
						{
					        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss");
					        Date endTime = new Date();
					        String message = String.format("SCCM package uninstall job started on: %s <b>was finished</b> on: %s",
					                HtmlUtils.htmlEscape(dateFormat.format(startDate)), HtmlUtils.htmlEscape(dateFormat.format(endTime)));
					        MessageEvent messageEvent = MessageEvent.INFO(message);
					        messageEvent.setId(msgID);
					        serviceLocator.getClientMessagingService().sendMessage(messageEvent, serviceLocator);
						}
					}
		        });	
				
				logger.info("PAD {} will scan {}", padID, Arrays.asList((ScanTarget) target));		
			}
			else
			{
				throw new PadException(String.format("The PAD selected for this reclamation plan is currently inactive. (PAD id = %s)", padID));
			}
		}
		catch (final Exception e)
		{
			msgService.hideProgress(serviceLocator);
			throw e;
		}
		finally
		{
			msgService.hideProgress(serviceLocator);
		}

		startTime = new Date();
		MessageEvent messageEvent = MessageEvent.INFO(getTraceMessage(plan.getString("name"),
				intentTime, startTime));
		messageEvent.setId(msgID);
		msgService.sendMessage(messageEvent, serviceLocator);
	}


	/**
	 * Returns the default/first plan.
	 * 
	 * @param mgr
	 * @return
	 */
	protected static Entity getDefaultUninstallPlan(final EntityManager mgr)
	{
		Entity defaultPlan = null;
		List<Entity> templates = mgr.query("pad_probe_sccm_uninstall")
			.limitAndOffset(1, -1)
			.execute();

		if (!templates.isEmpty())
		{
			defaultPlan = templates.get(0);
		}

		else

			throw new UIException("Unable to locate the template Discovery plan");

		return defaultPlan;
	}

	
	/**
	 * Returns the default/first template.
	 * 
	 * @param mgr
	 * @return
	 */
	protected static Entity getDefaultUninstallTemplate(final EntityManager mgr)
	{
		Entity defaultTemplate = null;
		List<Entity> templates = mgr.query("pad_probe_sccm_uninstall")
			.limitAndOffset(1, -1)
			.execute();

		if (!templates.isEmpty())
		{
			defaultTemplate = templates.get(0);
		}

		else

			throw new UIException("Unable to locate the template Discovery template");

		return defaultTemplate;
	}
}
