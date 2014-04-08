


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

public class Jdoc
{
	Properties prp = new Properties();
	// Add to the desired logger
	Logger logger;

	public Jdoc(String propertyFileName)
	{
		try
		{
			this.prp.load(new BufferedInputStream(new FileInputStream(propertyFileName)));

		}
		catch(IOException ioEx)
		{

		}
		catch(Exception ex)
		{

		}
	}


	public String readWriteDocProp(boolean set,String obj,String docName)
	{
		String user,pwd,reports,dbID,dbPwd,cmsName;
		String response = null;
		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("Cms");

		try
		{
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

			query = "SELECT SI_PARENTID,SI_ID,SI_NAME FROM CI_INFOOBJECTS WHERE SI_KIND='WEBI' AND  SI_INSTANCE=0 AND SI_NAME IN ('"+docName+"')";

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
					DocumentInstance boDocumentInstance = boReportEngine.openDocument(oInfoObject.getID());
					java.util.Properties props = boDocumentInstance.getProperties ();
					if(!set)
					{
						response = props.getProperty ("MapColor", "Red") + ";";
						response = response + props.getProperty ("Maprotation", "20");
					}
					else
					{
						String[] propVal = obj.split(";");
						props.setProperty ("MapColor", propVal[0]);
						props.setProperty ("Maprotation", propVal[1]);
						boDocumentInstance.setProperties (props);
						boDocumentInstance.applyFormat();
						boDocumentInstance.save();
					}
				}
			}

			oEnterpriseSession.logoff();
		}
		catch(SDKException sdkEx)
		{
			return sdkEx.getMessage();
		}
		catch(Exception ex)
		{
			return ex.getMessage();
		}

		return response;

	}


}

