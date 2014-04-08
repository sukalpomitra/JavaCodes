


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
import com.businessobjects.sdk.plugin.desktop.overload.*;
import com.businessobjects.sdk.plugin.desktop.universe.*;
import com.crystaldecisions.sdk.exception.*;
import com.crystaldecisions.sdk.properties.*;
import com.crystaldecisions.sdk.plugin.desktop.usergroup.*;
import com.crystaldecisions.sdk.plugin.desktop.user.*;
//import com.businessobjects.rebean.wi.*;

public class Jsecurity
{
	Properties prp = new Properties();
	private static String logFile = "BOForce.log";
	private final static DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss ");
	public FileHandler handler;
	// Add to the desired logger
	Logger logger;

	public Jsecurity(String propertyFileName)
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
		 logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + ioEx.getMessage());
		 return;
		}
		catch(Exception ex)
		{
			//log error
			//System.out.println(ex.getMessage());
			logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + ex.getMessage());
			return;
		}
	}
	public String getUniverseSecurityRestrictions()
	{

		String user,pwd,cmsName,reports,dbID,dbPwd;

		user = prp.getProperty("UserName");
		pwd = prp.getProperty("Password");
		cmsName = prp.getProperty("CMSName");
		dbID = prp.getProperty("DatabaseID");
		dbPwd = prp.getProperty("DatabasePwd");
		Boolean doCommit = false;

		if( user == null)
		{
			//System.out.println("UserName can not be null. Please check biar.properties");
			logger.severe(Jsecurity.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.");
			//return false;
			return Jsecurity.df.format(new java.util.Date()) + " UserName can not be null. Please check biar.properties.";
		}

		if( cmsName == null)
		{
			//System.out.println("CMSName can not be null. Please check biar.properties");
			logger.severe(Jsecurity.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.");
			//return false;
			return Jsecurity.df.format(new java.util.Date()) + " CMSName can not be null. Please check biar.properties.";
		}
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
			IInfoObjects oInfoObjects,oSecurityObjects;

			//Initialize SQL Connections
			//Class.forName("net.sourceforge.jtds.jdbc.Driver");
			//Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://bidev1:1433/tempdb",dbID,dbPwd);
			//Boolean commitMode = conn.getAutoCommit();
			//conn.setAutoCommit(false);

			query = "SELECT SI_ID,SI_USERGROUPS_ORDER FROM CI_APPOBJECTS WHERE SI_NAME IN ('Intergraph CAD','Intergraph LEADS')";

			//Execite the query and retrieve the folder name
			oInfoObjects = oInfoStore.query(query);
			if (oInfoObjects.getResultSize()>0)
			{
				//CMSINFO
				//CallableStatement stmt = (CallableStatement)conn.prepareCall("{call ReportsMetadata.dbo.sp_getCMSID(?,?)}");
				//stmt.setString(1,cmsName);
				//stmt.registerOutParameter(2,java.sql.Types.BIGINT);
				//stmt.execute();
				//int cmsId = stmt.getInt(2);

				int objectCount = oInfoObjects.getResultSize();
				BufferedWriter out = new BufferedWriter(new FileWriter("rowlevel.txt"));
				BufferedWriter out1 = new BufferedWriter(new FileWriter("objectlevel.txt"));
				BufferedWriter out2 = new BufferedWriter(new FileWriter("mapping.txt"));
				for(int n=0;n<=objectCount-1;n++)
				{
					IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(n);
					query = "SELECT * FROM CI_APPOBJECTS WHERE SI_KIND = 'Overload' AND SI_PARENTID = " + oInfoObject.getID();
					oSecurityObjects = oInfoStore.query(query);
					int oSecurityCount = oSecurityObjects.getResultSize();
					for(int i=0;i<=oSecurityCount-1;i++)
					{
						IInfoObject oSecurityObject = (IInfoObject)oSecurityObjects.get(i);
						IRowsOverload overloadRows = ((IOverloadBase)oSecurityObject).getRestrictedRows();
						IObjectsOverload overloadObjects = ((IOverloadBase)oSecurityObject).getRestrictedObjects();
						int restrictedRowsCount = overloadRows.size();
						for(int j=0;j<=restrictedRowsCount-1;j++)
						{
							IRowOverload rowOverload = (IRowOverload)overloadRows.get(j);
							System.out.println(oInfoObject.getTitle()+":::"+oSecurityObject.getTitle()+":::"+rowOverload.getRestrictedTableName()+":::"+rowOverload.getWhereClause());
							out.write(oInfoObject.getTitle()+","+oSecurityObject.getTitle()+","+rowOverload.getRestrictedTableName()+"\n");
						}
						int restrictedObjectsCount = overloadObjects.size();
						for(int k=0;k<=restrictedObjectsCount-1;k++)
						{
							IObjectOverload objectOverload = (IObjectOverload)overloadObjects.get(k);
							System.out.println(oInfoObject.getTitle()+":::"+oSecurityObject.getTitle()+":::"+objectOverload.getObjectName());
							out1.write(oInfoObject.getTitle()+","+oSecurityObject.getTitle()+","+objectOverload.getObjectName()+"\n");
						}
					}
					Set userGroupOrderedID = ((IUniverseBase)oInfoObject).getUserGroupOrderedIDs();
					Iterator groupIter = userGroupOrderedID.iterator();
					while (groupIter.hasNext())
					{
						int id = Integer.parseInt(groupIter.next().toString());
						query = "Select SI_NAME from CI_SYSTEMOBJECTS where SI_KIND = 'UserGroup' AND SI_ID = "+id;
						System.out.println("Group "+((IInfoObject)oInfoStore.query(query).get(0)).getTitle()+" is mapped with "+((IUniverseBase)oInfoObject).getAggregatedOverload(id).getTitle());
						out2.write(oInfoObject.getTitle()+","+((IUniverseBase)oInfoObject).getAggregatedOverload(id).getTitle()+","+((IInfoObject)oInfoStore.query(query).get(0)).getTitle()+"\n");
					}
				}
				if (out != null)
				{
					out.close();
				}
				if (out1 != null)
				{
					out1.close();
				}
				if (out2 != null)
				{
					out2.close();
				}
			}
			else
			{
				//System.out.println("No reports matched your criteria");
				return "Pre-Packaged Universes Not Found";
			}

			oEnterpriseSession.logoff();

			//if (doCommit)
			//{
				//conn.commit();
			//}
			//else
			//{
				//conn.rollback();
			//}
			//conn.setAutoCommit(commitMode);
			//if (!conn.isClosed())
			//{
				//conn.close();
			//}
		}
		catch(SDKException sdkEx)
		{
			//System.out.println(sdkEx.getMessage());
			logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + sdkEx.getMessage());
			return Jsecurity.df.format(new java.util.Date()) + " " + sdkEx.getMessage();
			//return false;

		}
		catch(IOException ioex)
		{
			logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + ioex.getMessage());
			return Jsecurity.df.format(new java.util.Date()) + " " + ioex.getMessage();
		}
		//catch(SQLException sqlEx)
		//{
			//logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + sqlEx.getMessage());
			//return Jsecurity.df.format(new java.util.Date()) + " " + sqlEx.getMessage();
			//return false;
		//}
		//catch(ClassNotFoundException cEx)
		//{
			//logger.severe(Jsecurity.df.format(new java.util.Date()) + " " + cEx.getMessage());
			//return Jsecurity.df.format(new java.util.Date()) + " " + cEx.getMessage();
			//return false;
		//}

		return "STATUS:OK";
	}

	public static boolean checkPrincipal(IObjectPrincipal principal, Iterator<IObjectPrincipal> iterPrincipal){

	        while(iterPrincipal.hasNext()){

	              IObjectPrincipal univprincipal = iterPrincipal.next();
	              if(univprincipal.getName().equals(principal.getName())){
	                   return false;
	              }
	          }
	     return true;
    }
}

