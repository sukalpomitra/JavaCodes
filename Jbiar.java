


import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.util.logging.*;


import com.businessobjects.sdk.biar.*;
import com.businessobjects.sdk.biar.om.*;
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
	private final static DateFormat df = new SimpleDateFormat ("yyyy.MM.dd  hh:mm:ss ");
	private final static DateFormat dfb = new SimpleDateFormat ("yyyy.MM.dd-hh:mm:ss");
	public FileHandler handler;
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

	public boolean BIARExport()
			{

				String user,pwd,cmsName, biarfile, artifactName, artifactType, fileloc;

				user = prp.getProperty("UserName");
				pwd = prp.getProperty("Password");
				cmsName = prp.getProperty("CMSName");
				biarfile = prp.getProperty("BiarFileName");
				artifactName = prp.getProperty("ArtifactName");
				artifactType = prp.getProperty("ArtifactType");
				fileloc = prp.getProperty("BiarFileLocation");

				if( user == null)
				{
					//System.out.println("UserName can not be null. Please check biar.properties");
					logger.severe(Jbiar.df.format(new Date()) + " UserName can not be null. Please check biar.properties.");
					return false;
				}

				if( cmsName == null)
				{
					//System.out.println("CMSName can not be null. Please check biar.properties");
					logger.severe(Jbiar.df.format(new Date()) + " CMSName can not be null. Please check biar.properties.");
					return false;
				}

				if( biarfile == null)
				{
					//System.out.println("BIAR file can not be null. Please check biar.properties");
					logger.severe(Jbiar.df.format(new Date()) + " BIAR file can not be null. Please check biar.properties.");
					return false;
				}
				if( artifactName == null)
				{
					//System.out.println("ArtifactName can not be null. Please check biar.properties");
					logger.severe(Jbiar.df.format(new Date()) + " ArtifactName can not be null. Please check biar.properties.");
					return false;
				}
				if( artifactType == null)
				{
					//System.out.println("artifactType can not be null. Please check biar.properties");
					logger.severe(Jbiar.df.format(new Date()) + " ArtifactType can not be null. Please check biar.properties.");
					return false;
				}
				if(fileloc == null)
				{
					//System.out.println("No file location mentioned in biar.properties file. exported biar file will be saved in current directory.");
					logger.severe(Jbiar.df.format(new Date()) + " No file location mentioned in biar.properties file. exported biar file will be saved in current directory.");
				}

				//ExportBiarFile.append(biarfile);
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
				IInfoObjects oInfoObjects,oAppObjects,oUnivObjects;
				BIARFactory factory = BIARFactory.getFactory();
				IExportOptions options = factory.createExportOptions();

				query = "Select * from CI_INFOOBJECTS WHERE SI_KIND = 'Folder' AND SI_NAME IN ('I/CAD Reports','I/LEADS Reports')";
				//Execite the query and retrieve the folder name
				oInfoObjects = oInfoStore.query(query);
				if (oInfoObjects.getResultSize()>0)
				{
					query = "Select * from CI_INFOOBJECTS WHERE SI_KIND = 'Webi' AND SI_PARENTID IN (";
					for (int i=0;i<=oInfoObjects.getResultSize()-1;i++)
					{
						IInfoObject oInfoObject = (IInfoObject)oInfoObjects.get(i);
						query = query + "'"+ oInfoObject.getID() + "',";
					}
					query = query.substring(0,query.length()-1) + ")";
					oInfoObjects.merge(oInfoStore.query(query));
				}

				query = "Select * from CI_APPOBJECTS WHERE SI_KIND = 'Folder' AND SI_NAME IN ('SGI Reporting')";
				oAppObjects = oInfoStore.query(query);
				if (oAppObjects.getResultSize()>0)
				{
					IInfoObject oAppObject = (IInfoObject)oAppObjects.get(0);
					query = "Select * from CI_APPOBJECTS WHERE SI_KIND = 'Universe' AND SI_PARENTID = " + oAppObject.getID();
					oUnivObjects = oInfoStore.query(query);
					if (oUnivObjects.getResultSize()>0)
					{
						query = "Select * From CI_APPOBJECTS where SI_KIND = 'MetaData.DataConnection' and SI_ID IN (";
						for(int i = 0; i<= oUnivObjects.getResultSize()-1;i++)
						{
							IInfoObject oInfoObject = (IInfoObject)oUnivObjects.get(i);
							IProperties prop = oInfoObject.properties();
							String connID = prop.getProperty("SI_DATACONNECTION").getValue().toString();
							String[] temp = null;
							temp = connID.split(" ");
							temp = temp[1].split("\\)");
							connID = temp[0];
							query = query + "'" + connID + "',";
						}
						query = query.substring(0,query.length()-1) + ")";
						oUnivObjects.merge(oInfoStore.query(query));
					}
					oAppObjects.merge(oUnivObjects);
				}

				oInfoObjects.merge(oAppObjects);

				query = "Select * from CI_APPOBJECTS where SI_KIND = 'Qaaws' AND SI_NAME IN ('AgencCombo','DispatchCombo','Priority_Unit','WidgetConfiguration','GaugeMeasures')";
				oInfoObjects.merge(oInfoStore.query(query));

				query = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND = 'UserGroup'";
				oInfoObjects.merge(oInfoStore.query(query));

				//System.out.println("Query Fired...");
				String datetime = Jbiar.dfb.format(new Date()).toString();
				datetime = datetime.replace(':','.');

				options.setIncludeDependencies(true);
				options.setIncludeSecurity(true);
				BIAROutput biarOutput = new BIAROutput(oEnterpriseSession, fileloc+datetime+"-"+biarfile, options);
				biarOutput.exportAll(oInfoObjects);
				biarOutput.finish();
				biarOutput.close();
				//System.out.println("Exported");
			}
		    catch(SDKException sdkEx)
		      {
				//System.out.println(sdkEx.getMessage());
				logger.severe(Jbiar.df.format(new Date()) + " " + sdkEx.getMessage());
				return false;

		      }
		    catch(OMException omEx)
		      {
				//System.out.println(omEx.getMessage());
				logger.severe(Jbiar.df.format(new Date()) + " " + omEx.getMessage());
				return false;

		      }
		    catch(BIARException biarEx)
		      {
				//System.out.println(biarEx.getMessage());
				logger.severe(Jbiar.df.format(new Date()) + " " + biarEx.getMessage());
				return false;

		      }

				return true;
		}

}

