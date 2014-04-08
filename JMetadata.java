


import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.logging.*;
import java.sql.*;

import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.occa.security.*;
import com.crystaldecisions.sdk.occa.security.internal.*;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.framework.internal.*;
import com.crystaldecisions.sdk.plugin.desktop.folder.*;
import com.crystaldecisions.sdk.exception.*;
import com.crystaldecisions.sdk.properties.*;
import com.businessobjects.rebean.wi.*;

public class Jdata
{
	Properties prp = new Properties();
	private static String logFile = "BOForce.log";
	private final static DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss ");
	public FileHandler handler;
	// Add to the desired logger
	Logger logger;

	public Jdata(String propertyFileName)
	{
		try
		{
			handler = new FileHandler(logFile, true);
			logger = Logger.getLogger("BOForce");
			logger.addHandler(handler);
			this.prp.load(new BufferedInputStream(new FileInputStream(propertyFileName)));

		}
		catch(IOException ioEx)
		{
		 //log error
		 //System.out.println(ioEx.getMessage());
		 logger.severe(Jdata.df.format(new java.util.Date()) + " " + ioEx.getMessage());
		 return;
		}
		catch(Exception ex)
		{
			//log error
			//System.out.println(ex.getMessage());
			logger.severe(Jdata.df.format(new java.util.Date()) + " " + ex.getMessage());
			return;
		}
	}
	public String getData(int currentTask)
	{

		String user,pwd,cmsName,reports,dbID,dbPwd;
		if (currentTask == 4)
		{
			user = prp.getProperty("UserName");
			pwd = prp.getProperty("Password");
			cmsName = prp.getProperty("CMSName");
		}
		else
		{
			user = prp.getProperty("IntegrationUserName");
			pwd = prp.getProperty("IntegrationPassword");
			cmsName = prp.getProperty("IntegrationCMSName");
		}
		reports = prp.getProperty("ArtifactName");
		dbID = prp.getProperty("DatabaseID");
		dbPwd = prp.getProperty("DatabasePwd");
		Boolean doCommit = false;

		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.";
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.";
		}
		if( reports == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " You have not provided any Reports. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " You have not provided any Reports. Please check biar.properties.";
		}

		try
		{
			BufferedWriter out = null;
			if (currentTask == 4)
			{
				out = new BufferedWriter(new FileWriter("QueryPanelMetadata.txt"));
			}
			else
			{
				out = new BufferedWriter(new FileWriter("IntegrationQueryPanelMetadata.txt"));
			}
			String report = "(";
			String[] reportNames = reports.split(",");
			for (int p=0;p<=reportNames.length-1;p++)
			{
				report = report + "'" + reportNames[p] +"',";
			}
			report = report.substring(0,report.length()-1) + ")";

			//Retrieve the ISessionMgr object to perform the logon
			ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

			//Logon to Enterprise
			IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

			//Retrieve the InfoStore object from the Enterprise Session
			IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
			//System.out.println("Logged In");
			//The query used to retrieve the name
			String query = "";
			IInfoObjects oInfoObjects,oFolderObjects,allObjects;

			if (reports.compareToIgnoreCase("ALL") == 0)
			{
				query = "SELECT SI_ID FROM CI_INFOOBJECTS WHERE SI_NAME IN ('I/CAD Reports','I/LEADS Reports')";
				allObjects = oInfoStore.query(query);
				String parents = "(";
				for (int parent=0;parent<=allObjects.getResultSize()-1;parent++)
				{
					IInfoObject parentObject = (IInfoObject)allObjects.get(parent);
					int parentid = parentObject.getID();
					parents = parents + parentid+",";
				}
				parents = parents.substring(0,parents.length()-1) + ")";

				query = "SELECT SI_PARENTID,SI_ID,SI_NAME FROM CI_INFOOBJECTS WHERE SI_KIND='WEBI' AND  SI_INSTANCE=0 AND SI_PARENTID IN "+parents;
			}
			else
			{
				query = "SELECT SI_PARENTID,SI_ID,SI_NAME FROM CI_INFOOBJECTS WHERE SI_KIND='WEBI' AND  SI_INSTANCE=0 AND SI_NAME IN "+report;
			}
			//Execite the query and retrieve the folder name
			oInfoObjects = oInfoStore.query(query);
			if (oInfoObjects.getResultSize()>0)
			{
				// Retrieve the Report Engines
				ReportEngines boReportEngines = (ReportEngines)oEnterpriseSession.getService("ReportEngines");

				// Retrieve the Report Engine for Web Intelligence documents
				ReportEngine boReportEngine = boReportEngines.getService(ReportEngines.ReportEngineType.WI_REPORT_ENGINE);

				int objectCount = oInfoObjects.getResultSize();
				for(int n=0;n<=objectCount-1;n++)
				{
					IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(n);
					query = "SELECT SI_NAME,SI_PATH FROM CI_INFOOBJECTS WHERE SI_ID = " + oInfoObject.getParentID();
					oFolderObjects = oInfoStore.query(query);
					IInfoObject oFolderObject = (IInfoObject)oFolderObjects.get(0);
					if ((oFolderObject.getTitle().trim().compareTo("I/CAD Reports") != 0) && (oFolderObject.getTitle().trim().compareTo("I/LEADS Reports")!=0))
					{
						continue;
					}
					//String[] folderNames;
					System.out.println("");
					System.out.println("");
					out.write("\n");
					out.write("\n");
					String folderPath = "Public Folders/"+ oFolderObject.getTitle() + "/";
					System.out.println("Document Name:- "+ oInfoObject.getTitle());
					out.write("Document Name:- "+ oInfoObject.getTitle()+"\n");
					System.out.println("Document Path:- "+folderPath);
					out.write("Document Path:- "+folderPath+"\n");
					//if (((IFolder)oFolderObject).getPath() != null)
					//{
						//folderNames = ((IFolder)oFolderObject).getPath();
						//int folderCount = folderNames.length;
						//if (folderCount>0)
						//{
							//for (int o=0;o<=folderCount-1;o++)
							//{
								//folderPath = folderPath + folderNames[o] + "/";
							//}
						//}
					//}
					//folderPath = folderPath + oFolderObject.getTitle() + "/";

					// Retrieve the document instance for the Web Intelligence document
					DocumentInstance boDocumentInstance = boReportEngine.openDocument(oInfoObject.getID());

					DataProviders dataProviders = boDocumentInstance.getDataProviders();
					System.out.println("Document Contexts raised when refreshing the document:- "+boDocumentInstance.getContextPrompts().getCount());
					out.write("Document Contexts raised when refreshing the document:- "+boDocumentInstance.getContextPrompts().getCount()+"\n");
					System.out.println("If any universe contexts must be filled? "+boDocumentInstance.getMustFillContexts());
					out.write("If any universe contexts must be filled? "+boDocumentInstance.getMustFillContexts()+"\n");

					int providerCount = dataProviders.getCount();
					if (providerCount <= 0)
					{
						continue;
					}
					//doCommit = true;
					for (int providerIndex = 0;providerIndex<=providerCount-1;providerIndex++)
					{
						System.out.println("");
						System.out.println("");
						out.write("\n");
						out.write("\n");
						// Retrieve the data provider to be viewed
						DataProvider boDataProvider = dataProviders.getItem(providerIndex);

						System.out.println("Data provider Name:- "+boDataProvider.getName());
						out.write("Data provider Name:- "+boDataProvider.getName()+"\n");
						System.out.println("Universe Name:- "+boDataProvider.getSource());
						out.write("Universe Name:- "+boDataProvider.getSource()+"\n");
						//Retrieve SQL data provider
						SQLDataProvider sqlDataProvider = (SQLDataProvider)boDocumentInstance.getDataProviders().getItem(providerIndex);
						SQLContainer sqlContainer = sqlDataProvider.getSQLContainer();
						System.out.println("");
						System.out.println("");
						out.write("\n");
						out.write("\n");
						System.out.println("Query:- ");
						out.write("Query:- "+"\n");
						for(int w=0;w<=sqlContainer.getChildCount()-1;w++)
						{
							SQLNode node = (SQLNode)sqlContainer.getChildAt(0);
							SQLSelectStatement select = (SQLSelectStatement)node;
							System.out.println(select.getSQL());
							out.write(select.getSQL()+"\n");
						}

						//Gets the Query
						Query docQuery = boDataProvider.getQuery();

						int objects = docQuery.getResultObjectCount();
						for (int i = 0; i<=objects-1;i++)
						{
							System.out.println("");
							out.write("\n");
							DataSourceObject source = docQuery.getResultObject(i);

							System.out.print("Select Clause:- "+source.getQualification().toString()+"<-"+source.getName());
							//out.write("Select Clause:- "+source.getQualification().toString()+"<-"+source.getName());
							String selectClause = "Select Clause:- "+source.getQualification().toString()+":"+source.getName();
							source = (DataSourceObject)source.getParent();
							while (source!=null)
							{
								System.out.print("<-"+source.getQualification().toString()+"<-"+source.getName());
								//out.write("<-"+source.getQualification().toString()+"<-"+source.getName()+"\n");
								selectClause = selectClause + "<" + source.getQualification().toString()+":"+source.getName();
								source = (DataSourceObject)source.getParent();
							}

							out.write(selectClause+"\n");

						}

						if (docQuery.hasCondition())
						{
							System.out.println("");
							out.write("\n");
							ConditionContainer filter = docQuery.getCondition();
							getWhereClause(filter,out);

							FilterConditionContainer filterCondition = (FilterConditionContainer)filter;
							System.out.println("");
							System.out.println("");
							out.write("\n");
							out.write("\n");
							System.out.print("Prompt Operators:- ");
							out.write("Prompt Operators:- ");
							for(int x=0;x<=filterCondition.getChildCount()-1;x++)
							{
								FilterConditionNode fcNode = (FilterConditionNode)filterCondition.getChildAt(x);
								System.out.print(fcNode.getName()+" "+filterCondition.getOperator().toString()+" ");
								out.write(fcNode.getName()+" "+filterCondition.getOperator().toString()+" ");
							}

							Prompts prompts = boDocumentInstance.getPrompts();
							int promptCount = prompts.getCount();
							if (promptCount>0)
							{
								System.out.println("");
								System.out.println("");
								out.write("\n");
								out.write("\n");
								for (int z = 0;z<=promptCount-1;z++)
								{
									Prompt prompt = prompts.getItem(z);
									System.out.println("Prompt:- "+prompt.getName());
									out.write("Prompt:- "+prompt.getName()+"\n");
									String[] defValues = prompt.getDefaultValues();
									if (defValues.length>0)
									{
										String values = "Default Values:- ";
										for(int y=0;y<=defValues.length-1;y++)
										{
											values = values + defValues[y]+",";
										}
										values = values.substring(0,values.length()-1);
										System.out.println(values);
										out.write(values+"\n");
									}
									System.out.println("Default Input Format:-"+prompt.getInputFormat());
									out.write("Default Input Format:-"+prompt.getInputFormat()+"\n");
									String[] prevValues = prompt.getPreviousValues();
									if (prevValues.length>0)
									{
										System.out.println("Will value(s) entered be kept for the next time the user is prompted? Yes");
										out.write("Will value(s) entered be kept for the next time the user is prompted? Yes \n");
									}
									else
									{
										System.out.println("Will value(s) entered be kept for the next time the user is prompted? No");
										out.write("Will value(s) entered be kept for the next time the user is prompted? No \n");
									}
									System.out.println("Does this prompt have a list of values? "+prompt.hasLOV());
									out.write("Does this prompt have a list of values? "+prompt.hasLOV()+"\n");
									System.out.println("Whether a user can type a value or not? "+prompt.isConstrained());
									out.write("Whether a user can type a value or not? "+prompt.isConstrained()+"\n");
									System.out.println("What is the Prompt Type? "+prompt.getType().toString());
									out.write("What is the Prompt Type? "+prompt.getType().toString()+"\n");
									if (prompt.requireAnswer())
									{
										System.out.println("Is this an optional prompt? No");
										out.write("Is this an optional prompt? No \n");
									}
									else
									{
										System.out.println("Is this an optional prompt? Yes");
										out.write("Is this an optional prompt? Yes \n");
									}
									while (prompt.getLOV().getNestedPrompts().getCount()>0)
									{
										prompt = prompt.getLOV().getNestedPrompts().getItem(0);
										System.out.println("Nested Prompt:- "+prompt.getName());
										out.write("Nested Prompt:- "+prompt.getName()+"\n");
										defValues = prompt.getDefaultValues();
										if (defValues.length>0)
										{
											String values = "Default Values:- ";
											for(int y=0;y<=defValues.length-1;y++)
											{
												values = values + defValues[y]+",";
											}
											values = values.substring(0,values.length()-1);
											System.out.println(values);
											out.write(values+"\n");
										}
										System.out.println("Default Input Format:-"+prompt.getInputFormat());
										out.write("Default Input Format:-"+prompt.getInputFormat()+"\n");
										prevValues = prompt.getPreviousValues();
										if (prevValues.length>0)
										{
											System.out.println("Will value(s) entered be kept for the next time the user is prompted? Yes");
											out.write("Will value(s) entered be kept for the next time the user is prompted? Yes \n");
										}
										else
										{
											System.out.println("Will value(s) entered be kept for the next time the user is prompted? No");
											out.write("Will value(s) entered be kept for the next time the user is prompted? No \n");
										}
										System.out.println("Does this Nested prompt have a list of values? "+prompt.hasLOV());
										out.write("Does this prompt have a list of values? "+prompt.hasLOV()+"\n");
										System.out.println("Whether a user can type a value or not? "+prompt.isConstrained());
										out.write("Whether a user can type a value or not? "+prompt.isConstrained()+"\n");
										System.out.println("What is the Nested Prompt Type? "+prompt.getType().toString());
										out.write("What is the Nested Prompt Type? "+prompt.getType().toString()+"\n");
										if (prompt.requireAnswer())
										{
											System.out.println("Is this an optional Nested prompt? No");
											out.write("Is this an optional Nested prompt? No \n");
										}
										else
										{
											System.out.println("Is this an Nested optional prompt? Yes");
											out.write("Is this an optional Nested prompt? Yes \n");
										}

									}

								}
							}
							PromptOrder pOrder = docQuery.getPromptOrder();
							promptCount = pOrder.getCount();
							if (promptCount>0)
							{
								System.out.println("");
								System.out.println("");
								out.write("\n");
								out.write("\n");
								String orderPrompt = "Prompt Order:- ";
								for (int z = 0;z<=promptCount-1;z++)
								{
									ConditionPrompt cPrompt = pOrder.getItem(z);
									orderPrompt = orderPrompt + cPrompt.getQuestion()+",";
								}
								orderPrompt = orderPrompt.substring(0,orderPrompt.length()-1);
								System.out.println(orderPrompt);
								out.write(orderPrompt+"\n");
							}
						}

						Scope scope = docQuery.getScope();
						int scopeCount = scope.getScopeObjectCount();
						if (scopeCount>0)
						{
							System.out.println("");
							out.write("\n");
							for(int l = 0;l<=scopeCount-1;l++)
							{
								System.out.println("");
								out.write("\n");
								DataSourceObject sSource = scope.getScopeObject(l);
								System.out.print("Scope:- "+sSource.getQualification().toString()+"<-"+sSource.getName());
								//out.write("Scope:- "+sSource.getQualification().toString()+"<-"+sSource.getName());
								String scopeClause = "Scope:- "+sSource.getQualification().toString()+":"+sSource.getName();
								sSource = (DataSourceObject)sSource.getParent();
								while (sSource!=null)
								{
									System.out.print("<-"+sSource.getQualification().toString()+"<-"+sSource.getName());
									//out.write("<-"+sSource.getQualification().toString()+"<-"+sSource.getName()+"\n");
									scopeClause = scopeClause + "<"+sSource.getQualification().toString()+":"+sSource.getName();
									sSource = (DataSourceObject)sSource.getParent();
								}

								out.write(scopeClause+"\n");
							}
						}
					}
				}
				out.close();
			}
			else
			{
				//System.out.println("No reports matched your criteria");
				return "No reports matched your criteria";
			}

			oEnterpriseSession.logoff();
		}
		catch(SDKException sdkEx)
		{
			//System.out.println(sdkEx.getMessage());
			logger.severe(Jdata.df.format(new java.util.Date()) + " " + sdkEx.getMessage());
			return Jdata.df.format(new java.util.Date()) + " " + sdkEx.getMessage();
			//return false;

		}
		catch(IOException ioex)
		{
			logger.severe(Jdata.df.format(new java.util.Date()) + " " + ioex.getMessage());
			return Jdata.df.format(new java.util.Date()) + " " + ioex.getMessage();
		}

		return "STATUS:OK";
	}

	public void getWhereClause(ConditionContainer filter, BufferedWriter out) throws IOException
	{
		int childCount = filter.getChildCount();
		for (int k = 0;k<=childCount-1;k++)
		{
			System.out.println("");
			out.write("\n");
			FilterConditionNode fNode = (FilterConditionNode)filter.getChildAt(k);
			String fromClause = null;
			Boolean innerFrom = false;
			if (fNode.getClass().getName() == "com.businessobjects.wp.om.filter.OMFilterSimple")
			{
				DataSourceObject cSource = ((ConditionObject)fNode).getDataSourceObject();
				System.out.print("Where Clause:- "+cSource.getQualification().toString()+"<-"+cSource.getName());
				//out.write("From Clause:- "+cSource.getQualification().toString()+"<-"+cSource.getName());
				fromClause = "Where Clause:- "+cSource.getQualification().toString()+":"+cSource.getName();
				cSource = (DataSourceObject)cSource.getParent();
				while (cSource!=null)
				{
					System.out.print("<-"+cSource.getQualification().toString()+"<-"+cSource.getName());
					//out.write("<-"+cSource.getQualification().toString()+"<-"+cSource.getName()+"\n");
					fromClause = fromClause + "<"+cSource.getQualification().toString()+":"+cSource.getName();
					cSource = (DataSourceObject)cSource.getParent();
				}
			}
			else if (fNode.getClass().getName() == "com.businessobjects.wp.om.filter.OMConditionContainer")
			{
				ConditionContainer innerFilter = (ConditionContainer)fNode;
				getWhereClause(innerFilter,out);
			}
			else
			{
				int omChildCount = fNode.getChildCount();
				for (int m=0;m<=omChildCount-1;m++)
				{
					System.out.println("");
					out.write("\n");
					DataSourceObject omSource = ((ConditionObject)fNode.getChildAt(m)).getDataSourceObject();
					System.out.print("Where Clause:- "+omSource.getQualification().toString()+"<-"+omSource.getName());
					//out.write("From Clause:- "+omSource.getQualification().toString()+"<-"+omSource.getName());
					String innerFromClause = "Where Clause:- "+omSource.getQualification().toString()+":"+omSource.getName();
					omSource = (DataSourceObject)omSource.getParent();
					while (omSource!=null)
					{
						System.out.print("<-"+omSource.getQualification().toString()+"<-"+omSource.getName());
						//out.write("<-"+omSource.getQualification().toString()+"<-"+omSource.getName()+"\n");
						innerFromClause = innerFromClause+"<"+omSource.getQualification().toString()+":"+omSource.getName();
						omSource = (DataSourceObject)omSource.getParent();
					}

					innerFrom = true;
					if (innerFromClause != null)
					{
						out.write(innerFromClause+"\n");
					}
				}
			}

			//FROMINFO
			if (!innerFrom)
			{
				if (fromClause != null)
				{
					out.write(fromClause+"\n");
				}
			}
			innerFrom = false;
}
	}

	public String getReportData(int currentTask)
	{
		String user,pwd,cmsName,reports,dbID,dbPwd;

		if (currentTask == 4)
		{
			user = prp.getProperty("UserName");
			pwd = prp.getProperty("Password");
			cmsName = prp.getProperty("CMSName");
		}
		else
		{
			user = prp.getProperty("IntegrationUserName");
			pwd = prp.getProperty("IntegrationPassword");
			cmsName = prp.getProperty("IntegrationCMSName");
		}
		reports = prp.getProperty("ArtifactName");
		dbID = prp.getProperty("DatabaseID");
		dbPwd = prp.getProperty("DatabasePwd");
		Boolean doCommit = false;

		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.";
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.";
		}
		if( reports == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jdata.df.format(new java.util.Date()) + " You have not provided any Reports. Please check biar.properties.");
			//return false;
			return Jdata.df.format(new java.util.Date()) + " You have not provided any Reports. Please check biar.properties.";
		}

		try
		{
			BufferedWriter out = null;
			if (currentTask == 4)
			{
				out = new BufferedWriter(new FileWriter("ReportPanelMetadata.txt"));
			}
			else
			{
				out = new BufferedWriter(new FileWriter("IntegrationReportPanelMetadata.txt"));
			}
			String report = "(";
			String[] reportNames = reports.split(",");
			for (int p=0;p<=reportNames.length-1;p++)
			{
				report = report + "'" + reportNames[p] +"',";
			}
			report = report.substring(0,report.length()-1) + ")";
			//Retrieve the ISessionMgr object to perform the logon
			ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

			//Logon to Enterprise
			IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

			//Retrieve the InfoStore object from the Enterprise Session
			IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
			//System.out.println("Logged In");
			//The query used to retrieve the name
			String query = "";
			IInfoObjects oInfoObjects,oFolderObjects,allObjects;

			if (reports.compareToIgnoreCase("ALL") == 0)
			{
				query = "SELECT SI_ID FROM CI_INFOOBJECTS WHERE SI_NAME IN ('I/CAD Reports','I/LEADS Reports')";
				allObjects = oInfoStore.query(query);
				String parents = "(";
				for (int parent=0;parent<=allObjects.getResultSize()-1;parent++)
				{
					IInfoObject parentObject = (IInfoObject)allObjects.get(parent);
					int parentid = parentObject.getID();
					parents = parents + parentid+",";
				}
				parents = parents.substring(0,parents.length()-1) + ")";
				query = "SELECT SI_PARENTID,SI_ID,SI_NAME FROM CI_INFOOBJECTS WHERE SI_KIND='WEBI' AND  SI_INSTANCE=0 AND SI_PARENTID IN "+parents;
			}
			else
			{
				query = "SELECT SI_PARENTID,SI_ID,SI_NAME FROM CI_INFOOBJECTS WHERE SI_KIND='WEBI' AND  SI_INSTANCE=0 AND SI_NAME IN "+report;
			}

			//Execite the query and retrieve the folder name
			oInfoObjects = oInfoStore.query(query);
			if (oInfoObjects.getResultSize()>0)
			{
				// Retrieve the Report Engines
				ReportEngines boReportEngines = (ReportEngines)oEnterpriseSession.getService("ReportEngines");

				// Retrieve the Report Engine for Web Intelligence documents
				ReportEngine boReportEngine = boReportEngines.getService(ReportEngines.ReportEngineType.WI_REPORT_ENGINE);


				int objectCount = oInfoObjects.getResultSize();
				for(int n=0;n<=objectCount-1;n++)
				{
					System.out.println("");
					System.out.println("");
					out.write("\n");
					out.write("\n");
					IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(n);
					System.out.println("Document Name:- "+ oInfoObject.getTitle());
					out.write("Document Name:- "+ oInfoObject.getTitle()+"\n");
					query = "SELECT SI_NAME,SI_PATH FROM CI_INFOOBJECTS WHERE SI_ID = " + oInfoObject.getParentID();
					oFolderObjects = oInfoStore.query(query);
					IInfoObject oFolderObject = (IInfoObject)oFolderObjects.get(0);
					if ((oFolderObject.getTitle().trim().compareTo("I/CAD Reports") != 0) && (oFolderObject.getTitle().trim().compareTo("I/LEADS Reports")!=0))
					{
						continue;
					}
					DocumentInstance boDocumentInstance = boReportEngine.openDocument(oInfoObject.getID());
					java.util.Properties docProperties = boDocumentInstance.getProperties();
					System.out.println("Should the Document Refresh On Open? "+ docProperties.getProperty(PropertiesType.REFRESH_ON_OPEN));
					out.write("Should the Document Refresh On Open? "+ docProperties.getProperty(PropertiesType.REFRESH_ON_OPEN)+"\n");
					System.out.println("Should the Document Auto Merge Dimensions? "+ docProperties.getProperty(PropertiesType.MERGE_DIMENSION));
					out.write("Should the Document Auto Merge Dimensions? "+ docProperties.getProperty(PropertiesType.MERGE_DIMENSION)+"\n");
					int reportCount = boDocumentInstance.getStructure().getReportElementCount();
					int dpi = boDocumentInstance.getMediaDPI();
					for(int i=0;i<=reportCount-1;i++)
					{
						System.out.println("");
						out.write("\n");
						ReportContainer reportContainer = (ReportContainer)boDocumentInstance.getStructure().getReportElement(i);
						System.out.println("Report Name:- "+reportContainer.getName());
						out.write("Report Name:- "+reportContainer.getName()+"\n");

						System.out.println("Page Orientation:- "+reportContainer.getPageInfo().getOrientation());
						out.write("Page Orientation:- "+reportContainer.getPageInfo().getOrientation()+"\n");
						System.out.println("Page Size:- "+reportContainer.getPageInfo().getPaperSize());
						out.write("Page Size:- "+reportContainer.getPageInfo().getPaperSize()+"\n");
						System.out.println("Top Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getTop()*dpi)/25.4)+" px");
						out.write("Top Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getTop()*dpi)/25.4)+" px\n");
						System.out.println("Bottom Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getBottom()*dpi)/25.4)+" px");
						out.write("Bottom Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getBottom()*dpi)/25.4)+" px\n");
						System.out.println("Left Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getLeft()*dpi)/25.4)+" px");
						out.write("Left Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getLeft()*dpi)/25.4)+" px\n");
						System.out.println("Right Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getRight()*dpi)/25.4)+" px");
						out.write("Right Margin:- "+Math.round((reportContainer.getPageInfo().getMargins().getRight()*dpi)/25.4)+" px\n");

						System.out.println("Page Header Height:- "+Math.round((reportContainer.getPageHeader().getHeight()*dpi)/25.4)+" px");
						out.write("Page Header Height:- "+Math.round((reportContainer.getPageHeader().getHeight()*dpi)/25.4)+" px\n");

						System.out.println("Report Header Vertical Alignment:- "+reportContainer.getPageHeader().getAlignment().getVertical());
						out.write("Report Header Vertical Alignment:- "+reportContainer.getPageHeader().getAlignment().getVertical()+"\n");
						System.out.println("Report Header Horizal Alignment:- "+reportContainer.getPageHeader().getAlignment().getHorizontal());
						out.write("Report Header Horizontal Alignment:- "+reportContainer.getPageHeader().getAlignment().getHorizontal()+"\n");
						System.out.println("Report Header Background Vertical Alignment:- "+reportContainer.getPageHeader().getBackgroundAlignment().getVertical());
						out.write("Report Header Background Vertical Alignment:- "+reportContainer.getPageHeader().getBackgroundAlignment().getVertical()+"\n");
						System.out.println("Report Header Background Horizal Alignment:- "+reportContainer.getPageHeader().getBackgroundAlignment().getHorizontal());
						out.write("Report Header Background Horizontal Alignment:- "+reportContainer.getPageHeader().getBackgroundAlignment().getHorizontal()+"\n");
						System.out.println("Report Header Font:- "+reportContainer.getPageHeader().getFont().getName());
						out.write("Report Header Font:- "+reportContainer.getPageHeader().getFont().getName()+"\n");
						System.out.println("Report Header Font Size:- "+reportContainer.getPageHeader().getFont().getSize());
						out.write("Report Header Font Size:- "+reportContainer.getPageHeader().getFont().getSize()+"\n");
						System.out.println("Report Header Font Style:- "+reportContainer.getPageHeader().getFont().getStyle());
						out.write("Report Header Font Style:- "+reportContainer.getPageHeader().getFont().getStyle()+"\n");
						java.awt.Color color = reportContainer.getPageHeader().getAttributes().getBackground();
						System.out.println("Report Header BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
						out.write("Report Header Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
						if (reportContainer.getPageHeader().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
						{
							System.out.println("Report Header Border size:- "+((SimpleBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getSize());
							out.write("Report Header Border size:- "+((SimpleBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getSize()+"\n");
						}
						else
						{
							System.out.println("Report Header Top Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getTop().getSize());
							out.write("Report Header Top Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getTop().getSize()+"\n");
							System.out.println("Report Header Bottom Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getBottom().getSize());
							out.write("Report Header Bottom Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getBottom().getSize()+"\n");
							System.out.println("Report Header Left Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getLeft().getSize());
							out.write("Report Header Left Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getLeft().getSize()+"\n");
							System.out.println("Report Header Right Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getRight().getSize());
							out.write("Report Header Right Border size:- "+((ComplexBorder)reportContainer.getPageHeader().getAttributes().getBorder()).getRight().getSize()+"\n");
						}
						System.out.println("Report Header Background Image Display Mode:- "+reportContainer.getPageHeader().getAttributes().getBackgroundImageDisplayMode().toString());
						out.write("Report Header Background Image Display Mode:- "+reportContainer.getPageHeader().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
						if (reportContainer.getPageHeader().getAttributes().getBackgroundImageURL()!=null)
						{
							System.out.println("Report Header Background Image Url:- "+reportContainer.getPageHeader().getAttributes().getBackgroundImageURL().toString());
							out.write("Report Header Background Image Url:- "+reportContainer.getPageHeader().getAttributes().getBackgroundImageURL().toString()+"\n");
						}
						if (reportContainer.getPageHeader().getAttributes().getSkin()!=null)
						{
							System.out.println("Report Header Skin Name:- "+reportContainer.getPageHeader().getAttributes().getSkin().getName());
							out.write("Report Header Skin Name:- "+reportContainer.getPageHeader().getAttributes().getSkin().getName()+"\n");
							System.out.println("Report Header Skin Url:- "+reportContainer.getPageHeader().getAttributes().getSkin().getUrl());
							out.write("Report Header Skin Url:- "+reportContainer.getPageHeader().getAttributes().getSkin().getUrl()+"\n");
						}

						int headerCount = reportContainer.getPageHeader().getReportElementCount();
						if (headerCount>0)
						{
							System.out.println("");
							out.write("\n");
							System.out.println("Report Header Elements*********");
							out.write("Report Header Elements*********\n");
						}
						for (int headerLoopCounter=0;headerLoopCounter<=headerCount-1;headerLoopCounter++)
						{
							if(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellExpr")
							{
								System.out.println("");
								out.write("\n");
								ReportCell cell = (ReportCell)reportContainer.getPageHeader().getReportElement(headerLoopCounter);
								getReportCellProperties("Report Cell",cell,out,dpi,"Report Header");
								getBaseCellProperties("Report Cell",cell,out,dpi,"Report Header");
							}
							else if(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellLabel")
							{
								System.out.println("");
								out.write("\n");
								FreeCell cell = (FreeCell)reportContainer.getPageHeader().getReportElement(headerLoopCounter);
								getFreeCellProperties("Free Cell",cell,out,dpi,"Report Header");
								getBaseCellProperties("Free Cell",cell,out,dpi,"Report Header");
							}
							else if(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
							{
								System.out.println("");
								out.write("\n");
								ReportBlock block = (ReportBlock)reportContainer.getPageHeader().getReportElement(headerLoopCounter);
								getBaseBlockProperties("Report Block",block,out,dpi,"Report Header");
							}
							else if(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
							{
								System.out.println("");
								out.write("\n");
								SectionContainer scontainer = (SectionContainer)reportContainer.getPageHeader().getReportElement(headerLoopCounter);
								getSectionContainerProperties("Section Container",scontainer,out,dpi,"Report Header");
							}
							else
							{
								System.out.println(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName());
								out.write(reportContainer.getPageHeader().getReportElement(headerLoopCounter).getClass().getName()+"\n");
								System.out.println("Unrecognised Report Element");
								out.write("Unrecognised Report Element\n");
							}
						}


						System.out.println("");
						out.write("\n");
						System.out.println("Report Footer Height:- "+Math.round((reportContainer.getPageFooter().getHeight()*dpi)/25.4)+" px");
						out.write("Report Footer Height:- "+Math.round((reportContainer.getPageFooter().getHeight()*dpi)/25.4)+" px\n");
						System.out.println("Report Footer Vertical Alignment:- "+reportContainer.getPageFooter().getAlignment().getVertical());
						out.write("Report Footer Vertical Alignment:- "+reportContainer.getPageFooter().getAlignment().getVertical()+"\n");
						System.out.println("Report Footer Horizal Alignment:- "+reportContainer.getPageFooter().getAlignment().getHorizontal());
						out.write("Report Footer Horizontal Alignment:- "+reportContainer.getPageFooter().getAlignment().getHorizontal()+"\n");
						System.out.println("Report Footer Background Vertical Alignment:- "+reportContainer.getPageFooter().getBackgroundAlignment().getVertical());
						out.write("Report Footer Background Vertical Alignment:- "+reportContainer.getPageFooter().getBackgroundAlignment().getVertical()+"\n");
						System.out.println("Report Footer Background Horizal Alignment:- "+reportContainer.getPageFooter().getBackgroundAlignment().getHorizontal());
						out.write("Report Footer Background Horizontal Alignment:- "+reportContainer.getPageFooter().getBackgroundAlignment().getHorizontal()+"\n");
						System.out.println("Report Footer Font:- "+reportContainer.getPageFooter().getFont().getName());
						out.write("Report Footer Font:- "+reportContainer.getPageFooter().getFont().getName()+"\n");
						System.out.println("Report Footer Font Size:- "+reportContainer.getPageFooter().getFont().getSize());
						out.write("Report Footer Font Size:- "+reportContainer.getPageFooter().getFont().getSize()+"\n");
						System.out.println("Report Footer Font Style:- "+reportContainer.getPageFooter().getFont().getStyle());
						out.write("Report Footer Font Style:- "+reportContainer.getPageFooter().getFont().getStyle()+"\n");
						color = reportContainer.getPageFooter().getAttributes().getBackground();
						if (color != null)
						{
							System.out.println("Report Footer BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
							out.write("Report Footer Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
						}
						if (reportContainer.getPageFooter().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
						{
							System.out.println("Report Footer Border size:- "+((SimpleBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getSize());
							out.write("Report Footer Border size:- "+((SimpleBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getSize()+"\n");
						}
						else
						{
							System.out.println("Report Footer Top Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getTop().getSize());
							out.write("Report Footer Top Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getTop().getSize()+"\n");
							System.out.println("Report Footer Bottom Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getBottom().getSize());
							out.write("Report Footer Bottom Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getBottom().getSize()+"\n");
							System.out.println("Report Footer Left Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getLeft().getSize());
							out.write("Report Footer Left Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getLeft().getSize()+"\n");
							System.out.println("Report Footer Right Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getRight().getSize());
							out.write("Report Footer Right Border size:- "+((ComplexBorder)reportContainer.getPageFooter().getAttributes().getBorder()).getRight().getSize()+"\n");
						}
						System.out.println("Report Footer Background Image Display Mode:- "+reportContainer.getPageFooter().getAttributes().getBackgroundImageDisplayMode().toString());
						out.write("Report Footer Background Image Display Mode:- "+reportContainer.getPageFooter().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
						if (reportContainer.getPageFooter().getAttributes().getBackgroundImageURL()!=null)
						{
							System.out.println("Report Footer Background Image Url:- "+reportContainer.getPageFooter().getAttributes().getBackgroundImageURL().toString());
							out.write("Report Footer Background Image Url:- "+reportContainer.getPageFooter().getAttributes().getBackgroundImageURL().toString()+"\n");
						}
						if (reportContainer.getPageFooter().getAttributes().getSkin()!=null)
						{
							System.out.println("Report Footer Skin Name:- "+reportContainer.getPageFooter().getAttributes().getSkin().getName());
							out.write("Report Footer Skin Name:- "+reportContainer.getPageFooter().getAttributes().getSkin().getName()+"\n");
							System.out.println("Report Footer Skin Url:- "+reportContainer.getPageFooter().getAttributes().getSkin().getUrl());
							out.write("Report Footer Skin Url:- "+reportContainer.getPageFooter().getAttributes().getSkin().getUrl()+"\n");
						}


						int footerCount = reportContainer.getPageFooter().getReportElementCount();
						if (footerCount>0)
						{
							System.out.println("Report Footer Elements*********");
							out.write("Report Footer Elements*********\n");
						}
						for (int footerLoopCounter=0;footerLoopCounter<=footerCount-1;footerLoopCounter++)
						{
							if(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellExpr")
							{
								System.out.println("");
								out.write("\n");
								ReportCell cell = (ReportCell)reportContainer.getPageFooter().getReportElement(footerLoopCounter);
								getReportCellProperties("Report Cell",cell,out,dpi,"Report Footer");
								getBaseCellProperties("Report Cell",cell,out,dpi,"Report Footer");
							}
							else if(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellLabel")
							{
								System.out.println("");
								out.write("\n");
								FreeCell cell = (FreeCell)reportContainer.getPageFooter().getReportElement(footerLoopCounter);
								getFreeCellProperties("Free Cell",cell,out,dpi,"Report Footer");
								getBaseCellProperties("Free Cell",cell,out,dpi,"Report Footer");
							}
							else if(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
							{
								System.out.println("");
								out.write("\n");
								ReportBlock block = (ReportBlock)reportContainer.getPageFooter().getReportElement(footerLoopCounter);
								getBaseBlockProperties("Report Block",block,out,dpi,"Report Footer");
							}
							else if(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
							{
								System.out.println("");
								out.write("\n");
								SectionContainer scontainer = (SectionContainer)reportContainer.getPageFooter().getReportElement(footerLoopCounter);
								getSectionContainerProperties("Section Container",scontainer,out,dpi,"Report Footer");
							}
							else
							{
								System.out.println(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName());
								out.write(reportContainer.getPageFooter().getReportElement(footerLoopCounter).getClass().getName()+"\n");
								System.out.println("Unrecognised Report Element");
								out.write("Unrecognised Report Element\n");
							}
						}

						System.out.println("");
						out.write("\n");
						System.out.println("Report Body Vertical Alignment:- "+reportContainer.getReportBody().getAlignment().getVertical());
						out.write("Report Body Vertical Alignment:- "+reportContainer.getReportBody().getAlignment().getVertical()+"\n");
						System.out.println("Report Body Horizal Alignment:- "+reportContainer.getReportBody().getAlignment().getHorizontal());
						out.write("Report Body Horizontal Alignment:- "+reportContainer.getReportBody().getAlignment().getHorizontal()+"\n");
						System.out.println("Report Body Background Vertical Alignment:- "+reportContainer.getReportBody().getBackgroundAlignment().getVertical());
						out.write("Report Body Background Vertical Alignment:- "+reportContainer.getReportBody().getBackgroundAlignment().getVertical()+"\n");
						System.out.println("Report Body Background Horizal Alignment:- "+reportContainer.getReportBody().getBackgroundAlignment().getHorizontal());
						out.write("Report Body Background Horizontal Alignment:- "+reportContainer.getReportBody().getBackgroundAlignment().getHorizontal()+"\n");
						System.out.println("Report Body Font:- "+reportContainer.getReportBody().getFont().getName());
						out.write("Report Body Font:- "+reportContainer.getReportBody().getFont().getName()+"\n");
						System.out.println("Report Body Font Size:- "+reportContainer.getReportBody().getFont().getSize());
						out.write("Report Body Font Size:- "+reportContainer.getReportBody().getFont().getSize()+"\n");
						System.out.println("Report Body Font Style:- "+reportContainer.getReportBody().getFont().getStyle());
						out.write("Report Body Font Style:- "+reportContainer.getReportBody().getFont().getStyle()+"\n");
						color = reportContainer.getReportBody().getAttributes().getBackground();
						System.out.println("Report Body BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
						out.write("Report Body Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
						if (reportContainer.getReportBody().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
						{
							System.out.println("Report Body Border size:- "+((SimpleBorder)reportContainer.getReportBody().getAttributes().getBorder()).getSize());
							out.write("Report Body Border size:- "+((SimpleBorder)reportContainer.getReportBody().getAttributes().getBorder()).getSize()+"\n");
						}
						else
						{
							System.out.println("Report Body Top Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getTop().getSize());
							out.write("Report Body Top Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getTop().getSize()+"\n");
							System.out.println("Report Body Bottom Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getBottom().getSize());
							out.write("Report Body Bottom Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getBottom().getSize()+"\n");
							System.out.println("Report Body Left Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getLeft().getSize());
							out.write("Report Body Left Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getLeft().getSize()+"\n");
							System.out.println("Report Body Right Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getRight().getSize());
							out.write("Report Body Right Border size:- "+((ComplexBorder)reportContainer.getReportBody().getAttributes().getBorder()).getRight().getSize()+"\n");
						}
						System.out.println("Report Body Background Image Display Mode:- "+reportContainer.getReportBody().getAttributes().getBackgroundImageDisplayMode().toString());
						out.write("Report Body Background Image Display Mode:- "+reportContainer.getReportBody().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
						if (reportContainer.getReportBody().getAttributes().getBackgroundImageURL()!=null)
						{
							System.out.println("Report Body Background Image Url:- "+reportContainer.getReportBody().getAttributes().getBackgroundImageURL().toString());
							out.write("Report Body Background Image Url:- "+reportContainer.getReportBody().getAttributes().getBackgroundImageURL().toString()+"\n");
						}
						if (reportContainer.getReportBody().getAttributes().getSkin()!=null)
						{
							System.out.println("Report Body Skin Name:- "+reportContainer.getReportBody().getAttributes().getSkin().getName());
							out.write("Report Body Skin Name:- "+reportContainer.getReportBody().getAttributes().getSkin().getName()+"\n");
							System.out.println("Report Body Skin Url:- "+reportContainer.getReportBody().getAttributes().getSkin().getUrl());
							out.write("Report Body Skin Url:- "+reportContainer.getReportBody().getAttributes().getSkin().getUrl()+"\n");
						}


						int bodyCount = reportContainer.getReportBody().getReportElementCount();
						if (bodyCount>0)
						{
							System.out.println("Report Body Elements*********");
							out.write("Report Body Elements*********\n");
						}
						for (int bodyLoopCounter=0;bodyLoopCounter<=bodyCount-1;bodyLoopCounter++)
						{
							if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellExpr")
							{
								System.out.println("");
								out.write("\n");
								ReportCell cell = (ReportCell)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getReportCellProperties("Report Cell",cell,out,dpi,"Report Body");
								getBaseCellProperties("Report Cell",cell,out,dpi,"Report Body");
							}
							else if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellLabel")
							{
								System.out.println("");
								out.write("\n");
								FreeCell cell = (FreeCell)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getFreeCellProperties("Free Cell",cell,out,dpi,"Report Body");
								getBaseCellProperties("Free Cell",cell,out,dpi,"Report Body");
							}
							else if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
							{
								System.out.println("");
								out.write("\n");
								ReportBlock block = (ReportBlock)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getBaseBlockProperties("Report Block",block,out,dpi,"Report Body");
							}
							else if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
							{
								System.out.println("");
								out.write("\n");
								SectionContainer scontainer = (SectionContainer)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getSectionContainerProperties("Section Container",scontainer,out,dpi,"Report Body");
							}
							else
							{
								System.out.println(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName());
								out.write(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()+"\n");
								System.out.println("Unrecognised Report Element");
								out.write("Unrecognised Report Element\n");
							}
						}

						if(reportContainer.hasFilter())
						{
							System.out.println("");
							out.write("\n");
							System.out.println("Report Body has following filters:- "+reportContainer.getFilter());
							out.write("Report Body has following filters:- "+reportContainer.getFilter()+"\n");
						}
					}
				}
				out.close();
			}

			oEnterpriseSession.logoff();
		}
		catch(SDKException sdkEx)
		{
			//System.out.println(sdkEx.getMessage());
			logger.severe(Jdata.df.format(new java.util.Date()) + " " + sdkEx.getMessage());
			return Jdata.df.format(new java.util.Date()) + " " + sdkEx.getMessage();
			//return false;

		}
		catch(IOException ioex)
		{
			logger.severe(Jdata.df.format(new java.util.Date()) + " " + ioex.getMessage());
			return Jdata.df.format(new java.util.Date()) + " " + ioex.getMessage();
		}

		return "STATUS:OK";
	}

	public void getSectionContainerProperties(String element,SectionContainer scontainer,BufferedWriter out,int dpi,String parentContainer) throws IOException
	{
		if (scontainer.getSectionCells().length>0)
		{
			ReportCell[] cells = scontainer.getSectionCells();
			System.out.println(cells[0].getExpr());
			element = element + " " + cells[0].getExpr();
		}
		System.out.println(element+" Height:- "+Math.round((scontainer.getHeight()*dpi)/25.4)+" px");
		out.write(element+" Height:- "+Math.round((scontainer.getHeight()*dpi)/25.4)+" px\n");
		System.out.println(element+" Vertical Alignment:- "+scontainer.getAlignment().getVertical());
		out.write(element+" Vertical Alignment:- "+scontainer.getAlignment().getVertical()+"\n");
		System.out.println(element+" Horizal Alignment:- "+scontainer.getAlignment().getHorizontal());
		out.write(element+" Horizontal Alignment:- "+scontainer.getAlignment().getHorizontal()+"\n");
		System.out.println(element+" Background Vertical Alignment:- "+scontainer.getBackgroundAlignment().getVertical());
		out.write(element+" Background Vertical Alignment:- "+scontainer.getBackgroundAlignment().getVertical()+"\n");
		System.out.println(element+" Background Horizal Alignment:- "+scontainer.getBackgroundAlignment().getHorizontal());
		out.write(element+" Background Horizontal Alignment:- "+scontainer.getBackgroundAlignment().getHorizontal()+"\n");
		System.out.println(element+" Font:- "+scontainer.getFont().getName());
		out.write(element+" Font:- "+scontainer.getFont().getName()+"\n");
		System.out.println(element+" Font Size:- "+scontainer.getFont().getSize());
		out.write(element+" Font Size:- "+scontainer.getFont().getSize()+"\n");
		System.out.println(element+" Font Style:- "+scontainer.getFont().getStyle());
		out.write(element+" Font Style:- "+scontainer.getFont().getStyle()+"\n");
		java.awt.Color color = scontainer.getAttributes().getBackground();
		if (color != null)
		{
			System.out.println(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		if (scontainer.getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
		{
			System.out.println(element+" Border size:- "+((SimpleBorder)scontainer.getAttributes().getBorder()).getSize());
			out.write(element+" Border size:- "+((SimpleBorder)scontainer.getAttributes().getBorder()).getSize()+"\n");
		}
		else
		{
			System.out.println(element+" Top Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getTop().getSize());
			out.write(element+" Top Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getTop().getSize()+"\n");
			System.out.println(element+" Bottom Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getBottom().getSize());
			out.write(element+" Bottom Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getBottom().getSize()+"\n");
			System.out.println(element+" Left Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getLeft().getSize());
			out.write(element+" Left Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getLeft().getSize()+"\n");
			System.out.println(element+" Right Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getRight().getSize());
			out.write(element+" Right Border size:- "+((ComplexBorder)scontainer.getAttributes().getBorder()).getRight().getSize()+"\n");
		}
		System.out.println(element+" Background Image Display Mode:- "+scontainer.getAttributes().getBackgroundImageDisplayMode().toString());
		out.write(element+" Background Image Display Mode:- "+scontainer.getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
		if (scontainer.getAttributes().getBackgroundImageURL()!=null)
		{
			System.out.println(element+" Background Image Url:- "+scontainer.getAttributes().getBackgroundImageURL().toString());
			out.write(element+" Background Image Url:- "+scontainer.getAttributes().getBackgroundImageURL().toString()+"\n");
		}
		if (scontainer.getAttributes().getSkin()!=null)
		{
			System.out.println(element+" Skin Name:- "+scontainer.getAttributes().getSkin().getName());
			out.write(element+" Skin Name:- "+scontainer.getAttributes().getSkin().getName()+"\n");
			System.out.println(element+" Skin Url:- "+scontainer.getAttributes().getSkin().getUrl());
			out.write(element+" Skin Url:- "+scontainer.getAttributes().getSkin().getUrl()+"\n");
		}

		if(scontainer.getHAttachTo()==null)
		{
			System.out.println(element+" Horizontal Relative Position:- "+Math.round((scontainer.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer);
			out.write(element+" Horizontal Relative Position:- "+Math.round((scontainer.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((scontainer.getX()*dpi)/25.4)+" px From "+scontainer.getHorizontalAnchor()+" Edge Of "+scontainer.getHAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((scontainer.getX()*dpi)/25.4)+" px From "+scontainer.getHorizontalAnchor()+" Edge Of "+scontainer.getHAttachTo()+"\n");

		}
		if(scontainer.getVAttachTo()==null)
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((scontainer.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer);
			out.write(element+" Vertical Relative Position:- "+Math.round((scontainer.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((scontainer.getY()*dpi)/25.4)+" px From "+scontainer.getVerticalAnchor()+" Edge Of "+scontainer.getVAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((scontainer.getY()*dpi)/25.4)+" px From "+scontainer.getVerticalAnchor()+" Edge Of "+scontainer.getVAttachTo()+"\n");
		}

		System.out.println(element+" Should Start On a New Page?:- "+scontainer.startOnNewPage());
		out.write(element+" Should Start On a New Page?:- "+scontainer.startOnNewPage()+"\n");
		System.out.println(element+" Should Avoid Page Break?:- "+scontainer.avoidPageBreak());
		out.write(element+" Should Avoid Page Break?:- "+scontainer.avoidPageBreak()+"\n");
		System.out.println(element+" Should Section Cells be Repeated On Every Page?:- "+scontainer.repeatHeadersOnEveryPage());
		out.write(element+" Should Section Cells be Repeated On Every Page?:- "+scontainer.repeatHeadersOnEveryPage()+"\n");
		System.out.println(element+" Bottom Padding:- "+Math.round((scontainer.getBottomPadding()*dpi)/25.4)+" px");
		out.write(element+" Bottom Padding:- "+Math.round((scontainer.getBottomPadding()*dpi)/25.4)+" px\n");
		System.out.println(element+" Is Indexed?:- "+scontainer.isInIndex());
		out.write(element+" Is Indexed?:- "+scontainer.isInIndex()+"\n");

		BlockSort vsort = scontainer.getAxis().getBlockSort();
		System.out.print(element+" Sort is Horizontal? "+vsort.isHorizontal());
		if (vsort.getCount()>0)
		{
			System.out.print(element+" Vertical Sort Priorities are:- ");
			out.write(element+" Vertical Sort Priorities are:- ");
			for(int vloop=0;vloop<=vsort.getCount()-1;vloop++)
			{
				System.out.print(vsort.getSortElement(vloop).getExpression()+"("+vsort.getSortElement(vloop).getType()+") ");
				out.write(vsort.getSortElement(vloop).getExpression()+"("+vsort.getSortElement(vloop).getType()+") ");
			}
			System.out.println("");
			out.write("\n");
		}

		int sectionCount = scontainer.getReportElementCount();
		if (sectionCount>0)
		{
			System.out.println(element+" Elements*********");
		}
		for (int sectionLoopCounter=0;sectionLoopCounter<=sectionCount-1;sectionLoopCounter++)
		{
			if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellExpr")
			{
				System.out.println("");
				out.write("\n");
				ReportCell cell = (ReportCell)scontainer.getReportElement(sectionLoopCounter);
				getReportCellProperties("Report Cell",cell,out,dpi,element);
				getBaseCellProperties("Report Cell",cell,out,dpi,element);
			}
			else if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportCellLabel")
			{
				System.out.println("");
				out.write("\n");
				FreeCell cell = (FreeCell)scontainer.getReportElement(sectionLoopCounter);
				getFreeCellProperties("Free Cell",cell,out,dpi,element);
				getBaseCellProperties("Free Cell",cell,out,dpi,element);
			}
			else if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
			{
				System.out.println("");
				out.write("\n");
				ReportBlock block = (ReportBlock)scontainer.getReportElement(sectionLoopCounter);
				getBaseBlockProperties("Report Block",block,out,dpi,element);
			}
			else if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
			{
				System.out.println("");
				out.write("\n");
				SectionContainer sinnercontainer = (SectionContainer)scontainer.getReportElement(sectionLoopCounter);
				getSectionContainerProperties("Section Container",sinnercontainer,out,dpi,element);
			}
			else
			{
				System.out.println(scontainer.getReportElement(sectionLoopCounter).getClass().getName());
				out.write(scontainer.getReportElement(sectionLoopCounter).getClass().getName()+"\n");
				System.out.println("Unrecognised Report Element");
				out.write("Unrecognised Report Element\n");
			}
		}
		if(scontainer.hasFilter())
		{
			System.out.println(element+" has following filters:- "+scontainer.getFilter());
			out.write(element+" has following filters:- "+scontainer.getFilter()+"\n");
		}
	}

	public void getBaseBlockProperties(String element,ReportBlock reportElement,BufferedWriter out,int dpi,String parentContainer) throws IOException
	{
		System.out.println(element+" Name:- "+reportElement.getName());
		out.write(element+" Name:- "+reportElement.getName()+"\n");
		System.out.println(element+" Type:- "+reportElement.getRepresentation().getType());
		out.write(element+" Type:- "+reportElement.getRepresentation().getType()+"\n");
		System.out.println(element+" Repeat On Every Page?:- "+reportElement.repeatOnEveryPage());
		out.write(element+" Repeat On Every Page?:- "+reportElement.repeatOnEveryPage()+"\n");
		System.out.println(element+" Show When Empty?:- "+reportElement.isShowWhenEmpty());
		out.write(element+" Show When Empty?:- "+reportElement.isShowWhenEmpty()+"\n");
		System.out.println(element+" Avoid Duplicate Row Aggregation?:- "+reportElement.getDuplicateRowAggregation());
		out.write(element+" Avoid Duplicate Row Aggregation?:- "+reportElement.getDuplicateRowAggregation()+"\n");
		System.out.println(element+" Should Start On a New Page?:- "+reportElement.startOnNewPage());
		out.write(element+" Should Start On a New Page?:- "+reportElement.startOnNewPage()+"\n");
		System.out.println(element+" Should Avoid Page Break?:- "+reportElement.avoidPageBreak());
		out.write(element+" Should Avoid Page Break?:- "+reportElement.avoidPageBreak()+"\n");

		if(reportElement.getHAttachTo()==null)
		{
			System.out.println(element+" Horizontal Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer);
			out.write(element+" Horizontal Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From "+reportElement.getHorizontalAnchor()+" Edge Of "+reportElement.getHAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From "+reportElement.getHorizontalAnchor()+" Edge Of "+reportElement.getHAttachTo()+"\n");

		}
		if(reportElement.getVAttachTo()==null)
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer);
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From "+reportElement.getVerticalAnchor()+" Edge Of "+reportElement.getVAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From "+reportElement.getVerticalAnchor()+" Edge Of "+reportElement.getVAttachTo()+"\n");
		}

		BlockSort hsort = reportElement.getBlockSort(true);
		BlockSort vsort = reportElement.getBlockSort(false);
		if (hsort.getCount()>0)
		{
			System.out.print(element+" Horizontal Sort Priorities are:- ");
			out.write(element+" Horizontal Sort Priorities are:- ");
			for(int hloop=0;hloop<=hsort.getCount()-1;hloop++)
			{
				System.out.print(hsort.getSortElement(hloop).getExpression()+"("+hsort.getSortElement(hloop).getType()+") ");
				out.write(hsort.getSortElement(hloop).getExpression()+"("+hsort.getSortElement(hloop).getType()+") ");
			}
			System.out.println("");
			out.write("\n");
		}
		if (vsort.getCount()>0)
		{
			System.out.print(element+" Vertical Sort Priorities are:- ");
			out.write(element+" Vertical Sort Priorities are:- ");
			for(int vloop=0;vloop<=vsort.getCount()-1;vloop++)
			{
				System.out.print(vsort.getSortElement(vloop).getExpression()+"("+vsort.getSortElement(vloop).getType()+") ");
				out.write(vsort.getSortElement(vloop).getExpression()+"("+vsort.getSortElement(vloop).getType()+") ");
			}
			System.out.println("");
			out.write("\n");
		}
		if (reportElement.getRepresentation().toString().startsWith("com.businessobjects.rebean.wi.Graph") == false)
		{
			if (reportElement.getAxis(TableAxis.VERTICAL).getCount()>0)
			{
				BlockBreak vbreak = reportElement.getAxis(TableAxis.VERTICAL).getBlockBreak();
				if (vbreak.getCount()>0)
				{
					for(int vloop=0;vloop<=vbreak.getCount()-1;vloop++)
					{
						int vloop1 = vloop+1;
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" has expression:- "+vbreak.getBreakElement(vloop).getExpression());
						out.write(element+" Horizontal Break with Priority "+vloop1+" has expression:- "+vbreak.getBreakElement(vloop).getExpression()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" has implicit sort? "+vbreak.getBreakElement(vloop).hasImplicateSort());
						out.write(element+" Horizontal Break with Priority "+vloop1+" has implicit sort? "+vbreak.getBreakElement(vloop).hasImplicateSort()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should avoid page break? "+vbreak.getBreakElement(vloop).isAvoidPageBreak());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should avoid page break? "+vbreak.getBreakElement(vloop).isAvoidPageBreak()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the header of the break be shown in every page? "+vbreak.getBreakElement(vloop).isHeaderOnEveryPage());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the header of the break be shown in every page? "+vbreak.getBreakElement(vloop).isHeaderOnEveryPage()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the header of the break be visible? "+vbreak.getBreakElement(vloop).isHeaderVisible());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the header of the break be visible? "+vbreak.getBreakElement(vloop).isHeaderVisible()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the footer of the break be visible? "+vbreak.getBreakElement(vloop).isFooterVisible());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the footer of the break be visible? "+vbreak.getBreakElement(vloop).isFooterVisible()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should a different break value be placed on a new page? "+vbreak.getBreakElement(vloop).isStartOnNewPage());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should a different break value be placed on a new page? "+vbreak.getBreakElement(vloop).isStartOnNewPage()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the break value centred across rows or columns? "+vbreak.getBreakElement(vloop).isValueCentered());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the break value centred across rows or columns? "+vbreak.getBreakElement(vloop).isValueCentered()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the break value be repeated on each page? "+vbreak.getBreakElement(vloop).isValueOnNewPage());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the break value be repeated on each page? "+vbreak.getBreakElement(vloop).isValueOnNewPage()+"\n");
						System.out.println(element+" Horizontal Break with Priority "+vloop1+" should the break's value be repeated on each row or column? "+vbreak.getBreakElement(vloop).isValueRepeated());
						out.write(element+" Horizontal Break with Priority "+vloop1+" should the break value be repeated on each row or column? "+vbreak.getBreakElement(vloop).isValueRepeated()+"\n");
					}
				}
				BlockCalculation vhcalc = reportElement.getAxis(TableAxis.VERTICAL).getHBlockCalculation();
				BlockCalculation vvcalc = reportElement.getAxis(TableAxis.VERTICAL).getVBlockCalculation();
				if (vhcalc != null)
				{
					if (vhcalc.getCount()>0)
					{
						System.out.print(element+" Horizontal Calculation Priorities are:- ");
						out.write(element+" Horizontal Calculation Priorities are:- ");
						for(int vloop=0;vloop<=vhcalc.getCount()-1;vloop++)
						{
							System.out.print(vhcalc.getCalculationElement(vloop).getExpression()+"("+vhcalc.getCalculationElement(vloop).getType()+") ");
							out.write(vhcalc.getCalculationElement(vloop).getExpression()+"("+vhcalc.getCalculationElement(vloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
				if (vvcalc != null)
				{
					if (vvcalc.getCount()>0)
					{
						System.out.print(element+" Vertical Calculation Priorities are:- ");
						out.write(element+" Vertical Calculation Priorities are:- ");
						for(int vloop=0;vloop<=vvcalc.getCount()-1;vloop++)
						{
							System.out.print(vvcalc.getCalculationElement(vloop).getExpression()+"("+vvcalc.getCalculationElement(vloop).getType()+") ");
							out.write(vvcalc.getCalculationElement(vloop).getExpression()+"("+vvcalc.getCalculationElement(vloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
			}
			if (reportElement.getAxis(TableAxis.HORIZONTAL).getCount()>0)
			{
				BlockBreak hbreak = reportElement.getAxis(TableAxis.HORIZONTAL).getBlockBreak();
				if (hbreak.getCount()>0)
				{
					for(int hloop=0;hloop<=hbreak.getCount()-1;hloop++)
					{
						int hloop1 = hloop+1;
						System.out.println(element+" Vertical Break with Priority "+hloop1+" has expression:- "+hbreak.getBreakElement(hloop).getExpression());
						out.write(element+" Vertical Break with Priority "+hloop1+" has expression:- "+hbreak.getBreakElement(hloop).getExpression()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" has implicit sort? "+hbreak.getBreakElement(hloop).hasImplicateSort());
						out.write(element+" Vertical Break with Priority "+hloop1+" has implicit sort? "+hbreak.getBreakElement(hloop).hasImplicateSort()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should avoid page break? "+hbreak.getBreakElement(hloop).isAvoidPageBreak());
						out.write(element+" Vertical Break with Priority "+hloop1+" should avoid page break? "+hbreak.getBreakElement(hloop).isAvoidPageBreak()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the header of the break be shown in every page? "+hbreak.getBreakElement(hloop).isHeaderOnEveryPage());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the header of the break be shown in every page? "+hbreak.getBreakElement(hloop).isHeaderOnEveryPage()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the header of the break be visible? "+hbreak.getBreakElement(hloop).isHeaderVisible());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the header of the break be visible? "+hbreak.getBreakElement(hloop).isHeaderVisible()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the footer of the break be visible? "+hbreak.getBreakElement(hloop).isFooterVisible());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the footer of the break be visible? "+hbreak.getBreakElement(hloop).isFooterVisible()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should a different break value be placed on a new page? "+hbreak.getBreakElement(hloop).isStartOnNewPage());
						out.write(element+" Vertical Break with Priority "+hloop1+" should a different break value be placed on a new page? "+hbreak.getBreakElement(hloop).isStartOnNewPage()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the break value centred across rows or columns? "+hbreak.getBreakElement(hloop).isValueCentered());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the break value centred across rows or columns? "+hbreak.getBreakElement(hloop).isValueCentered()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the break value be repeated on each page? "+hbreak.getBreakElement(hloop).isValueOnNewPage());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the break value be repeated on each page? "+hbreak.getBreakElement(hloop).isValueOnNewPage()+"\n");
						System.out.println(element+" Vertical Break with Priority "+hloop1+" should the break's value be repeated on each row or column? "+hbreak.getBreakElement(hloop).isValueRepeated());
						out.write(element+" Vertical Break with Priority "+hloop1+" should the break value be repeated on each row or column? "+hbreak.getBreakElement(hloop).isValueRepeated()+"\n");
					}
				}
				BlockCalculation hvcalc = reportElement.getAxis(TableAxis.HORIZONTAL).getVBlockCalculation();
				BlockCalculation hhcalc = reportElement.getAxis(TableAxis.HORIZONTAL).getHBlockCalculation();
				if (hvcalc != null)
				{
					if (hvcalc.getCount()>0)
					{
						System.out.print(element+" Vertical Calculation Priorities are:- ");
						out.write(element+" Vertical Calculation Priorities are:- ");
						for(int hloop=0;hloop<=hvcalc.getCount()-1;hloop++)
						{
							System.out.print(hvcalc.getCalculationElement(hloop).getExpression()+"("+hvcalc.getCalculationElement(hloop).getType()+") ");
							out.write(hvcalc.getCalculationElement(hloop).getExpression()+"("+hvcalc.getCalculationElement(hloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
				if (hhcalc != null)
				{
					if (hhcalc.getCount()>0)
					{
						System.out.print(element+" Horizontal Calculation Priorities are:- ");
						out.write(element+" Horizontal Calculation Priorities are:- ");
						for(int hloop=0;hloop<=hhcalc.getCount()-1;hloop++)
						{
							System.out.print(hhcalc.getCalculationElement(hloop).getExpression()+"("+hhcalc.getCalculationElement(hloop).getType()+") ");
							out.write(hhcalc.getCalculationElement(hloop).getExpression()+"("+hhcalc.getCalculationElement(hloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
			}
			if (reportElement.getAxis(TableAxis.CONTENT).getCount()>0)
			{
				BlockBreak cbreak = reportElement.getAxis(TableAxis.CONTENT).getBlockBreak();
				if (cbreak.getCount()>0)
				{
					for(int cloop=0;cloop<=cbreak.getCount()-1;cloop++)
					{
						int cloop1 = cloop+1;
						System.out.println(element+" Break with Priority "+cloop1+" has expression:- "+cbreak.getBreakElement(cloop).getExpression());
						out.write(element+" Break with Priority "+cloop1+" has expression:- "+cbreak.getBreakElement(cloop).getExpression()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" has implicit sort? "+cbreak.getBreakElement(cloop).hasImplicateSort());
						out.write(element+" Break with Priority "+cloop1+" has implicit sort? "+cbreak.getBreakElement(cloop).hasImplicateSort()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should avoid page break? "+cbreak.getBreakElement(cloop).isAvoidPageBreak());
						out.write(element+" Break with Priority "+cloop1+" should avoid page break? "+cbreak.getBreakElement(cloop).isAvoidPageBreak()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the header of the break be shown in every page? "+cbreak.getBreakElement(cloop).isHeaderOnEveryPage());
						out.write(element+" Break with Priority "+cloop1+" should the header of the break be shown in every page? "+cbreak.getBreakElement(cloop).isHeaderOnEveryPage()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the header of the break be visible? "+cbreak.getBreakElement(cloop).isHeaderVisible());
						out.write(element+" Break with Priority "+cloop1+" should the header of the break be visible? "+cbreak.getBreakElement(cloop).isHeaderVisible()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the footer of the break be visible? "+cbreak.getBreakElement(cloop).isFooterVisible());
						out.write(element+" Break with Priority "+cloop1+" should the footer of the break be visible? "+cbreak.getBreakElement(cloop).isFooterVisible()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should a different break value be placed on a new page? "+cbreak.getBreakElement(cloop).isStartOnNewPage());
						out.write(element+" Break with Priority "+cloop1+" should a different break value be placed on a new page? "+cbreak.getBreakElement(cloop).isStartOnNewPage()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the break value centred across rows or columns? "+cbreak.getBreakElement(cloop).isValueCentered());
						out.write(element+" Break with Priority "+cloop1+" should the break value centred across rows or columns? "+cbreak.getBreakElement(cloop).isValueCentered()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the break value be repeated on each page? "+cbreak.getBreakElement(cloop).isValueOnNewPage());
						out.write(element+" Break with Priority "+cloop1+" should the break value be repeated on each page? "+cbreak.getBreakElement(cloop).isValueOnNewPage()+"\n");
						System.out.println(element+" Break with Priority "+cloop1+" should the break's value be repeated on each row or column? "+cbreak.getBreakElement(cloop).isValueRepeated());
						out.write(element+" Break with Priority "+cloop1+" should the break value be repeated on each row or column? "+cbreak.getBreakElement(cloop).isValueRepeated()+"\n");
					}
				}
				BlockCalculation cvcalc = reportElement.getAxis(TableAxis.CONTENT).getVBlockCalculation();
				BlockCalculation chcalc = reportElement.getAxis(TableAxis.CONTENT).getHBlockCalculation();
				if (cvcalc != null)
				{
					if (cvcalc.getCount()>0)
					{
						System.out.print(element+" Content Vertical Calculation Priorities are:- ");
						out.write(element+" Content Vertical Calculation Priorities are:- ");
						for(int hloop=0;hloop<=cvcalc.getCount()-1;hloop++)
						{
							System.out.print(cvcalc.getCalculationElement(hloop).getExpression()+"("+cvcalc.getCalculationElement(hloop).getType()+") ");
							out.write(cvcalc.getCalculationElement(hloop).getExpression()+"("+cvcalc.getCalculationElement(hloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
				if (chcalc != null)
				{
					if (chcalc.getCount()>0)
					{
						System.out.print(element+" Content Horizontal Calculation Priorities are:- ");
						out.write(element+" Content Horizontal Calculation Priorities are:- ");
						for(int hloop=0;hloop<=chcalc.getCount()-1;hloop++)
						{
							System.out.print(chcalc.getCalculationElement(hloop).getExpression()+"("+chcalc.getCalculationElement(hloop).getType()+") ");
							out.write(chcalc.getCalculationElement(hloop).getExpression()+"("+chcalc.getCalculationElement(hloop).getType()+") ");
						}
						System.out.println("");
						out.write("\n");
					}
				}
			}
			TableFormBase base = (TableFormBase)reportElement.getRepresentation();
			java.awt.Color color = base.getAlternateColor();
			if (color != null)
			{
				System.out.println(element+" Alternate Color:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Alternate Color:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			System.out.println(element + " Alternate Color Frequency:- "+base.getAlternateColorFrequency());
			out.write(element + " Alternate Color Frequency:- "+base.getAlternateColorFrequency()+"\n");
			System.out.println(element + " Cell Spacing:- "+Math.round((base.getCellSpacing()*dpi)/25.4)+" px");
			out.write(element + " Cell Spacing:- "+Math.round((base.getCellSpacing()*dpi)/25.4)+" px\n");
			System.out.println(element+" Vertical Alignment:- "+base.getBodyTableDecoration().getAlignment().getVertical());
			out.write(element+" Vertical Alignment:- "+base.getBodyTableDecoration().getAlignment().getVertical()+"\n");
			System.out.println(element+" Horizal Alignment:- "+base.getBodyTableDecoration().getAlignment().getHorizontal());
			out.write(element+" Horizontal Alignment:- "+base.getBodyTableDecoration().getAlignment().getHorizontal()+"\n");
			System.out.println(element+" Background Vertical Alignment:- "+base.getBodyTableDecoration().getBackgroundAlignment().getVertical());
			out.write(element+" Background Vertical Alignment:- "+base.getBodyTableDecoration().getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Background Horizal Alignment:- "+base.getBodyTableDecoration().getBackgroundAlignment().getHorizontal());
			out.write(element+" Background Horizontal Alignment:- "+base.getBodyTableDecoration().getBackgroundAlignment().getHorizontal()+"\n");
			System.out.println(element+" Font:- "+base.getBodyTableDecoration().getFont().getName());
			out.write(element+" Font:- "+base.getBodyTableDecoration().getFont().getName()+"\n");
			System.out.println(element+" Font Size:- "+base.getBodyTableDecoration().getFont().getSize());
			out.write(element+" Font Size:- "+base.getBodyTableDecoration().getFont().getSize()+"\n");
			System.out.println(element+" Font Style:- "+base.getBodyTableDecoration().getFont().getStyle());
			out.write(element+" Font Style:- "+base.getBodyTableDecoration().getFont().getStyle()+"\n");
			color = base.getBodyTableDecoration().getAttributes().getForeground();
			if (color != null)
			{
				System.out.println(element+" ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = base.getBodyTableDecoration().getAttributes().getBackground();
			if (color != null)
			{
				System.out.println(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			if (base.getBodyTableDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
			{
				System.out.println(element+" Border size:- "+((SimpleBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getSize());
				out.write(element+" Border size:- "+((SimpleBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getSize()+"\n");
			}
			else
			{
				System.out.println(element+" Top Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getTop().getSize());
				out.write(element+" Top Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
				System.out.println(element+" Bottom Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getBottom().getSize());
				out.write(element+" Bottom Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
				System.out.println(element+" Left Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getLeft().getSize());
				out.write(element+" Left Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
				System.out.println(element+" Right Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getRight().getSize());
				out.write(element+" Right Border size:- "+((ComplexBorder)base.getBodyTableDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
			}
				System.out.println(element+" Background Image Display Mode:- "+base.getBodyTableDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
				out.write(element+" Background Image Display Mode:- "+base.getBodyTableDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
			if (base.getBodyTableDecoration().getAttributes().getBackgroundImageURL()!=null)
			{
				System.out.println(element+" Background Image Url:- "+base.getBodyTableDecoration().getAttributes().getBackgroundImageURL().toString());
				out.write(element+" Background Image Url:- "+base.getBodyTableDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
			}
			if (base.getBodyTableDecoration().getAttributes().getSkin()!=null)
			{
				System.out.println(element+" Skin Name:- "+base.getBodyTableDecoration().getAttributes().getSkin().getName());
				out.write(element+" Skin Name:- "+base.getBodyTableDecoration().getAttributes().getSkin().getName()+"\n");
				System.out.println(element+" Skin Url:- "+base.getBodyTableDecoration().getAttributes().getSkin().getUrl());
				out.write(element+" Skin Url:- "+base.getBodyTableDecoration().getAttributes().getSkin().getUrl()+"\n");
			}
			if (reportElement.getRepresentation().getType().toString() != "TableType.FORM")
			{
				Table table = (Table)reportElement.getRepresentation();
				System.out.println(element+" Header Vertical Alignment:- "+table.getHeaderDecoration().getAlignment().getVertical());
				out.write(element+" Header Vertical Alignment:- "+table.getHeaderDecoration().getAlignment().getVertical()+"\n");
				System.out.println(element+" Header Horizal Alignment:- "+table.getHeaderDecoration().getAlignment().getHorizontal());
				out.write(element+" Header Horizontal Alignment:- "+table.getHeaderDecoration().getAlignment().getHorizontal()+"\n");
				System.out.println(element+" Header Background Vertical Alignment:- "+table.getHeaderDecoration().getBackgroundAlignment().getVertical());
				out.write(element+" Header Background Vertical Alignment:- "+table.getHeaderDecoration().getBackgroundAlignment().getVertical()+"\n");
				System.out.println(element+" Header Background Horizal Alignment:- "+table.getHeaderDecoration().getBackgroundAlignment().getHorizontal());
				out.write(element+" Header Background Horizontal Alignment:- "+table.getHeaderDecoration().getBackgroundAlignment().getHorizontal()+"\n");
				System.out.println(element+" Header Font:- "+table.getHeaderDecoration().getFont().getName());
				out.write(element+" Header Font:- "+table.getHeaderDecoration().getFont().getName()+"\n");
				System.out.println(element+" Header Font Size:- "+table.getHeaderDecoration().getFont().getSize());
				out.write(element+" Header Font Size:- "+table.getHeaderDecoration().getFont().getSize()+"\n");
				System.out.println(element+" Header Font Style:- "+table.getHeaderDecoration().getFont().getStyle());
				out.write(element+" Header Font Style:- "+table.getHeaderDecoration().getFont().getStyle()+"\n");
				color = table.getHeaderDecoration().getAttributes().getForeground();
				if (color != null)
				{
					System.out.println(element+" Header ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Header Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				color = table.getHeaderDecoration().getAttributes().getBackground();
				if (color != null)
				{
					System.out.println(element+" Header BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Header Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				if (table.getHeaderDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
				{
					System.out.println(element+" Header Border size:- "+((SimpleBorder)table.getHeaderDecoration().getAttributes().getBorder()).getSize());
					out.write(element+" Header Border size:- "+((SimpleBorder)table.getHeaderDecoration().getAttributes().getBorder()).getSize()+"\n");
				}
				else
				{
					System.out.println(element+" Header Top Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getTop().getSize());
					out.write(element+" Header Top Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
					System.out.println(element+" Header Bottom Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getBottom().getSize());
					out.write(element+" Header Bottom Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
					System.out.println(element+" Header Left Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getLeft().getSize());
					out.write(element+" Header Left Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
					System.out.println(element+" Header Right Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getRight().getSize());
					out.write(element+" Header Right Border size:- "+((ComplexBorder)table.getHeaderDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
				}
					System.out.println(element+" Header Background Image Display Mode:- "+table.getHeaderDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
					out.write(element+" Header Background Image Display Mode:- "+table.getHeaderDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
				if (table.getHeaderDecoration().getAttributes().getBackgroundImageURL()!=null)
				{
					System.out.println(element+" Header Background Image Url:- "+table.getHeaderDecoration().getAttributes().getBackgroundImageURL().toString());
					out.write(element+" Header Background Image Url:- "+table.getHeaderDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
				}
				if (table.getHeaderDecoration().getAttributes().getSkin()!=null)
				{
					System.out.println(element+" Header Skin Name:- "+table.getHeaderDecoration().getAttributes().getSkin().getName());
					out.write(element+" Header Skin Name:- "+table.getHeaderDecoration().getAttributes().getSkin().getName()+"\n");
					System.out.println(element+" Header Skin Url:- "+table.getHeaderDecoration().getAttributes().getSkin().getUrl());
					out.write(element+" Header Skin Url:- "+table.getHeaderDecoration().getAttributes().getSkin().getUrl()+"\n");
				}

				System.out.println(element+" Header Is Visible? "+table.isHeaderVisible());
				out.write(element+" Header Is Visible? "+table.isHeaderVisible()+"\n");
				System.out.println(element+" Should Header be Repeated on Every Page? "+table.isHeaderOnEveryPage());
				out.write(element+" Should Header be Repeated on Every Page? "+table.isHeaderOnEveryPage()+"\n");

				System.out.println(element+" Footer Vertical Alignment:- "+table.getFooterDecoration().getAlignment().getVertical());
				out.write(element+" Footer Vertical Alignment:- "+table.getFooterDecoration().getAlignment().getVertical()+"\n");
				System.out.println(element+" Footer Horizal Alignment:- "+table.getFooterDecoration().getAlignment().getHorizontal());
				out.write(element+" Footer Horizontal Alignment:- "+table.getFooterDecoration().getAlignment().getHorizontal()+"\n");
				System.out.println(element+" Footer Background Vertical Alignment:- "+table.getFooterDecoration().getBackgroundAlignment().getVertical());
				out.write(element+" Footer Background Vertical Alignment:- "+table.getFooterDecoration().getBackgroundAlignment().getVertical()+"\n");
				System.out.println(element+" Footer Background Horizal Alignment:- "+table.getFooterDecoration().getBackgroundAlignment().getHorizontal());
				out.write(element+" Footer Background Horizontal Alignment:- "+table.getFooterDecoration().getBackgroundAlignment().getHorizontal()+"\n");
				System.out.println(element+" Footer Font:- "+table.getFooterDecoration().getFont().getName());
				out.write(element+" Footer Font:- "+table.getFooterDecoration().getFont().getName()+"\n");
				System.out.println(element+" Footer Font Size:- "+table.getFooterDecoration().getFont().getSize());
				out.write(element+" Footer Font Size:- "+table.getFooterDecoration().getFont().getSize()+"\n");
				System.out.println(element+" Footer Font Style:- "+table.getFooterDecoration().getFont().getStyle());
				out.write(element+" Footer Font Style:- "+table.getFooterDecoration().getFont().getStyle()+"\n");
				color = table.getFooterDecoration().getAttributes().getForeground();
				if (color != null)
				{
					System.out.println(element+" Footer ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Footer Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				color = table.getFooterDecoration().getAttributes().getBackground();
				if (color != null)
				{
					System.out.println(element+" Footer BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Footer Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				if (table.getFooterDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
				{
					System.out.println(element+" Footer Border size:- "+((SimpleBorder)table.getFooterDecoration().getAttributes().getBorder()).getSize());
					out.write(element+" Footer Border size:- "+((SimpleBorder)table.getFooterDecoration().getAttributes().getBorder()).getSize()+"\n");
				}
				else
				{
					System.out.println(element+" Footer Top Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getTop().getSize());
					out.write(element+" Footer Top Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
					System.out.println(element+" Footer Bottom Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getBottom().getSize());
					out.write(element+" Footer Bottom Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
					System.out.println(element+" Footer Left Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getLeft().getSize());
					out.write(element+" Footer Left Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
					System.out.println(element+" Footer Right Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getRight().getSize());
					out.write(element+" Footer Right Border size:- "+((ComplexBorder)table.getFooterDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
				}
					System.out.println(element+" Footer Background Image Display Mode:- "+table.getFooterDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
					out.write(element+" Footer Background Image Display Mode:- "+table.getFooterDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
				if (table.getFooterDecoration().getAttributes().getBackgroundImageURL()!=null)
				{
					System.out.println(element+" Footer Background Image Url:- "+table.getFooterDecoration().getAttributes().getBackgroundImageURL().toString());
					out.write(element+" Footer Background Image Url:- "+table.getFooterDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
				}
				if (table.getFooterDecoration().getAttributes().getSkin()!=null)
				{
					System.out.println(element+" Footer Skin Name:- "+table.getFooterDecoration().getAttributes().getSkin().getName());
					out.write(element+" Footer Skin Name:- "+table.getFooterDecoration().getAttributes().getSkin().getName()+"\n");
					System.out.println(element+" Footer Skin Url:- "+table.getFooterDecoration().getAttributes().getSkin().getUrl());
					out.write(element+" Footer Skin Url:- "+table.getFooterDecoration().getAttributes().getSkin().getUrl()+"\n");
				}
				System.out.println(element+" Footer Is Visible? "+table.isFooterVisible());
				out.write(element+" Footer Is Visible? "+table.isFooterVisible()+"\n");
				System.out.println(element+" Should Footer be Repeated on Every Page? "+table.isFooterOnEveryPage());
				out.write(element+" Should Footer be Repeated on Every Page? "+table.isFooterOnEveryPage()+"\n");

				if (reportElement.getRepresentation().getType().toString() == "TableType.HTABLE" || reportElement.getRepresentation().getType().toString() == "TableType.VTABLE")
				{
					SimpleTable sTable = (SimpleTable)table;

					System.out.println(element+" Empty Rows Should be Shown? "+sTable.isShowEmptyRows());
					out.write(element+" Empty Rows Should be Shown? "+sTable.isShowEmptyRows()+"\n");

					int rowCount = sTable.getBody().getRowCount();
					int colCount = sTable.getBody().getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getBody().getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Body Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = sTable.getHeader(null).getRowCount();
					colCount = sTable.getHeader(null).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getHeader(null).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Header Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = sTable.getFooter(null).getRowCount();
					colCount = sTable.getFooter(null).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getFooter(null).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}
				}
				else
				{
					CrossTable cTable = (CrossTable)table;
					System.out.println(element+" Empty rows Should be Shown? "+cTable.isShowEmptyRows());
					out.write(element+" Empty rows Should be Shown? "+cTable.isShowEmptyRows()+"\n");
					System.out.println(element+" Empty Columns Should be Shown? "+cTable.isShowEmptyColumns());
					out.write(element+" Empty Columns Should be Shown? "+cTable.isShowEmptyColumns()+"\n");
					System.out.println(element+" Extra Headers Should be Visible? "+cTable.isExtraHeaderVisible());
					out.write(element+" Extra Headers Should be Visible? "+cTable.isExtraHeaderVisible()+"\n");
					System.out.println(element+" Headers Should be Visible? "+cTable.isHeaderVisible());
					out.write(element+" Headers Should be Visible? "+cTable.isHeaderVisible()+"\n");
					System.out.println(element+" Left Headers Should be Visible? "+cTable.isLeftHeaderVisible());
					out.write(element+" Left Headers Should be Visible? "+cTable.isLeftHeaderVisible()+"\n");
					System.out.println(element+" Top Headers Should be Visible? "+cTable.isTopHeaderVisible());
					out.write(element+" Top Headers Should be Visible? "+cTable.isTopHeaderVisible()+"\n");
					System.out.println(element+" Extra Header Vertical Alignment:- "+cTable.getExtraHeaderDecoration().getAlignment().getVertical());
					out.write(element+" Extra Header Vertical Alignment:- "+cTable.getExtraHeaderDecoration().getAlignment().getVertical()+"\n");
					System.out.println(element+" Extra Header Horizal Alignment:- "+cTable.getExtraHeaderDecoration().getAlignment().getHorizontal());
					out.write(element+" Extra Header Horizontal Alignment:- "+cTable.getExtraHeaderDecoration().getAlignment().getHorizontal()+"\n");
					System.out.println(element+" Extra Header Background Vertical Alignment:- "+cTable.getExtraHeaderDecoration().getBackgroundAlignment().getVertical());
					out.write(element+" Extra Header Background Vertical Alignment:- "+cTable.getExtraHeaderDecoration().getBackgroundAlignment().getVertical()+"\n");
					System.out.println(element+" Extra Header Background Horizal Alignment:- "+cTable.getExtraHeaderDecoration().getBackgroundAlignment().getHorizontal());
					out.write(element+" Extra Header Background Horizontal Alignment:- "+cTable.getExtraHeaderDecoration().getBackgroundAlignment().getHorizontal()+"\n");
					System.out.println(element+" Extra Header Font:- "+cTable.getExtraHeaderDecoration().getFont().getName());
					out.write(element+" Extra Header Font:- "+cTable.getExtraHeaderDecoration().getFont().getName()+"\n");
					System.out.println(element+" Extra Header Font Size:- "+cTable.getExtraHeaderDecoration().getFont().getSize());
					out.write(element+" Extra Header Font Size:- "+cTable.getExtraHeaderDecoration().getFont().getSize()+"\n");
					System.out.println(element+" Extra Header Font Style:- "+cTable.getExtraHeaderDecoration().getFont().getStyle());
					out.write(element+" Extra Header Font Style:- "+cTable.getExtraHeaderDecoration().getFont().getStyle()+"\n");
					color = cTable.getExtraHeaderDecoration().getAttributes().getForeground();
					if (color != null)
					{
						System.out.println(element+" Extra Header ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
						out.write(element+" Extra Header Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
					}
					color = cTable.getExtraHeaderDecoration().getAttributes().getBackground();
					if (color != null)
					{
						System.out.println(element+" Extra Header BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
						out.write(element+" Extra Header Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
					}
					if (cTable.getExtraHeaderDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
					{
						System.out.println(element+" Extra Header Border size:- "+((SimpleBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getSize());
						out.write(element+" Extra Header Border size:- "+((SimpleBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getSize()+"\n");
					}
					else
					{
						System.out.println(element+" Extra Header Top Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getTop().getSize());
						out.write(element+" Extra Header Top Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
						System.out.println(element+" Extra Header Bottom Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getBottom().getSize());
						out.write(element+" Extra Header Bottom Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
						System.out.println(element+" Extra Header Left Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getLeft().getSize());
						out.write(element+" Extra Header Left Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
						System.out.println(element+" Extra Header Right Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getRight().getSize());
						out.write(element+" Extra Header Right Border size:- "+((ComplexBorder)cTable.getExtraHeaderDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
					}
						System.out.println(element+" Extra Header Background Image Display Mode:- "+cTable.getExtraHeaderDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
						out.write(element+" Extra Header Background Image Display Mode:- "+cTable.getExtraHeaderDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
					if (cTable.getExtraHeaderDecoration().getAttributes().getBackgroundImageURL()!=null)
					{
						System.out.println(element+" Extra Header Background Image Url:- "+cTable.getExtraHeaderDecoration().getAttributes().getBackgroundImageURL().toString());
						out.write(element+" Extra Header Background Image Url:- "+cTable.getExtraHeaderDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
					}
					if (cTable.getExtraHeaderDecoration().getAttributes().getSkin()!=null)
					{
						System.out.println(element+" Extra Header Skin Name:- "+cTable.getExtraHeaderDecoration().getAttributes().getSkin().getName());
						out.write(element+" Extra Header Skin Name:- "+cTable.getExtraHeaderDecoration().getAttributes().getSkin().getName()+"\n");
						System.out.println(element+" Extra Header Skin Url:- "+cTable.getExtraHeaderDecoration().getAttributes().getSkin().getUrl());
						out.write(element+" Extra Header Skin Url:- "+cTable.getExtraHeaderDecoration().getAttributes().getSkin().getUrl()+"\n");
					}

					int rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getRowCount();
					int colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Left Header Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Header Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Left Header Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Right Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Right Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Left Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Right Footer Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Body Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
						}
					}

				}
			}
			else
			{
				Form form = (Form)reportElement.getRepresentation();
				System.out.println(element+" Label Vertical Alignment:- "+form.getLabelDecoration().getAlignment().getVertical());
				out.write(element+" Label Vertical Alignment:- "+form.getLabelDecoration().getAlignment().getVertical()+"\n");
				System.out.println(element+" Label Horizal Alignment:- "+form.getLabelDecoration().getAlignment().getHorizontal());
				out.write(element+" Label Horizontal Alignment:- "+form.getLabelDecoration().getAlignment().getHorizontal()+"\n");
				System.out.println(element+" Label Background Vertical Alignment:- "+form.getLabelDecoration().getBackgroundAlignment().getVertical());
				out.write(element+" Label Background Vertical Alignment:- "+form.getLabelDecoration().getBackgroundAlignment().getVertical()+"\n");
				System.out.println(element+" Label Background Horizal Alignment:- "+form.getLabelDecoration().getBackgroundAlignment().getHorizontal());
				out.write(element+" Label Background Horizontal Alignment:- "+form.getLabelDecoration().getBackgroundAlignment().getHorizontal()+"\n");
				System.out.println(element+" Label Font:- "+form.getLabelDecoration().getFont().getName());
				out.write(element+" Label Font:- "+form.getLabelDecoration().getFont().getName()+"\n");
				System.out.println(element+" Label Font Size:- "+form.getLabelDecoration().getFont().getSize());
				out.write(element+" Label Font Size:- "+form.getLabelDecoration().getFont().getSize()+"\n");
				System.out.println(element+" Label Font Style:- "+form.getLabelDecoration().getFont().getStyle());
				out.write(element+" Label Font Style:- "+form.getLabelDecoration().getFont().getStyle()+"\n");
				color = form.getLabelDecoration().getAttributes().getForeground();
				if (color != null)
				{
					System.out.println(element+" Label ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Label Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				color = form.getLabelDecoration().getAttributes().getBackground();
				if (color != null)
				{
					System.out.println(element+" Label BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Label Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				if (form.getLabelDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
				{
					System.out.println(element+" Label Border size:- "+((SimpleBorder)form.getLabelDecoration().getAttributes().getBorder()).getSize());
					out.write(element+" Label Border size:- "+((SimpleBorder)form.getLabelDecoration().getAttributes().getBorder()).getSize()+"\n");
				}
				else
				{
					System.out.println(element+" Label Top Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getTop().getSize());
					out.write(element+" Label Top Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
					System.out.println(element+" Label Bottom Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getBottom().getSize());
					out.write(element+" Label Bottom Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
					System.out.println(element+" Label Left Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getLeft().getSize());
					out.write(element+" Label Left Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
					System.out.println(element+" Label Right Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getRight().getSize());
					out.write(element+" Label Right Border size:- "+((ComplexBorder)form.getLabelDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
				}
					System.out.println(element+" Label Background Image Display Mode:- "+form.getLabelDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
					out.write(element+" Label Background Image Display Mode:- "+form.getLabelDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
				if (form.getLabelDecoration().getAttributes().getBackgroundImageURL()!=null)
				{
					System.out.println(element+" Label Background Image Url:- "+form.getLabelDecoration().getAttributes().getBackgroundImageURL().toString());
					out.write(element+" Label Background Image Url:- "+form.getLabelDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
				}
				if (form.getLabelDecoration().getAttributes().getSkin()!=null)
				{
					System.out.println(element+" Label Skin Name:- "+form.getLabelDecoration().getAttributes().getSkin().getName());
					out.write(element+" Label Skin Name:- "+form.getLabelDecoration().getAttributes().getSkin().getName()+"\n");
					System.out.println(element+" Label Skin Url:- "+form.getLabelDecoration().getAttributes().getSkin().getUrl());
					out.write(element+" Label Skin Url:- "+form.getLabelDecoration().getAttributes().getSkin().getUrl()+"\n");
				}

				System.out.println(element+" Value Vertical Alignment:- "+form.getValueDecoration().getAlignment().getVertical());
				out.write(element+" Value Vertical Alignment:- "+form.getValueDecoration().getAlignment().getVertical()+"\n");
				System.out.println(element+" Value Horizal Alignment:- "+form.getValueDecoration().getAlignment().getHorizontal());
				out.write(element+" Value Horizontal Alignment:- "+form.getValueDecoration().getAlignment().getHorizontal()+"\n");
				System.out.println(element+" Value Background Vertical Alignment:- "+form.getValueDecoration().getBackgroundAlignment().getVertical());
				out.write(element+" Value Background Vertical Alignment:- "+form.getValueDecoration().getBackgroundAlignment().getVertical()+"\n");
				System.out.println(element+" Value Background Horizal Alignment:- "+form.getValueDecoration().getBackgroundAlignment().getHorizontal());
				out.write(element+" Value Background Horizontal Alignment:- "+form.getValueDecoration().getBackgroundAlignment().getHorizontal()+"\n");
				System.out.println(element+" Value Font:- "+form.getValueDecoration().getFont().getName());
				out.write(element+" Value Font:- "+form.getValueDecoration().getFont().getName()+"\n");
				System.out.println(element+" Value Font Size:- "+form.getValueDecoration().getFont().getSize());
				out.write(element+" Value Font Size:- "+form.getValueDecoration().getFont().getSize()+"\n");
				System.out.println(element+" Value Font Style:- "+form.getValueDecoration().getFont().getStyle());
				out.write(element+" Value Font Style:- "+form.getValueDecoration().getFont().getStyle()+"\n");
				color = form.getValueDecoration().getAttributes().getForeground();
				if (color != null)
				{
					System.out.println(element+" Value ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Value Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				color = form.getValueDecoration().getAttributes().getBackground();
				if (color != null)
				{
					System.out.println(element+" Value BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
					out.write(element+" Value Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
				}
				if (form.getValueDecoration().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
				{
					System.out.println(element+" Value Border size:- "+((SimpleBorder)form.getValueDecoration().getAttributes().getBorder()).getSize());
					out.write(element+" Value Border size:- "+((SimpleBorder)form.getValueDecoration().getAttributes().getBorder()).getSize()+"\n");
				}
				else
				{
					System.out.println(element+" Value Top Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getTop().getSize());
					out.write(element+" Value Top Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getTop().getSize()+"\n");
					System.out.println(element+" Value Bottom Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getBottom().getSize());
					out.write(element+" Value Bottom Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getBottom().getSize()+"\n");
					System.out.println(element+" Value Left Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getLeft().getSize());
					out.write(element+" Value Left Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getLeft().getSize()+"\n");
					System.out.println(element+" Value Right Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getRight().getSize());
					out.write(element+" Value Right Border size:- "+((ComplexBorder)form.getValueDecoration().getAttributes().getBorder()).getRight().getSize()+"\n");
				}
					System.out.println(element+" Value Background Image Display Mode:- "+form.getValueDecoration().getAttributes().getBackgroundImageDisplayMode().toString());
					out.write(element+" Value Background Image Display Mode:- "+form.getValueDecoration().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
				if (form.getValueDecoration().getAttributes().getBackgroundImageURL()!=null)
				{
					System.out.println(element+" Value Background Image Url:- "+form.getValueDecoration().getAttributes().getBackgroundImageURL().toString());
					out.write(element+" Value Background Image Url:- "+form.getValueDecoration().getAttributes().getBackgroundImageURL().toString()+"\n");
				}
				if (form.getValueDecoration().getAttributes().getSkin()!=null)
				{
					System.out.println(element+" Value Skin Name:- "+form.getValueDecoration().getAttributes().getSkin().getName());
					out.write(element+" Value Skin Name:- "+form.getValueDecoration().getAttributes().getSkin().getName()+"\n");
					System.out.println(element+" Value Skin Url:- "+form.getValueDecoration().getAttributes().getSkin().getUrl());
					out.write(element+" Value Skin Url:- "+form.getValueDecoration().getAttributes().getSkin().getUrl()+"\n");
				}

				int rowCount = form.getCells().getRowCount();
				int colCount = form.getCells().getColumnCount();
				for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
				{
					for (int colLoop=0;colLoop<=colCount-1;colLoop++)
					{
						TableCell tcell = form.getCells().getCell(rowLoop,colLoop);
						getTableCellProperties(element+" Form Cell",tcell,out,dpi,parentContainer,rowLoop,colLoop,color);
					}
				}
			}
		}
		else
		{
			Graph graph = (Graph)reportElement.getRepresentation();
			System.out.println(element+" Vertical Alignment:- "+graph.getAlignment().getVertical());
			out.write(element+" Vertical Alignment:- "+graph.getAlignment().getVertical()+"\n");
			System.out.println(element+" Horizal Alignment:- "+graph.getAlignment().getHorizontal());
			out.write(element+" Horizontal Alignment:- "+graph.getAlignment().getHorizontal()+"\n");
			System.out.println(element+" Background Vertical Alignment:- "+graph.getBackgroundAlignment().getVertical());
			out.write(element+" Background Vertical Alignment:- "+graph.getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Background Horizal Alignment:- "+graph.getBackgroundAlignment().getHorizontal());
			out.write(element+" Background Horizontal Alignment:- "+graph.getBackgroundAlignment().getHorizontal()+"\n");
			System.out.println(element+" Font:- "+graph.getFont().getName());
			out.write(element+" Font:- "+graph.getFont().getName()+"\n");
			System.out.println(element+" Font Size:- "+graph.getFont().getSize());
			out.write(element+" Font Size:- "+graph.getFont().getSize()+"\n");
			System.out.println(element+" Font Style:- "+graph.getFont().getStyle());
			out.write(element+" Font Style:- "+graph.getFont().getStyle()+"\n");
			java.awt.Color color = graph.getAttributes().getForeground();
			if (color != null)
			{
				System.out.println(element+" ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = graph.getAttributes().getBackground();
			if (color != null)
			{
				System.out.println(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			if (graph.getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
			{
				System.out.println(element+" Border size:- "+((SimpleBorder)graph.getAttributes().getBorder()).getSize());
				out.write(element+" Border size:- "+((SimpleBorder)graph.getAttributes().getBorder()).getSize()+"\n");
			}
			else
			{
				System.out.println(element+" Top Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getTop().getSize());
				out.write(element+" Top Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getTop().getSize()+"\n");
				System.out.println(element+" Bottom Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getBottom().getSize());
				out.write(element+" Bottom Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getBottom().getSize()+"\n");
				System.out.println(element+" Left Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getLeft().getSize());
				out.write(element+" Left Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getLeft().getSize()+"\n");
				System.out.println(element+" Right Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getRight().getSize());
				out.write(element+" Right Border size:- "+((ComplexBorder)graph.getAttributes().getBorder()).getRight().getSize()+"\n");
			}
				System.out.println(element+" Background Image Display Mode:- "+graph.getAttributes().getBackgroundImageDisplayMode().toString());
				out.write(element+" Background Image Display Mode:- "+graph.getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
			if (graph.getAttributes().getBackgroundImageURL()!=null)
			{
				System.out.println(element+" Background Image Url:- "+graph.getAttributes().getBackgroundImageURL().toString());
				out.write(element+" Background Image Url:- "+graph.getAttributes().getBackgroundImageURL().toString()+"\n");
			}
			if (graph.getAttributes().getSkin()!=null)
			{
				System.out.println(element+" Skin Name:- "+graph.getAttributes().getSkin().getName());
				out.write(element+" Skin Name:- "+graph.getAttributes().getSkin().getName()+"\n");
				System.out.println(element+" Skin Url:- "+graph.getAttributes().getSkin().getUrl());
				out.write(element+" Skin Url:- "+graph.getAttributes().getSkin().getUrl()+"\n");
			}
			System.out.println(element+" Height:- "+Math.round((graph.getHeight()*dpi)/25.4)+" px");
			out.write(element+" Height:- "+Math.round((graph.getHeight()*dpi)/25.4)+" px\n");
			System.out.println(element+" Width:- "+Math.round((graph.getWidth()*dpi)/25.4)+" px");
			out.write(element+" Width:- "+Math.round((graph.getWidth()*dpi)/25.4)+" px\n");
			System.out.println(element+" Padding:- "+Math.round((graph.getPadding()*dpi)/25.4)+" px");
			out.write(element+" Padding:- "+Math.round((graph.getPadding()*dpi)/25.4)+" px\n");
			color = graph.getPrimaryDataColor();
			if (color != null)
			{
				System.out.println(element+" Primary Data Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Primary Data Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = graph.getWallColor();
			if (color != null)
			{
				System.out.println(element+" Wall Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Wall Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			System.out.println(element+" is 3D? "+graph.is3D());
			out.write(element+" is 3D? "+graph.is3D()+"\n");
			System.out.println(element+" has a 3D look? "+graph.is3DLook());
			out.write(element+" has a 3D look? "+graph.is3DLook()+"\n");
			System.out.println(element+" Is the Bottom Wall Visible? "+graph.isBottomWallVisible());
			out.write(element+" Is the Bottom Wall Visible? "+graph.isBottomWallVisible()+"\n");
			System.out.println(element+" Is the Left Wall Visible? "+graph.isLeftWallVisible());
			out.write(element+" Is the Left Wall Visible? "+graph.isLeftWallVisible()+"\n");
			System.out.println(element+" Is the Right Wall Visible? "+graph.isRightWallVisible());
			out.write(element+" Is the Right Wall Visible? "+graph.isRightWallVisible()+"\n");
			System.out.println(element+" Should the Graph Data be shown in Percentage? "+graph.isValueInPercentage());
			out.write(element+" Should the Graph Data be shown in Percentage? "+graph.isValueInPercentage()+"\n");
			System.out.println(element+" Graph Title:- "+graph.getTitle().getTitle());
			out.write(element+" Graph Title:- "+graph.getTitle().getTitle()+"\n");
			System.out.println(element+" Is the Graph Title Visible? "+graph.getTitle().isVisible());
			out.write(element+" Is the Graph Title Visible? "+graph.getTitle().isVisible()+"\n");
			System.out.println(element+" Graph Title Vertical Alignment:- "+graph.getTitle().getAlignment().getVertical());
			out.write(element+" Graph Title Vertical Alignment:- "+graph.getTitle().getAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Title Horizal Alignment:- "+graph.getTitle().getAlignment().getHorizontal());
			out.write(element+" Graph Title Horizontal Alignment:- "+graph.getTitle().getAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Title Background Vertical Alignment:- "+graph.getTitle().getBackgroundAlignment().getVertical());
			out.write(element+" Graph Title Background Vertical Alignment:- "+graph.getTitle().getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Title Background Horizal Alignment:- "+graph.getTitle().getBackgroundAlignment().getHorizontal());
			out.write(element+" Graph Title Background Horizontal Alignment:- "+graph.getTitle().getBackgroundAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Title Font:- "+graph.getTitle().getFont().getName());
			out.write(element+" Graph Title Font:- "+graph.getTitle().getFont().getName()+"\n");
			System.out.println(element+" Graph Title Font Size:- "+graph.getTitle().getFont().getSize());
			out.write(element+" Graph Title Font Size:- "+graph.getTitle().getFont().getSize()+"\n");
			System.out.println(element+" Graph Title Font Style:- "+graph.getTitle().getFont().getStyle());
			out.write(element+" Graph Title Font Style:- "+graph.getTitle().getFont().getStyle()+"\n");
			color = graph.getTitle().getAttributes().getForeground();
			if (color != null)
			{
				System.out.println(element+" Graph Title ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Title Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = graph.getTitle().getAttributes().getBackground();
			if (color != null)
			{
				System.out.println(element+" Graph Title BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Title Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			if (graph.getTitle().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
			{
				System.out.println(element+" Graph Title Border size:- "+((SimpleBorder)graph.getTitle().getAttributes().getBorder()).getSize());
				out.write(element+" Graph Title Border size:- "+((SimpleBorder)graph.getTitle().getAttributes().getBorder()).getSize()+"\n");
			}
			else
			{
				System.out.println(element+" Graph Title Top Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getTop().getSize());
				out.write(element+" Graph Title Top Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getTop().getSize()+"\n");
				System.out.println(element+" Graph Title Bottom Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getBottom().getSize());
				out.write(element+" Graph Title Bottom Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getBottom().getSize()+"\n");
				System.out.println(element+" Graph Title Left Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getLeft().getSize());
				out.write(element+" Graph Title Left Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getLeft().getSize()+"\n");
				System.out.println(element+" Graph Title Right Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getRight().getSize());
				out.write(element+" Graph Title Right Border size:- "+((ComplexBorder)graph.getTitle().getAttributes().getBorder()).getRight().getSize()+"\n");
			}
				System.out.println(element+" Graph Title Background Image Display Mode:- "+graph.getTitle().getAttributes().getBackgroundImageDisplayMode().toString());
				out.write(element+" Graph Title Background Image Display Mode:- "+graph.getTitle().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
			if (graph.getTitle().getAttributes().getBackgroundImageURL()!=null)
			{
				System.out.println(element+" Graph Title Background Image Url:- "+graph.getTitle().getAttributes().getBackgroundImageURL().toString());
				out.write(element+" Graph Title Background Image Url:- "+graph.getTitle().getAttributes().getBackgroundImageURL().toString()+"\n");
			}
			if (graph.getTitle().getAttributes().getSkin()!=null)
			{
				System.out.println(element+" Graph Title Skin Name:- "+graph.getTitle().getAttributes().getSkin().getName());
				out.write(element+" Graph Title Skin Name:- "+graph.getTitle().getAttributes().getSkin().getName()+"\n");
				System.out.println(element+" Graph Title Skin Url:- "+graph.getTitle().getAttributes().getSkin().getUrl());
				out.write(element+" Graph Title Skin Url:- "+graph.getTitle().getAttributes().getSkin().getUrl()+"\n");
			}

			System.out.println(element+" Is the Graph Legend Visible? "+graph.getLegend().isVisible());
			out.write(element+" Is the Graph Legend Visible? "+graph.getLegend().isVisible()+"\n");
			System.out.println(element+" Is the Graph Legend Position:- "+graph.getLegend().getPosition());
			out.write(element+" Is the Graph Legend Position:- "+graph.getLegend().getPosition()+"\n");
			System.out.println(element+" Graph Legend Vertical Alignment:- "+graph.getLegend().getAlignment().getVertical());
			out.write(element+" Graph Legend Vertical Alignment:- "+graph.getLegend().getAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Legend Horizal Alignment:- "+graph.getLegend().getAlignment().getHorizontal());
			out.write(element+" Graph Legend Horizontal Alignment:- "+graph.getLegend().getAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Legend Background Vertical Alignment:- "+graph.getLegend().getBackgroundAlignment().getVertical());
			out.write(element+" Graph Legend Background Vertical Alignment:- "+graph.getLegend().getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Legend Background Horizal Alignment:- "+graph.getLegend().getBackgroundAlignment().getHorizontal());
			out.write(element+" Graph Legend Background Horizontal Alignment:- "+graph.getLegend().getBackgroundAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Legend Font:- "+graph.getLegend().getFont().getName());
			out.write(element+" Graph Legend Font:- "+graph.getLegend().getFont().getName()+"\n");
			System.out.println(element+" Graph Legend Font Size:- "+graph.getLegend().getFont().getSize());
			out.write(element+" Graph Legend Font Size:- "+graph.getLegend().getFont().getSize()+"\n");
			System.out.println(element+" Graph Legend Font Style:- "+graph.getLegend().getFont().getStyle());
			out.write(element+" Graph Legend Font Style:- "+graph.getLegend().getFont().getStyle()+"\n");
			color = graph.getLegend().getAttributes().getForeground();
			if (color != null)
			{
				System.out.println(element+" Graph Legend ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Legend Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = graph.getLegend().getAttributes().getBackground();
			if (color != null)
			{
				System.out.println(element+" Graph Legend BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Legend Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			if (graph.getLegend().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
			{
				System.out.println(element+" Graph Legend Border size:- "+((SimpleBorder)graph.getLegend().getAttributes().getBorder()).getSize());
				out.write(element+" Graph Legend Border size:- "+((SimpleBorder)graph.getLegend().getAttributes().getBorder()).getSize()+"\n");
			}
			else
			{
				System.out.println(element+" Graph Legend Top Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getTop().getSize());
				out.write(element+" Graph Legend Top Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getTop().getSize()+"\n");
				System.out.println(element+" Graph Legend Bottom Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getBottom().getSize());
				out.write(element+" Graph Legend Bottom Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getBottom().getSize()+"\n");
				System.out.println(element+" Graph Legend Left Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getLeft().getSize());
				out.write(element+" Graph Legend Left Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getLeft().getSize()+"\n");
				System.out.println(element+" Graph Legend Right Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getRight().getSize());
				out.write(element+" Graph Legend Right Border size:- "+((ComplexBorder)graph.getLegend().getAttributes().getBorder()).getRight().getSize()+"\n");
			}
				System.out.println(element+" Graph Legend Background Image Display Mode:- "+graph.getLegend().getAttributes().getBackgroundImageDisplayMode().toString());
				out.write(element+" Graph Legend Background Image Display Mode:- "+graph.getLegend().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
			if (graph.getLegend().getAttributes().getBackgroundImageURL()!=null)
			{
				System.out.println(element+" Graph Legend Background Image Url:- "+graph.getLegend().getAttributes().getBackgroundImageURL().toString());
				out.write(element+" Graph Legend Background Image Url:- "+graph.getLegend().getAttributes().getBackgroundImageURL().toString()+"\n");
			}
			if (graph.getLegend().getAttributes().getSkin()!=null)
			{
				System.out.println(element+" Graph Legend Skin Name:- "+graph.getLegend().getAttributes().getSkin().getName());
				out.write(element+" Graph Legend Skin Name:- "+graph.getLegend().getAttributes().getSkin().getName()+"\n");
				System.out.println(element+" Graph Legend Skin Url:- "+graph.getLegend().getAttributes().getSkin().getUrl());
				out.write(element+" Graph Legend Skin Url:- "+graph.getLegend().getAttributes().getSkin().getUrl()+"\n");
			}
			System.out.println(element+" Are the Data Labels in the Graph Visible? "+graph.getData().isVisible());
			out.write(element+" Are the Data Labels in the Graph Visible? "+graph.getData().isVisible()+"\n");
			System.out.println(element+" Does the Data representation in the Graph has Shapes? "+graph.getData().isShaped());
			out.write(element+" Does the Data representation in the Graph has Shapes? "+graph.getData().isShaped()+"\n");
			System.out.println(element+" Data Labels Orientation:- "+graph.getData().getOrientation());
			out.write(element+" Data Labels Orientation:- "+graph.getData().getOrientation()+"\n");
			System.out.println(element+" Graph Data Label Vertical Alignment:- "+graph.getData().getAlignment().getVertical());
			out.write(element+" Graph Data Label Vertical Alignment:- "+graph.getData().getAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Data Label Horizal Alignment:- "+graph.getData().getAlignment().getHorizontal());
			out.write(element+" Graph Data Label Horizontal Alignment:- "+graph.getData().getAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Data Label Background Vertical Alignment:- "+graph.getData().getBackgroundAlignment().getVertical());
			out.write(element+" Graph Data Label Background Vertical Alignment:- "+graph.getData().getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Graph Data Label Background Horizal Alignment:- "+graph.getData().getBackgroundAlignment().getHorizontal());
			out.write(element+" Graph Data Label Background Horizontal Alignment:- "+graph.getData().getBackgroundAlignment().getHorizontal()+"\n");
			System.out.println(element+" Graph Data Label Font:- "+graph.getData().getFont().getName());
			out.write(element+" Graph Data Label Font:- "+graph.getData().getFont().getName()+"\n");
			System.out.println(element+" Graph Data Label Font Size:- "+graph.getData().getFont().getSize());
			out.write(element+" Graph Data Label Font Size:- "+graph.getData().getFont().getSize()+"\n");
			System.out.println(element+" Graph Data Label Font Style:- "+graph.getData().getFont().getStyle());
			out.write(element+" Graph Data Label Font Style:- "+graph.getData().getFont().getStyle()+"\n");
			color = graph.getData().getAttributes().getForeground();
			if (color != null)
			{
				System.out.println(element+" Graph Data Label ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Data Label Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			color = graph.getData().getAttributes().getBackground();
			if (color != null)
			{
				System.out.println(element+" Graph Data Label BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
				out.write(element+" Graph Data Label Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
			}
			if (graph.getData().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
			{
				System.out.println(element+" Graph Data Label Border size:- "+((SimpleBorder)graph.getData().getAttributes().getBorder()).getSize());
				out.write(element+" Graph Data Label Border size:- "+((SimpleBorder)graph.getData().getAttributes().getBorder()).getSize()+"\n");
			}
			else
			{
				System.out.println(element+" Graph Data Label Top Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getTop().getSize());
				out.write(element+" Graph Data Label Top Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getTop().getSize()+"\n");
				System.out.println(element+" Graph Data Label Bottom Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getBottom().getSize());
				out.write(element+" Graph Data Label Bottom Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getBottom().getSize()+"\n");
				System.out.println(element+" Graph Data Label Left Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getLeft().getSize());
				out.write(element+" Graph Data Label Left Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getLeft().getSize()+"\n");
				System.out.println(element+" Graph Data Label Right Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getRight().getSize());
				out.write(element+" Graph Data Label Right Border size:- "+((ComplexBorder)graph.getData().getAttributes().getBorder()).getRight().getSize()+"\n");
			}
				System.out.println(element+" Graph Data Label Background Image Display Mode:- "+graph.getData().getAttributes().getBackgroundImageDisplayMode().toString());
				out.write(element+" Graph Data Label Background Image Display Mode:- "+graph.getData().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
			if (graph.getData().getAttributes().getBackgroundImageURL()!=null)
			{
				System.out.println(element+" Graph Data Label Background Image Url:- "+graph.getData().getAttributes().getBackgroundImageURL().toString());
				out.write(element+" Graph Data Label Background Image Url:- "+graph.getData().getAttributes().getBackgroundImageURL().toString()+"\n");
			}
			if (graph.getData().getAttributes().getSkin()!=null)
			{
				System.out.println(element+" Graph Data Label Skin Name:- "+graph.getData().getAttributes().getSkin().getName());
				out.write(element+" Graph Data Label Skin Name:- "+graph.getData().getAttributes().getSkin().getName()+"\n");
				System.out.println(element+" Graph Data Label Skin Url:- "+graph.getData().getAttributes().getSkin().getUrl());
				out.write(element+" Graph Data Label Skin Url:- "+graph.getData().getAttributes().getSkin().getUrl()+"\n");
			}
			GraphAxisProperties axis = graph.getAxis(GraphAxis.X);
			getGraphAxisProperties(element+" X graph Axis",axis,out,dpi,parentContainer,color,GraphAxis.X,reportElement,graph);
			axis = graph.getAxis(GraphAxis.Y);
			getGraphAxisProperties(element+" Y graph Axis",axis,out,dpi,parentContainer,color,GraphAxis.Y,reportElement,graph);
			axis = graph.getAxis(GraphAxis.Z);
			getGraphAxisProperties(element+" Z graph Axis",axis,out,dpi,parentContainer,color,GraphAxis.Z,reportElement,graph);
		}
		if(reportElement.hasFilter())
		{
			System.out.println(element+" has following filters:- "+reportElement.getFilter());
			out.write(element+" has following filters:- "+reportElement.getFilter()+"\n");
		}
	}

	public void getGraphAxisProperties(String element,GraphAxisProperties axis,BufferedWriter out,int dpi,String parentContainer,java.awt.Color color,GraphAxis axisType,ReportBlock reportElement,Graph graph) throws IOException
	{
		System.out.println(element+" has a marker Frequency of:- "+axis.getMarkerFrequency());
		out.write(element+" has a marker Frequency of:- "+axis.getMarkerFrequency()+"\n");
		System.out.println(element+" Grid is Visible? "+axis.getGrid().isVisible());
		out.write(element+" Grid is Visible? "+axis.getGrid().isVisible()+"\n");
		color = axis.getGrid().getColor();
		if (color != null)
		{
			System.out.println(element+" Grid Color:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Grid Color:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		System.out.println(element+" Graph Title:- "+axis.getLabel().getTitle());
		out.write(element+" Graph Title:- "+axis.getLabel().getTitle()+"\n");
		System.out.println(element+" Is the Graph Title Visible? "+axis.getLabel().isVisible());
		out.write(element+" Is the Graph Title Visible? "+axis.getLabel().isVisible()+"\n");
		System.out.println(element+" Graph Title Vertical Alignment:- "+axis.getLabel().getAlignment().getVertical());
		out.write(element+" Graph Title Vertical Alignment:- "+axis.getLabel().getAlignment().getVertical()+"\n");
		System.out.println(element+" Graph Title Horizal Alignment:- "+axis.getLabel().getAlignment().getHorizontal());
		out.write(element+" Graph Title Horizontal Alignment:- "+axis.getLabel().getAlignment().getHorizontal()+"\n");
		System.out.println(element+" Graph Title Background Vertical Alignment:- "+axis.getLabel().getBackgroundAlignment().getVertical());
		out.write(element+" Graph Title Background Vertical Alignment:- "+axis.getLabel().getBackgroundAlignment().getVertical()+"\n");
		System.out.println(element+" Graph Title Background Horizal Alignment:- "+axis.getLabel().getBackgroundAlignment().getHorizontal());
		out.write(element+" Graph Title Background Horizontal Alignment:- "+axis.getLabel().getBackgroundAlignment().getHorizontal()+"\n");
		System.out.println(element+" Graph Title Font:- "+axis.getLabel().getFont().getName());
		out.write(element+" Graph Title Font:- "+axis.getLabel().getFont().getName()+"\n");
		System.out.println(element+" Graph Title Font Size:- "+axis.getLabel().getFont().getSize());
		out.write(element+" Graph Title Font Size:- "+axis.getLabel().getFont().getSize()+"\n");
		System.out.println(element+" Graph Title Font Style:- "+axis.getLabel().getFont().getStyle());
		out.write(element+" Graph Title Font Style:- "+axis.getLabel().getFont().getStyle()+"\n");
		color = axis.getLabel().getAttributes().getForeground();
		if (color != null)
		{
			System.out.println(element+" Graph Title ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Graph Title Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		color = axis.getLabel().getAttributes().getBackground();
		if (color != null)
		{
			System.out.println(element+" Graph Title BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Graph Title Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		if (axis.getLabel().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
		{
			System.out.println(element+" Graph Title Border size:- "+((SimpleBorder)axis.getLabel().getAttributes().getBorder()).getSize());
			out.write(element+" Graph Title Border size:- "+((SimpleBorder)axis.getLabel().getAttributes().getBorder()).getSize()+"\n");
		}
		else
		{
			System.out.println(element+" Graph Title Top Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getTop().getSize());
			out.write(element+" Graph Title Top Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getTop().getSize()+"\n");
			System.out.println(element+" Graph Title Bottom Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getBottom().getSize());
			out.write(element+" Graph Title Bottom Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getBottom().getSize()+"\n");
			System.out.println(element+" Graph Title Left Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getLeft().getSize());
			out.write(element+" Graph Title Left Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getLeft().getSize()+"\n");
			System.out.println(element+" Graph Title Right Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getRight().getSize());
			out.write(element+" Graph Title Right Border size:- "+((ComplexBorder)axis.getLabel().getAttributes().getBorder()).getRight().getSize()+"\n");
		}
			System.out.println(element+" Graph Title Background Image Display Mode:- "+axis.getLabel().getAttributes().getBackgroundImageDisplayMode().toString());
			out.write(element+" Graph Title Background Image Display Mode:- "+axis.getLabel().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
		if (axis.getLabel().getAttributes().getBackgroundImageURL()!=null)
		{
			System.out.println(element+" Graph Title Background Image Url:- "+axis.getLabel().getAttributes().getBackgroundImageURL().toString());
			out.write(element+" Graph Title Background Image Url:- "+axis.getLabel().getAttributes().getBackgroundImageURL().toString()+"\n");
		}
		if (axis.getLabel().getAttributes().getSkin()!=null)
		{
			System.out.println(element+" Graph Title Skin Name:- "+axis.getLabel().getAttributes().getSkin().getName());
			out.write(element+" Graph Title Skin Name:- "+axis.getLabel().getAttributes().getSkin().getName()+"\n");
			System.out.println(element+" Graph Title Skin Url:- "+axis.getLabel().getAttributes().getSkin().getUrl());
			out.write(element+" Graph Title Skin Url:- "+axis.getLabel().getAttributes().getSkin().getUrl()+"\n");
		}
		System.out.println(element+" has a Max. Value of:- "+Math.round((axis.getMaxValueGraph()*dpi)/25.4)+" px");
		out.write(element+" has a Max. Value of:- "+Math.round((axis.getMaxValueGraph()*dpi)/25.4)+" px\n");
		System.out.println(element+" has a Min. Value of:- "+Math.round((axis.getMinValueGraph()*dpi)/25.4)+" px");
		out.write(element+" has a Min. Value of:- "+Math.round((axis.getMinValueGraph()*dpi)/25.4)+" px\n");
		System.out.println(element+" has Max Value? "+axis.hasMaxValue());
		out.write(element+" has Max Value? "+axis.hasMaxValue()+"\n");
		System.out.println(element+" has Min Value? "+axis.hasMinValue());
		out.write(element+" has Min Value? "+axis.hasMinValue()+"\n");
		System.out.println(element+" has Logarithmic Presentation? "+axis.isLogarithmic());
		out.write(element+" has Logarithmic Presentation? "+axis.isLogarithmic()+"\n");
		System.out.println(element+" represents Numeric Values? "+axis.isNumeric());
		out.write(element+" represents Numeric Values? "+axis.isNumeric()+"\n");
		System.out.println(element+" Value frequency:- "+axis.getValues().getFrequency());
		out.write(element+" Value Frequency:- "+axis.getValues().getFrequency()+"\n");
		System.out.println(element+" Value Font Orientation:- "+axis.getValues().getOrientation());
		out.write(element+" Value Font Orientation:- "+axis.getValues().getOrientation()+"\n");
		System.out.println(element+" Vaues Vertical Alignment:- "+axis.getValues().getAlignment().getVertical());
		out.write(element+" Vaues Vertical Alignment:- "+axis.getValues().getAlignment().getVertical()+"\n");
		System.out.println(element+" Vaues Horizal Alignment:- "+axis.getValues().getAlignment().getHorizontal());
		out.write(element+" Vaues Horizontal Alignment:- "+axis.getValues().getAlignment().getHorizontal()+"\n");
		if (axis.getValues().getBackgroundAlignment() != null)
		{
			System.out.println(element+" Vaues Background Vertical Alignment:- "+axis.getValues().getBackgroundAlignment().getVertical());
			out.write(element+" Vaues Background Vertical Alignment:- "+axis.getValues().getBackgroundAlignment().getVertical()+"\n");
			System.out.println(element+" Vaues Background Horizal Alignment:- "+axis.getValues().getBackgroundAlignment().getHorizontal());
			out.write(element+" Vaues Background Horizontal Alignment:- "+axis.getValues().getBackgroundAlignment().getHorizontal()+"\n");
		}
		System.out.println(element+" Vaues Font:- "+axis.getValues().getFont().getName());
		out.write(element+" Vaues Font:- "+axis.getValues().getFont().getName()+"\n");
		System.out.println(element+" Vaues Font Size:- "+axis.getValues().getFont().getSize());
		out.write(element+" Vaues Font Size:- "+axis.getValues().getFont().getSize()+"\n");
		System.out.println(element+" Vaues Font Style:- "+axis.getValues().getFont().getStyle());
		out.write(element+" Vaues Font Style:- "+axis.getValues().getFont().getStyle()+"\n");
		color = axis.getValues().getAttributes().getForeground();
		if (color != null)
		{
			System.out.println(element+" Vaues ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Vaues Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		color = axis.getValues().getAttributes().getBackground();
		if (color != null)
		{
			System.out.println(element+" Vaues BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Vaues Font BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		if (axis.getValues().getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
		{
			System.out.println(element+" Vaues Border size:- "+((SimpleBorder)axis.getValues().getAttributes().getBorder()).getSize());
			out.write(element+" Vaues Border size:- "+((SimpleBorder)axis.getValues().getAttributes().getBorder()).getSize()+"\n");
		}
		else
		{
			System.out.println(element+" Vaues Top Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getTop().getSize());
			out.write(element+" Vaues Top Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getTop().getSize()+"\n");
			System.out.println(element+" Vaues Bottom Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getBottom().getSize());
			out.write(element+" Vaues Bottom Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getBottom().getSize()+"\n");
			System.out.println(element+" Vaues Left Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getLeft().getSize());
			out.write(element+" Vaues Left Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getLeft().getSize()+"\n");
			System.out.println(element+" Vaues Right Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getRight().getSize());
			out.write(element+" Vaues Right Border size:- "+((ComplexBorder)axis.getValues().getAttributes().getBorder()).getRight().getSize()+"\n");
		}
			System.out.println(element+" Vaues Background Image Display Mode:- "+axis.getValues().getAttributes().getBackgroundImageDisplayMode().toString());
			out.write(element+" Vaues Background Image Display Mode:- "+axis.getValues().getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
		if (axis.getValues().getAttributes().getBackgroundImageURL()!=null)
		{
			System.out.println(element+" Vaues Background Image Url:- "+axis.getValues().getAttributes().getBackgroundImageURL().toString());
			out.write(element+" Vaues Background Image Url:- "+axis.getValues().getAttributes().getBackgroundImageURL().toString()+"\n");
		}
		if (axis.getValues().getAttributes().getSkin()!=null)
		{
			System.out.println(element+" Vaues Skin Name:- "+axis.getValues().getAttributes().getSkin().getName());
			out.write(element+" Vaues Skin Name:- "+axis.getValues().getAttributes().getSkin().getName()+"\n");
			System.out.println(element+" Vaues Skin Url:- "+axis.getValues().getAttributes().getSkin().getUrl());
			out.write(element+" Vaues Skin Url:- "+axis.getValues().getAttributes().getSkin().getUrl()+"\n");
		}

		BlockAxis baxis = null;
		if (axisType == GraphAxis.X)
		{
			baxis = reportElement.getAxis(TableAxis.HORIZONTAL);
			if (baxis.getCount()>0)
			{
				for(int axisloop=0;axisloop<=baxis.getCount()-1;axisloop++)
				{
					System.out.println(element+" Expression:- "+baxis.getExpr(axisloop));
					out.write(element+" Expression:- "+baxis.getExpr(axisloop)+"\n");
					if(baxis.getExpr(axisloop).getClass().getName() == "com.businessobjects.wp.om.OMDataSourceVariable")
					{
						VariableExpression variable = (VariableExpression)baxis.getExpr(axisloop);
						System.out.println(element+" Variable Expression:- "+variable.getFormula().getValue());
						out.write(element+" Variable Expression:- "+variable.getFormula().getValue()+"\n");
					}
				}
				if(axis.getFormatNumber(0) != null)
				{
					if (axis.getFormatNumber(0).getType().toString() != "CUSTOM")
					{
						System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample());
						out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample()+"\n");
					}
					else
					{
						System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined());
						out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined()+"\n");
					}
				}
			}
		}
		if (axisType == GraphAxis.Y)
		{
			baxis = reportElement.getAxis(TableAxis.VERTICAL);
			if (baxis.getCount()>0)
			{
				for(int axisloop=0;axisloop<=baxis.getCount()-1;axisloop++)
				{
					System.out.println(element+" Expression:- "+baxis.getExpr(axisloop));
					out.write(element+" Expression:- "+baxis.getExpr(axisloop)+"\n");
					if(baxis.getExpr(axisloop).getClass().getName() == "com.businessobjects.wp.om.OMDataSourceVariable")
					{
						VariableExpression variable = (VariableExpression)baxis.getExpr(axisloop);
						System.out.println(element+" Variable Expression:- "+variable.getFormula().getValue());
						out.write(element+" Variable Expression:- "+variable.getFormula().getValue()+"\n");
					}
				}
				if(axis.getFormatNumber(0) != null)
				{
					if (axis.getFormatNumber(0).getType().toString() != "CUSTOM")
					{
						System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample());
						out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample()+"\n");
					}
					else
					{
						System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined());
						out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined()+"\n");
					}
				}
			}
		}
		if (axisType == GraphAxis.Z)
		{
			baxis = reportElement.getAxis(TableAxis.CONTENT);
			if (baxis.getCount()>0)
			{
				for(int axisloop=0;axisloop<=baxis.getCount()-1;axisloop++)
				{
					System.out.println(element+" Expression:- "+baxis.getExpr(axisloop));
					out.write(element+" Expression:- "+baxis.getExpr(axisloop)+"\n");
					if(baxis.getExpr(axisloop).getClass().getName() == "com.businessobjects.wp.om.OMDataSourceVariable")
					{
						VariableExpression variable = (VariableExpression)baxis.getExpr(axisloop);
						System.out.println(element+" Variable Expression:- "+variable.getFormula().getValue());
						out.write(element+" Variable Expression:- "+variable.getFormula().getValue()+"\n");
					}
				}
				if (graph.is3D())
				{
					try
					{
						if(axis.getFormatNumber(0) != null)
						{
							if (axis.getFormatNumber(0).getType().toString() != "CUSTOM")
							{
								System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample());
								out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" "+axis.getFormatNumber(0).getSample()+"\n");
							}
							else
							{
								System.out.println(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined());
								out.write(element+" Number Format:- "+axis.getFormatNumber(0).getType()+" Positive:"+axis.getFormatNumber(0).getPositive()+" Negative:"+axis.getFormatNumber(0).getNegative()+" Zero:"+axis.getFormatNumber(0).getZero()+" Undefined:"+axis.getFormatNumber(0).getUndefined()+"\n");
							}
						}
					}
					catch(ArrayIndexOutOfBoundsException e)
					{
					}
				}
			}
		}

	}

	public void getTableCellProperties(String element,TableCell tcell,BufferedWriter out,int dpi,String parentContainer,int rowLoop,int colLoop,java.awt.Color color) throws IOException
	{
		System.out.println(element+" Expression at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getExpr());
		out.write(element+" Expression at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getExpr()+"\n");
		if(tcell.getFormatNumber() != null)
		{
			if (tcell.getFormatNumber().getType().toString() != "CUSTOM")
			{
				System.out.println(element+" Number Format at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFormatNumber().getType()+" "+tcell.getFormatNumber().getSample());
				out.write(element+" Number Format at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFormatNumber().getType()+" "+tcell.getFormatNumber().getSample()+"\n");
			}
			else
			{
				System.out.println(element+" Number Format at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFormatNumber().getType()+" Positive:"+tcell.getFormatNumber().getPositive()+" Negative:"+tcell.getFormatNumber().getNegative()+" Zero:"+tcell.getFormatNumber().getZero()+" Undefined:"+tcell.getFormatNumber().getUndefined());
				out.write(element+" Number Format at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFormatNumber().getType()+" "+tcell.getFormatNumber().getSample()+"\n");
			}
		}
		if (tcell.getExpr() != null)
		{
			if(tcell.getExpr().getClass().getName()=="com.businessobjects.wp.om.OMValueExpression")
			{
				if(tcell.getNestedExpr() != null)
				{
					if(tcell.getNestedExpr().getClass().getName() == "com.businessobjects.wp.om.OMDataSourceVariable")
					{
						VariableExpression variable = (VariableExpression)tcell.getNestedExpr();
						System.out.println(element+" Variable Expression at Row "+rowLoop+" and Column "+colLoop+":- "+variable.getFormula().getValue());
						out.write(element+" Variable Expression at Row "+rowLoop+" and Column "+colLoop+":- "+variable.getFormula().getValue()+"\n");
					}
				}
			}
		}
		System.out.println(element+" Content Type at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getContentType());
		out.write(element+" Content Type at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getContentType()+"\n");
		System.out.println(element+" Height at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getHeight()*dpi)/25.4)+" px");
		out.write(element+" Height at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getHeight()*dpi)/25.4)+" px\n");
		System.out.println(element+" Width at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getWidth()*dpi)/25.4)+" px");
		out.write(element+" Width at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getWidth()*dpi)/25.4)+" px\n");
		System.out.println(element+" Horizontal Padding at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getHorizontalPadding()*dpi)/25.4)+" px");
		out.write(element+" Horizontal Padding at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getHorizontalPadding()*dpi)/25.4)+" px\n");
		System.out.println(element+" Vertical Padding at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getVerticalPadding()*dpi)/25.4)+" px");
		out.write(element+" Vertical Padding at Row "+rowLoop+" and Column "+colLoop+":- "+Math.round((tcell.getVerticalPadding()*dpi)/25.4)+" px\n");
		System.out.println(element+" Row Span at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getRowSpan());
		out.write(element+" Row Span at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getRowSpan()+"\n");
		System.out.println(element+" Column Span at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getColSpan());
		out.write(element+" Column Span at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getColSpan()+"\n");
		System.out.println(element+" Autofit Width? at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.isAutoFitWidth());
		out.write(element+" Autofit Width? at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.isAutoFitWidth()+"\n");
		System.out.println(element+" Autofit Height? at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.isAutoFitHeight());
		out.write(element+" Autofit Height? at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.isAutoFitHeight()+"\n");
		System.out.println(element+" Vertical Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAlignment().getVertical());
		out.write(element+" Vertical Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAlignment().getVertical()+"\n");
		System.out.println(element+" Horizal Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAlignment().getHorizontal());
		out.write(element+" Horizontal Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAlignment().getHorizontal()+"\n");
		System.out.println(element+" Background Vertical Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getBackgroundAlignment().getVertical());
		out.write(element+" Background Vertical Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getBackgroundAlignment().getVertical()+"\n");
		System.out.println(element+" Background Horizal Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getBackgroundAlignment().getHorizontal());
		out.write(element+" Background Horizontal Alignment at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getBackgroundAlignment().getHorizontal()+"\n");
		System.out.println(element+" Font at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getName());
		out.write(element+" Font at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getName()+"\n");
		System.out.println(element+" Font Size at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getSize());
		out.write(element+" Font Size at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getSize()+"\n");
		System.out.println(element+" Font Style at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getStyle());
		out.write(element+" Font Style at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getFont().getStyle()+"\n");
		color = tcell.getAttributes().getForeground();
		if (color != null)
		{
			System.out.println(element+" ForeGround Color RGB at Row "+rowLoop+" and Column "+colLoop+":- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Font ForeGround Color RGB at Row "+rowLoop+" and Column "+colLoop+":- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		color = tcell.getAttributes().getBackground();
		if (color != null)
		{
			System.out.println(element+" BackGround Color RGB at Row "+rowLoop+" and Column "+colLoop+":- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" BackGround Color RGB at Row "+rowLoop+" and Column "+colLoop+":- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		if (tcell.getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
		{
			System.out.println(element+" Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((SimpleBorder)tcell.getAttributes().getBorder()).getSize());
			out.write(element+" Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((SimpleBorder)tcell.getAttributes().getBorder()).getSize()+"\n");
		}
		else
		{
			System.out.println(element+" Top Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getTop().getSize());
			out.write(element+" Top Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getTop().getSize()+"\n");
			System.out.println(element+" Bottom Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getBottom().getSize());
			out.write(element+" Bottom Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getBottom().getSize()+"\n");
			System.out.println(element+" Left Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getLeft().getSize());
			out.write(element+" Left Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getLeft().getSize()+"\n");
			System.out.println(element+" Right Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getRight().getSize());
			out.write(element+" Right Border size at Row "+rowLoop+" and Column "+colLoop+":- "+((ComplexBorder)tcell.getAttributes().getBorder()).getRight().getSize()+"\n");
		}
			System.out.println(element+" Background Image Display Mode at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getBackgroundImageDisplayMode().toString());
			out.write(element+" Background Image Display Mode at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
		if (tcell.getAttributes().getBackgroundImageURL()!=null)
		{
			System.out.println(element+" Background Image Url at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getBackgroundImageURL().toString());
			out.write(element+" Background Image Url at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getBackgroundImageURL().toString()+"\n");
		}
		if (tcell.getAttributes().getSkin()!=null)
		{
			System.out.println(element+" Skin Name at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getSkin().getName());
			out.write(element+" Skin Name at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getSkin().getName()+"\n");
			System.out.println(element+" Skin Url at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getSkin().getUrl());
			out.write(element+" Skin Url at Row "+rowLoop+" and Column "+colLoop+":- "+tcell.getAttributes().getSkin().getUrl()+"\n");
		}

		int alertCount = tcell.getAlerters().getCount();
		if (alertCount>0)
		{
			System.out.print(element+" has following alerters at Row "+rowLoop+" and Column "+colLoop+":- ");
			out.write(element+" has following alerters at Row "+rowLoop+" and Column "+colLoop+":- ");
		}
		for (int alertCounter=0;alertCounter<=alertCount-1;alertCounter++)
		{
			Alerter alerter = tcell.getAlerters().getAlerter(alertCounter);
			System.out.print(alerter.getName()+" ");
			out.write(alerter.getName()+" ");
		}
		if (alertCount>0)
		{
			System.out.println("");
			out.write("\n");
		}
	}

	public void getBaseCellProperties(String element,Cell reportElement,BufferedWriter out,int dpi,String parentContainer) throws IOException
	{
		System.out.println(element+" Content Type:- "+reportElement.getContentType());
		out.write(element+" Content Type:- "+reportElement.getContentType()+"\n");
		System.out.println(element+" Height:- "+Math.round((reportElement.getHeight()*dpi)/25.4)+" px");
		out.write(element+" Height:- "+Math.round((reportElement.getHeight()*dpi)/25.4)+" px\n");
		System.out.println(element+" Width:- "+Math.round((reportElement.getWidth()*dpi)/25.4)+" px");
		out.write(element+" Width:- "+Math.round((reportElement.getWidth()*dpi)/25.4)+" px\n");
		System.out.println(element+" Horizontal Padding:- "+Math.round((reportElement.getHorizontalPadding()*dpi)/25.4)+" px");
		out.write(element+" Horizontal Padding:- "+Math.round((reportElement.getHorizontalPadding()*dpi)/25.4)+" px\n");
		System.out.println(element+" Vertical Padding:- "+Math.round((reportElement.getVerticalPadding()*dpi)/25.4)+" px");
		out.write(element+" Vertical Padding:- "+Math.round((reportElement.getVerticalPadding()*dpi)/25.4)+" px\n");
		System.out.println(element+" Autofit Width?:- "+reportElement.isAutoFitWidth());
		out.write(element+" Autofit Width?:- "+reportElement.isAutoFitWidth()+"\n");
		System.out.println(element+" Autofit Height?:- "+reportElement.isAutoFitHeight());
		out.write(element+" Autofit Height?:- "+reportElement.isAutoFitHeight()+"\n");
		System.out.println(element+" Repeat On Every Page?:- "+reportElement.repeatOnEveryPage());
		out.write(element+" Repeat On Every Page?:- "+reportElement.repeatOnEveryPage()+"\n");
		System.out.println(element+" Show When Empty?:- "+reportElement.isShowWhenEmpty());
		out.write(element+" Show When Empty?:- "+reportElement.isShowWhenEmpty()+"\n");
		System.out.println(element+" Vertical Alignment:- "+reportElement.getAlignment().getVertical());
		out.write(element+" Vertical Alignment:- "+reportElement.getAlignment().getVertical()+"\n");
		System.out.println(element+" Horizal Alignment:- "+reportElement.getAlignment().getHorizontal());
		out.write(element+" Horizontal Alignment:- "+reportElement.getAlignment().getHorizontal()+"\n");
		System.out.println(element+" Background Vertical Alignment:- "+reportElement.getBackgroundAlignment().getVertical());
		out.write(element+" Background Vertical Alignment:- "+reportElement.getBackgroundAlignment().getVertical()+"\n");
		System.out.println(element+" Background Horizal Alignment:- "+reportElement.getBackgroundAlignment().getHorizontal());
		out.write(element+" Background Horizontal Alignment:- "+reportElement.getBackgroundAlignment().getHorizontal()+"\n");
		System.out.println(element+" Font:- "+reportElement.getFont().getName());
		out.write(element+" Font:- "+reportElement.getFont().getName()+"\n");
		System.out.println(element+" Font Size:- "+reportElement.getFont().getSize());
		out.write(element+" Font Size:- "+reportElement.getFont().getSize()+"\n");
		System.out.println(element+" Font Style:- "+reportElement.getFont().getStyle());
		out.write(element+" Font Style:- "+reportElement.getFont().getStyle()+"\n");
		java.awt.Color color = reportElement.getAttributes().getForeground();
		if (color != null)
		{
			System.out.println(element+" ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" Font ForeGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		color = reportElement.getAttributes().getBackground();
		if (color != null)
		{
			System.out.println(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue());
			out.write(element+" BackGround Color RGB:- "+color.getRed()+","+color.getGreen()+","+color.getBlue()+"\n");
		}
		if (reportElement.getAttributes().getBorder().getClass().getName().startsWith("com.businessobjects.rebean.wi.SimpleBorder"))
		{
			System.out.println(element+" Border size:- "+((SimpleBorder)reportElement.getAttributes().getBorder()).getSize());
			out.write(element+" Border size:- "+((SimpleBorder)reportElement.getAttributes().getBorder()).getSize()+"\n");
		}
		else
		{
			System.out.println(element+" Top Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getTop().getSize());
			out.write(element+" Top Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getTop().getSize()+"\n");
			System.out.println(element+" Bottom Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getBottom().getSize());
			out.write(element+" Bottom Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getBottom().getSize()+"\n");
			System.out.println(element+" Left Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getLeft().getSize());
			out.write(element+" Left Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getLeft().getSize()+"\n");
			System.out.println(element+" Right Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getRight().getSize());
			out.write(element+" Right Border size:- "+((ComplexBorder)reportElement.getAttributes().getBorder()).getRight().getSize()+"\n");
		}
			System.out.println(element+" Background Image Display Mode:- "+reportElement.getAttributes().getBackgroundImageDisplayMode().toString());
			out.write(element+" Background Image Display Mode:- "+reportElement.getAttributes().getBackgroundImageDisplayMode().toString()+"\n");
		if (reportElement.getAttributes().getBackgroundImageURL()!=null)
		{
			System.out.println(element+" Background Image Url:- "+reportElement.getAttributes().getBackgroundImageURL().toString());
			out.write(element+" Background Image Url:- "+reportElement.getAttributes().getBackgroundImageURL().toString()+"\n");
		}
		if (reportElement.getAttributes().getSkin()!=null)
		{
			System.out.println(element+" Skin Name:- "+reportElement.getAttributes().getSkin().getName());
			out.write(element+" Skin Name:- "+reportElement.getAttributes().getSkin().getName()+"\n");
			System.out.println(element+" Skin Url:- "+reportElement.getAttributes().getSkin().getUrl());
			out.write(element+" Skin Url:- "+reportElement.getAttributes().getSkin().getUrl()+"\n");
		}
		if(reportElement.getHAttachTo()==null)
		{
			System.out.println(element+" Horizontal Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer);
			out.write(element+" Horizontal Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From Left Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From "+reportElement.getHorizontalAnchor()+" Edge Of "+reportElement.getHAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getX()*dpi)/25.4)+" px From "+reportElement.getHorizontalAnchor()+" Edge Of "+reportElement.getHAttachTo()+"\n");

		}
		if(reportElement.getVAttachTo()==null)
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer);
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From Top Edge Of "+parentContainer+"\n");
		}
		else
		{
			System.out.println(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From "+reportElement.getVerticalAnchor()+" Edge Of "+reportElement.getVAttachTo());
			out.write(element+" Vertical Relative Position:- "+Math.round((reportElement.getY()*dpi)/25.4)+" px From "+reportElement.getVerticalAnchor()+" Edge Of "+reportElement.getVAttachTo()+"\n");
		}
		int alertCount = reportElement.getAlerters().getCount();
		if (alertCount>0)
		{
			System.out.print(element+" has following alerters:- ");
			out.write(element+" has following alerters:- ");
		}
		for (int alertCounter=0;alertCounter<=alertCount-1;alertCounter++)
		{
			Alerter alerter = reportElement.getAlerters().getAlerter(alertCounter);
			System.out.print(alerter.getName()+" ");
			out.write(alerter.getName()+" ");
		}
	}

	public void getReportCellProperties(String element,ReportCell reportElement,BufferedWriter out,int dpi,String parentContainer) throws IOException
	{
		System.out.println(element+" Expression:- "+reportElement.getExpr());
		out.write(element+" Expression:- "+reportElement.getExpr()+"\n");
		if(reportElement.getFormatNumber() != null)
		{
			if (reportElement.getFormatNumber().getType().toString() != "CUSTOM")
			{
				System.out.println(element+" Number Format:- "+reportElement.getFormatNumber().getType()+" "+reportElement.getFormatNumber().getSample());
				out.write(element+" Number Format:- "+reportElement.getFormatNumber().getType()+" "+reportElement.getFormatNumber().getSample()+"\n");
			}
			else
			{
				System.out.println(element+" Number Format:- "+reportElement.getFormatNumber().getType()+" Positive:"+reportElement.getFormatNumber().getPositive()+" Negative:"+reportElement.getFormatNumber().getNegative()+" Zero:"+reportElement.getFormatNumber().getZero()+" Undefined:"+reportElement.getFormatNumber().getUndefined());
				out.write(element+" Number Format:- "+reportElement.getFormatNumber().getType()+" Positive:"+reportElement.getFormatNumber().getPositive()+" Negative:"+reportElement.getFormatNumber().getNegative()+" Zero:"+reportElement.getFormatNumber().getZero()+" Undefined:"+reportElement.getFormatNumber().getUndefined()+"\n");
			}
		}
		if(reportElement.getExpr().getClass().getName()=="com.businessobjects.wp.om.OMValueExpression")
		{
			if(reportElement.getNestedExpr().getClass().getName() == "com.businessobjects.wp.om.OMDataSourceVariable")
			{
				VariableExpression variable = (VariableExpression)reportElement.getNestedExpr();
				System.out.println(element+" Variable Expression:- "+variable.getFormula().getValue());
				out.write(element+" Variable Expression:- "+variable.getFormula().getValue()+"\n");
			}
		}
		if(reportElement.hasFilter())
		{
			System.out.println("");
			out.write("\n");
			System.out.println(element+" has following filters:- "+reportElement.getFilter());
			out.write(element+" has following filters:- "+reportElement.getFilter()+"\n");
		}
	}

	public void getFreeCellProperties(String element,FreeCell reportElement,BufferedWriter out,int dpi,String parentContainer) throws IOException
	{
		System.out.println(element+" Expression:- "+reportElement.getValue());
		out.write(element+" Expression:- "+reportElement.getValue()+"\n");
	}
}

