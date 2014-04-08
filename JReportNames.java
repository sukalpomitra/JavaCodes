


import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.logging.*;


import com.crystaldecisions.sdk.occa.infostore.*;
import com.crystaldecisions.sdk.occa.security.*;
import com.crystaldecisions.sdk.occa.security.internal.*;
import com.crystaldecisions.sdk.framework.*;
import com.crystaldecisions.sdk.framework.internal.*;
import com.crystaldecisions.sdk.exception.*;
import com.crystaldecisions.sdk.properties.*;

public class Jbiar
{
	Properties prp = new Properties();
	private static String logFile = "BOForce.log";
	private final static DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss ");
	public FileHandler handler;
	public String[] reportNames;
	// Add to the desired logger
	Logger logger;

	public Jbiar(String propertyFileName)
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
		 logger.severe(Jbiar.df.format(new Date()) + " " + ioEx.getMessage());
		 return;
		}
		catch(Exception ex)
		{
			//log error
			//System.out.println(ex.getMessage());
			logger.severe(Jbiar.df.format(new Date()) + " " + ex.getMessage());
			return;
		}
	}


	public String[] getreportNames()
	{

		String user,pwd,cmsName;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");

		IEnterpriseSession oEnterpriseSession;
		//ExportBiarFile.append(biarfile);
		try
		{//Retrieve the ISessionMgr object to perform the logon
		ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

		//Logon to Enterprise
		oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

		//Retrieve the InfoStore object from the Enterprise Session
		IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
		//System.out.println("Logged In");
		//The query used to retrieve the name

		String query = "";
		IInfoObjects oInfoObjects,folderInfoObjects,reportInfoObjects;

		query = "select SI_ID from CI_INFOOBJECTS where SI_NAME = 'Intergraph Delivered'" ;
		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);
		if (oInfoObjects.getResultSize()>0)
		{
			IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(0);
			query = "Select SI_NAME,SI_ID from CI_INFOOBJECTS Where SI_PARENT_FOLDER = " + oInfoObject.getID();
			folderInfoObjects = oInfoStore.query(query);
			if (folderInfoObjects.getResultSize()>0)
			{

				IInfoObject folderInfoObject = (IInfoObject)folderInfoObjects.get(0);
				String filter = folderInfoObject.getID()+",";

				folderInfoObject = (IInfoObject)folderInfoObjects.get(1);
				query = "Select SI_NAME,SI_PARENT_FOLDER from CI_INFOOBJECTS Where SI_PARENT_FOLDER IN (" + filter + folderInfoObject.getID()+")";
				reportInfoObjects = oInfoStore.query(query);
				if (reportInfoObjects.getResultSize()>0)
				{
					reportNames = new String[reportInfoObjects.getResultSize()];
					for (int i=0;i<=reportInfoObjects.getResultSize()-1;i++)
					{
						IInfoObject reportInfoObject = (IInfoObject)reportInfoObjects.get(i);
						if (folderInfoObject.getID() == reportInfoObject.getParentID())
						{
							reportNames[i] = folderInfoObject.getTitle() + ";" + reportInfoObject.getTitle();
						}
						else
						{
							folderInfoObject = (IInfoObject)folderInfoObjects.get(0);
							reportNames[i] = folderInfoObject.getTitle() + ";" + reportInfoObject.getTitle();
						}
						folderInfoObject = (IInfoObject)folderInfoObjects.get(1);
					}
				}
			}
			oEnterpriseSession.logoff();
			return reportNames;
		}
		else
		{
			//System.out.println("No Objects to Export");
			logger.info(Jbiar.df.format(new Date()) + " No Objects to Export.");
			oEnterpriseSession.logoff();
			return new String[0];
		}


		//System.out.println("Exported");
	}
	catch(SDKException sdkEx)
	  {
		//System.out.println(sdkEx.getMessage());
		logger.severe(Jbiar.df.format(new Date()) + " " + sdkEx.getMessage());
		return new String[0];
	  }
	}
}

