package com.organization.live.survey.reclamation.impl;

/**
 * ReclamationEmail class represents an email object that is used by the ReclamationMessengerImpl class.
 * 
 * @author Danny Carvajal
 */
public class ReclamationEmail 
{
    private String to; 
    private String from; 
	private String subject;
	private String securityToken;
	private String body; 
	private Long reclamationComputerID;
	private String computerName;
	private String signatureName;	
	
	public void setTo(String to)
	{
		this.to = to;
	}
	
	public void setFrom(String from)
	{
		this.from = from;
	}
	
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	
	public void setSecurityToken(String securityToken)
	{
		this.securityToken = securityToken;
	}
	
	public void setMsg(String body)
	{
		this.body = body;
	}
	
	public void setReclamationComputerID(Long reclamationComputerID)
	{
		this.reclamationComputerID = reclamationComputerID;
	}
	
	public void setComputerName(String computerName)
	{
		this.computerName = computerName;
	}

	public void setSignatureName(String signatureName)
	{
		this.signatureName = signatureName;
	}
	
	public String getTo()
	{
		return this.to;
	}
	
	public String getFrom()
	{
		return this.from;
	}
	
	public String getSubject()
	{
		return this.subject;
	}
	
	public String getSecurityToken()
	{
		return this.securityToken;
	}
	
	public String getMsg()
	{
		return this.body;
	}
	
	public Long getReclamationComputerID()
	{
		return this.reclamationComputerID;
	}
	
	public String getComputerName()
	{
		return this.computerName;
	}

	public String getSignatureName()
	{
		return this.signatureName;
	}	
}
