package model.openproject;

import java.util.Date;

public class OPWorkPackage {
	public int id;
	public String _type;
	public String subject;
	public OPDescription description;
	public OPEmbeded _embedded;
	
	public Date startDate;
	public Date dueDate;
	
	public String estimatedTime; 
	public String remainingTime; 	
	public String spentTime;
	
	public int percentageDone;
	
	public Date createdAt;
	public Date updatedAt;
	
	public OPWorkPackageLink _links;

}

