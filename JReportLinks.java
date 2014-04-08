


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


	public String getReportData(String username,String password,String cms,String reportColl,String servername)
	{
		String user,pwd,cmsName,reports,dbID,dbPwd;

		if (username.compareTo("")==0)
		{
			user = prp.getProperty("UserName");
		}
		else
		{
			user = username;
		}
		if (password.compareTo("")==0)
		{
			pwd = prp.getProperty("Password");
		}
		else
		{
			pwd =password;
		}
		if (cms.compareTo("")==0)
		{
			cmsName = prp.getProperty("CMSName");
		}
		else
		{
			cmsName = cms;
		}
		if (reportColl.compareTo("")==0)
		{
			reports = prp.getProperty("ArtifactName");
		}
		else
		{
			reports = reportColl;
		}
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
					DocumentInstance boDocumentInstance = boReportEngine.openDocument(oInfoObject.getID());
					ReportDictionary dict = boDocumentInstance.getDictionary();
					int reportCount = boDocumentInstance.getStructure().getReportElementCount();
					int dpi = boDocumentInstance.getMediaDPI();
					for(int i=0;i<=reportCount-1;i++)
					{
						ReportContainer reportContainer = (ReportContainer)boDocumentInstance.getStructure().getReportElement(i);
						int bodyCount = reportContainer.getReportBody().getReportElementCount();
						for (int bodyLoopCounter=0;bodyLoopCounter<=bodyCount-1;bodyLoopCounter++)
						{
							if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
							{
								ReportBlock block = (ReportBlock)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getBaseBlockProperties("Report Block",block,dpi,"Report Body",servername,dict);
							}
							else if(reportContainer.getReportBody().getReportElement(bodyLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
							{
								SectionContainer scontainer = (SectionContainer)reportContainer.getReportBody().getReportElement(bodyLoopCounter);
								getSectionContainerProperties("Section Container",scontainer,dpi,"Report Body",servername,dict);
							}
						}
					}
					boDocumentInstance.applyFormat();
					boDocumentInstance.save();
				}
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

	public void getBaseBlockProperties(String element,ReportBlock reportElement,int dpi,String parentContainer,String servername,ReportDictionary dict) throws IOException
	{

		if (reportElement.getRepresentation().toString().startsWith("com.businessobjects.rebean.wi.Graph") == false)
		{
			if (reportElement.getRepresentation().getType().toString() != "TableType.FORM")
			{
				Table table = (Table)reportElement.getRepresentation();

				if (reportElement.getRepresentation().getType().toString() == "TableType.HTABLE" || reportElement.getRepresentation().getType().toString() == "TableType.VTABLE")
				{
					SimpleTable sTable = (SimpleTable)table;

					int rowCount = sTable.getBody().getRowCount();
					int colCount = sTable.getBody().getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getBody().getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Body Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = sTable.getHeader(null).getRowCount();
					colCount = sTable.getHeader(null).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getHeader(null).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Header Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = sTable.getFooter(null).getRowCount();
					colCount = sTable.getFooter(null).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = sTable.getFooter(null).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}
				}
				else
				{
					CrossTable cTable = (CrossTable)table;
					int rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getRowCount();
					int colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Left Header Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Header Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Left Header Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.TOP,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Top Right Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Right Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.LEFT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Left Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BOTTOM,HZoneType.RIGHT).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Bottom Right Footer Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

					rowCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getRowCount();
					colCount = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getColumnCount();

					for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
					{
						for (int colLoop=0;colLoop<=colCount-1;colLoop++)
						{
							TableCell tcell = cTable.getCellMatrix(VZoneType.BODY,HZoneType.BODY).getCell(rowLoop,colLoop);
							getTableCellProperties(element+" Body Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
						}
					}

				}
			}
			else
			{
				Form form = (Form)reportElement.getRepresentation();

				int rowCount = form.getCells().getRowCount();
				int colCount = form.getCells().getColumnCount();
				for (int rowLoop=0;rowLoop<=rowCount-1;rowLoop++)
				{
					for (int colLoop=0;colLoop<=colCount-1;colLoop++)
					{
						TableCell tcell = form.getCells().getCell(rowLoop,colLoop);
						getTableCellProperties(element+" Form Cell",tcell,dpi,parentContainer,rowLoop,colLoop,servername,dict);
					}
				}
			}
		}
	}

	public void getSectionContainerProperties(String element,SectionContainer scontainer,int dpi,String parentContainer,String servername,ReportDictionary dict) throws IOException
	{
		int sectionCount = scontainer.getReportElementCount();

		for (int sectionLoopCounter=0;sectionLoopCounter<=sectionCount-1;sectionLoopCounter++)
		{
			if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportBlock")
			{
				ReportBlock block = (ReportBlock)scontainer.getReportElement(sectionLoopCounter);
				getBaseBlockProperties("Report Block",block,dpi,element,servername,dict);
			}
			else if(scontainer.getReportElement(sectionLoopCounter).getClass().getName()=="com.businessobjects.wp.om.OMReportSection")
			{
				SectionContainer sinnercontainer = (SectionContainer)scontainer.getReportElement(sectionLoopCounter);
				getSectionContainerProperties("Section Container",sinnercontainer,dpi,element,servername,dict);
			}
		}
	}


	public void getTableCellProperties(String element,TableCell tcell,int dpi,String parentContainer,int rowLoop,int colLoop, String servername,ReportDictionary dict) throws IOException
	{
		if(tcell.getText().startsWith("=\"<ahref=http://"))
		{
			String href = tcell.getText();
			href = href.substring(href.indexOf("/OpenDocument"));
			href = "=\"<ahref=http://"+servername+href;
			tcell.setExpr(dict.createFormula(href));
		}
	}
}

