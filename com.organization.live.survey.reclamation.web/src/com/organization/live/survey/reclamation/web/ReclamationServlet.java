package com.organization.live.survey.reclamation.web;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.organization.live.survey.reclamation.web.impl.ReclamationServiceImpl;

/**
 * 
 * @author Danny Carvajal
 * Testing - access servlet as https://localhost/reclamation/?rt=<y/n>&t=<token>&rci=<reclamationComputerID>
 * Note: All nauseating component logging is done in ReclamationServletImpl.
 */
public class ReclamationServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String INDEX_FILE = "/index.html";
	private static final String REPLACE_RESP = "{resp}";
    private static final String REPLACE_STYLE = "{style}";
    private static final String REQUEST_GET_IMAGE = "/css/images/";
	private static final String REQUEST_RECLAMATION_COMPUTERID = "rci";
	private static final String REQUEST_NO = "n";
	private static final String REQUEST_TYPE = "rt";
    private static final String REQUEST_TOKEN = "t";
    private static final String REQUEST_YES = "y";
    private static final String STYLE_FILE = "/css/reclamation.css";
	private static final Logger logger = LoggerFactory.getLogger(ReclamationServlet.class);
	
    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) 
    throws ServletException,
    IOException 
    {
    	String reqType = req.getParameter(REQUEST_TYPE);
    	String token = req.getParameter(REQUEST_TOKEN);
    	String reclamationComputerID = req.getParameter(REQUEST_RECLAMATION_COMPUTERID);
        String path = req.getRequestURI().substring(req.getContextPath().length(), req.getRequestURI().length());
        
        // Read the request
        if (path.equals("/")) 
        {
        	// Do we have valid request?
        	if ((!REQUEST_YES.equals(reqType) && 
        			!REQUEST_NO.equals(reqType)) || 
        			reclamationComputerID == null || 
        			token == null || 
        			!reclamationComputerID.matches("\\d+"))
        	{
        		writeResponse("An error has occurred. One or more expected parameters were missing from the response url.", resp);
        	}
        	else
        	{
        		try
        		{
	        		initRecalamationServlet(reqType, 
	        				Long.parseLong(reclamationComputerID), 
	        				token, 
	        				req, 
	        				resp);
        		}
        		catch(Exception e)
        		{
        			writeResponse("An error has occurred. The server was unable to process the request at this time.", resp);
        			logger.error(String.format("Reclamation servlet error occurred when processing a request. Error: %s", e.getMessage()));
        		}
        	}	
         }
         // Write out the logo
         else if (path.equals(REQUEST_GET_IMAGE))
         {
         	ReclamationServiceImpl reclamationService = ReclamationServiceHolder.getReclamationService();
        	byte[] img = reclamationService.getLogo();          
        	IOUtils.write(img, resp.getOutputStream());
         }
         // Write anything else out
         else 
         {
        	 InputStream is = null;
        	 try 
        	 {
        		 is = getClass().getResourceAsStream(path);
        		 if (is != null) 
        		 {
        			 IOUtils.copy(is, resp.getOutputStream());
        		 }
        	 } 
        	 finally 
        	 {
        		 IOUtils.closeQuietly(is);
        	 }
         }
    }

        
    
    public void setReclamationService(final ReclamationServiceImpl reclamationService) 
    {
    	ReclamationServiceHolder.setReclamationService(reclamationService);
    }     
    
    
    private void initRecalamationServlet(final String reqType, final Long reclamationComputerID, String token, final HttpServletRequest req, final HttpServletResponse resp) 
    throws IOException 
    {
     	boolean uninstall = false;
     	boolean processedResponse = false;
     	
    	StringBuilder msg = new StringBuilder("");
   		msg.append("Thank you for your response.<br/><br/>");
   		
    	if (REQUEST_YES.equals(reqType))
    	{
    		msg.append("You have chosen to uninstall the software specified in the email.");
    		uninstall = true;
    	}
    	else
    	{
    		msg.append("You have chosen to not uninstall software specified in the email.");
    	}	    	
    	   	
   		ReclamationServiceImpl reclamationService = ReclamationServiceHolder.getReclamationService();
   		processedResponse = reclamationService.updateEmailResponse(reclamationComputerID, token, uninstall);
   		
   		if (!processedResponse)
   		{
    	    msg.append(" However, we were unable to process this request. This can happen if the email response timeframe has expired.");
    	}
    	else
    	{
    		msg.append(" This response has been recorded and no further action is needed from you.");
    	}
    	   	
        writeResponse(msg.toString(), resp);
    }
    
    
    /**
     * 
     * Note: here I load the css file into the page in order to get IE to work with it. I have
     * tried a million changes and this is the only way I could get IE to render the css.
     */
    private void writeResponse(final String msg, final HttpServletResponse resp) 
    		throws IOException
    {    	
    	InputStream indexFileStream = getClass().getResourceAsStream(INDEX_FILE);   
    	InputStream styleFileStream = getClass().getResourceAsStream(STYLE_FILE);
    	
        try 
        {
        	String indexFile = IOUtils.toString(indexFileStream);
        	String styleFile = IOUtils.toString(styleFileStream);
        	
        	indexFile = indexFile.replace(REPLACE_STYLE, styleFile);
        	indexFile = indexFile.replace(REPLACE_RESP, msg);       
        	
        	IOUtils.write(indexFile, resp.getOutputStream());
        } 
        finally 
        {
        	IOUtils.closeQuietly(indexFileStream);
        	IOUtils.closeQuietly(styleFileStream);
        }
    } 
}
