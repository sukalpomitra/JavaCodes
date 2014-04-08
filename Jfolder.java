


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

public class Jfolder
{
	Properties prp = new Properties();
	private static String logFile = "BOForce.log";
	private final static DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss ");
	public FileHandler handler;
	// Add to the desired logger
	Logger logger;

	public Jfolder(String propertyFileName)
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
		 logger.severe(Jfolder.df.format(new Date()) + " " + ioEx.getMessage());
		 return;
		}
		catch(Exception ex)
		{
			//log error
			//System.out.println(ex.getMessage());
			logger.severe(Jfolder.df.format(new Date()) + " " + ex.getMessage());
			return;
		}
	}
	public boolean DeleteFolder()
	{

		String user,pwd,cmsName;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");

		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
			return false;
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
			return false;
		}

		try
		{//Retrieve the ISessionMgr object to perform the logon
		ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

		//Logon to Enterprise
		IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

		//Retrieve the InfoStore object from the Enterprise Session
		IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
		//System.out.println("Logged In");
		//The query used to retrieve the name

		String query = "";
		IInfoObjects oInfoObjects;


		query = "SELECT SI_ID FROM  CI_INFOOBJECTS WHERE  SI_KIND IN ('Folder', 'FavoritesFolder') AND SI_PARENTID != '4' AND SI_NAME IN ('Intergraph Delivered')" ;

		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);
		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

				//Delete the folder from the InfoObjects collection
				oInfoObjects.delete(oInfoObject);

				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}

		query = "SELECT SI_ID,SI_NAME from CI_APPOBJECTS WHERE SI_NAME IN ('AgencCombo','DateRange','DispatchCombo','GaugeMeasures','Priority_Unit','WidgetConfiguration')" ;

		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);
		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

				//Delete the folder from the InfoObjects collection
				oInfoObjects.delete(oInfoObject);

				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}

		query = "SELECT SI_ID FROM  CI_APPOBJECTS WHERE  SI_KIND IN ('Folder', 'FavoritesFolder') AND SI_PARENTID != '4' AND SI_NAME IN ('SGI Reporting')";

		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);

		if (oInfoObjects.getResultSize()>0)
		{
			IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(0);

			//Delete the folder from the InfoObjects collection
			oInfoObjects.delete(oInfoObject);

			//Commit the changes to the CMS using the commit method. This deletes the folder.
			oInfoStore.commit(oInfoObjects);

		}

		query = "SELECT SI_ID FROM CI_SYSTEMOBJECTS WHERE  SI_KIND = 'UserGroup' AND (SI_NAME LIKE 'LEADS%' OR SI_NAME LIKE 'CAD%')";

		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);

		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

				//Delete the folder from the InfoObjects collection
				oInfoObjects.delete(oInfoObject);

				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}

	}
    catch(SDKException sdkEx)
      {
		//System.out.println(sdkEx.getMessage());
		logger.severe(Jfolder.df.format(new Date()) + " " + sdkEx.getMessage());
		return false;

      }

		return true;
	}

	public boolean DeleteBOSamples()
		{

			String user,pwd,cmsName;

			user = prp.getProperty("UserName");
			pwd = prp.getProperty("Password");
			cmsName = prp.getProperty("CMSName");

			if( user == null)
			{
				//System.out.println("UserName can not be null. Please check biar.properties");
				logger.severe(Jfolder.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
				return false;
			}

			if( cmsName == null)
			{
				//System.out.println("CMSName can not be null. Please check biar.properties");
				logger.severe(Jfolder.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
				return false;
			}

			try
			{//Retrieve the ISessionMgr object to perform the logon
			ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

			//Logon to Enterprise
			IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

			//Retrieve the InfoStore object from the Enterprise Session
			IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
			//System.out.println("Logged In");
			//The query used to retrieve the name

			String query = "";
			IInfoObjects oInfoObjects;

			query = "SELECT SI_ID FROM  CI_APPOBJECTS WHERE  SI_KIND IN ('Universe') AND SI_NAME IN ('eFashion','Island Resorts Marketing','Report Conversion Tool Audit Universe')";

			//Execite the query and retrieve the folder name
			oInfoObjects = oInfoStore.query(query);

			if (oInfoObjects.getResultSize()>0)
			{
				int loopMax = oInfoObjects.getResultSize();
				for(int i = 0;i<=loopMax-1;i++)
				{
					IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

					//Delete the folder from the InfoObjects collection
					oInfoObjects.delete(oInfoObject);

					//Commit the changes to the CMS using the commit method. This deletes the folder.
					oInfoStore.commit(oInfoObjects);

				}

			}

		}
	    catch(SDKException sdkEx)
	      {
			//System.out.println(sdkEx.getMessage());
			logger.severe(Jfolder.df.format(new Date()) + " " + sdkEx.getMessage());
			return false;

	      }

			return true;
	}

	public boolean DeleteCADUserGroups()
	{

		String user,pwd,cmsName,url;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");
		url = prp.getProperty("WSDLURL");


		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
			return false;
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
			return false;
		}

		try
		{//Retrieve the ISessionMgr object to perform the logon
		ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

		//Logon to Enterprise
		IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

		//Retrieve the InfoStore object from the Enterprise Session
		IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
		//System.out.println("Logged In");
		//The query used to retrieve the name

		String query = "";
		IInfoObjects oInfoObjects;

		query = "SELECT SI_ID FROM CI_SYSTEMOBJECTS WHERE  SI_KIND = 'UserGroup' AND SI_NAME LIKE 'CAD%'";

				//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);

		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

				//Delete the folder from the InfoObjects collection
				oInfoObjects.delete(oInfoObject);

				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}
	}
	catch(SDKException sdkEx)
	  {
		//System.out.println(sdkEx.getMessage());
		logger.severe(Jfolder.df.format(new Date()) + " " + sdkEx.getMessage());
		return false;

	  }

		return true;
	}

	public boolean DeleteLEADSUserGroups()
	{

		String user,pwd,cmsName,url;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");
		url = prp.getProperty("WSDLURL");


		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
			return false;
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
			return false;
		}

		try
		{//Retrieve the ISessionMgr object to perform the logon
		ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

		//Logon to Enterprise
		IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

		//Retrieve the InfoStore object from the Enterprise Session
		IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
		//System.out.println("Logged In");
		//The query used to retrieve the name

		String query = "";
		IInfoObjects oInfoObjects;

		query = "SELECT SI_ID FROM CI_SYSTEMOBJECTS WHERE  SI_KIND = 'UserGroup' AND SI_NAME LIKE 'LEADS%'";

				//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);

		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);

				//Delete the folder from the InfoObjects collection
				oInfoObjects.delete(oInfoObject);

				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}
	}
	catch(SDKException sdkEx)
	  {
		//System.out.println(sdkEx.getMessage());
		logger.severe(Jfolder.df.format(new Date()) + " " + sdkEx.getMessage());
		return false;

	  }

		return true;
	}


	public boolean UpdateQAAWS()
	{

		String user,pwd,cmsName,url;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");
		url = prp.getProperty("WSDLURL");


		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
			return false;
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jfolder.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
			return false;
		}

		try
		{//Retrieve the ISessionMgr object to perform the logon
		ISessionMgr oSessionMgr = CrystalEnterprise.getSessionMgr();

		//Logon to Enterprise
		IEnterpriseSession oEnterpriseSession = oSessionMgr.logon(user,pwd,cmsName,"secEnterprise");

		//Retrieve the InfoStore object from the Enterprise Session
		IInfoStore oInfoStore = (IInfoStore)oEnterpriseSession.getService("", "InfoStore");
		//System.out.println("Logged In");
		//The query used to retrieve the name

		String query = "";
		IInfoObjects oInfoObjects;

		query = "SELECT SI_WSDL_URL,SI_SERVICE_URL from CI_APPOBJECTS WHERE SI_NAME IN ('AgencCombo','DateRange','DispatchCombo','GaugeMeasures','Priority_Unit','WidgetConfiguration')" ;

		//Execite the query and retrieve the folder name
		oInfoObjects = oInfoStore.query(query);
		if (oInfoObjects.getResultSize()>0)
		{
			int loopMax = oInfoObjects.getResultSize();
			for(int i = 0;i<=loopMax-1;i++)
			{
				String wsdl = ((IInfoObject)oInfoObjects.get(i)).properties().getProperty("SI_WSDL_URL").toString();
				String service = ((IInfoObject)oInfoObjects.get(i)).properties().getProperty("SI_SERVICE_URL").toString();
				int index = wsdl.lastIndexOf("dswsbobje");
				int indexService = service.lastIndexOf("dswsbobje");
				String wsdlnew = url + wsdl.substring(index-1);
				String servicenew = url + service.substring(indexService-1);
				((IInfoObject)oInfoObjects.get(i)).properties().setProperty("SI_WSDL_URL",wsdlnew);
				((IInfoObject)oInfoObjects.get(i)).properties().setProperty("SI_SERVICE_URL",servicenew);
				//Commit the changes to the CMS using the commit method. This deletes the folder.
				oInfoStore.commit(oInfoObjects);

			}

		}
	}
	catch(SDKException sdkEx)
	  {
		//System.out.println(sdkEx.getMessage());
		logger.severe(Jfolder.df.format(new Date()) + " " + sdkEx.getMessage());
		return false;

	  }

		return true;
	}
}

