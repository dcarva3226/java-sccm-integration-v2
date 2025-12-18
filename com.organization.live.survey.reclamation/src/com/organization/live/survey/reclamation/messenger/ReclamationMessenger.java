package com.organization.live.survey.reclamation.messenger;

import com.organization.live.db.DbApi;
import com.organization.live.notification.transport.service.TransportService;

/**
 * Messenger service.  Handles building and sending notification messages for Reclamation.
 * 
 * @author Danny Carvajal
 *
 */
public interface ReclamationMessenger
{		
	public void setTransportService(final TransportService transportService);
	
	public void setDbApi(final DbApi dbApi);	
	

    /**
     * Sends the specified e-mail message to the specified recipients.
     */	
	public Long sendEmail(); 
}
