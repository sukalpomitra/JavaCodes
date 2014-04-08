


import java.util.*;
import java.util.logging.*;
import com.perforce.api.*;
import java.io.*;
import java.lang.*;
import java.text.*;


public class JPerforce
{
	Properties prp = new Properties();
	Env env;
	private static String logFile = "BOForce.log";
	private final static DateFormat df = new SimpleDateFormat ("yyyy.mm.dd  hh:mm:ss ");
	public FileHandler handler;
	// Add to the desired logger
	Logger logger;

	public JPerforce(String prpFile)
	{
		try
		{
		handler = new FileHandler(logFile, true);
		logger = Logger.getLogger("BOForce");
		logger.addHandler(handler);
		this.prp.load(new BufferedInputStream(new FileInputStream(prpFile)));
		env = new Env(prp);

		}
		catch(IOException ioEx)
		{
		 //log error
		 //System.out.println(ioEx.getMessage());
		 logger.severe(JPerforce.df.format(new Date()) + " " + ioEx.getMessage());
		 return;
		}

		catch(Exception ex)
		{
			//log error
			//System.out.println(ex.getMessage());
			logger.severe(JPerforce.df.format(new Date()) + " " + ex.getMessage());
			return;
		}

	}

	public boolean p4CheckIn(String filename)
	{

		 String l,location,  comments,user ;
		 P4Process p = null;

		 location = prp.getProperty("p4.Location");
		 //filename = prp.getProperty("p4.CheckInFile");
		 comments = prp.getProperty("p4.CheckInComments");
		 user = prp.getProperty("p4.user");

		 if (location==null)
		 {
			 //System.out.println("Location argument cannot be null. Please check p4.properties file.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "Location argument cannot be null. Please check p4.properties file.");
			 return false;
		 }
		 if (filename==null)
		 {
			 //System.out.println("No file to check in.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "No file to check in.");
			 return false;
		 }
		 if (comments==null)
		 {
			 //System.out.println("Comments argument cannot be null. Please check p4.properties file.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "Comments argument cannot be null. Please check p4.properties file.");
			 return false;
		 }

		 //CHECK IN command
		  String[] cmd = { "p4", "add", "-f", location + filename};
		  String[] cmd1 = { "p4", "submit","-d",comments, location + filename};
		  String changeno = null;

		  try
		  {
			  env.checkValidity();
			  p = new P4Process(env);
			  p.exec(cmd);
			  while (null != (l = p.readLine()))
			  {
				  if (l.startsWith("Client 'robot-client'"))
				  {
					  //System.out.println("Client argument cannot be null. Please check p4.properties file.");
					  logger.severe(JPerforce.df.format(new Date()) + " " + "Client argument cannot be null. Please check p4.properties file.");
					  return false;
				  }
				  if (l.startsWith("Access for user 'robot'"))
				  {
					  //System.out.println("User argument cannot be null. Please check p4.properties file.");
					  logger.severe(JPerforce.df.format(new Date()) + " " + "User argument cannot be null. Please check p4.properties file.");
					  return false;
				  }
				  // Parse the output.
					//System.out.println(l);
					logger.info(JPerforce.df.format(new Date()) + " " + l);
			  }
			  p.flush();
			  //p.close();
			  Thread.currentThread().sleep(3000);
			  //p1 = new P4Process(env);
			  p.exec(cmd1);
			  String l1 = l;
			  while (null != (l = p.readLine()))
			  {
				  l1 = l;
				  if (l.startsWith("Submitting"))
				  {
					  String[] temp = null;
					  temp = l.split(" ");
					  changeno = temp[2].substring(0,temp[2].length()-1);
				  }
				  //System.out.println(l);
				  logger.info(JPerforce.df.format(new Date()) + " " + l);
			  }
			  //p1.close();
			  if (!l1.endsWith("submitted."))
			  {
				  String[] cmd2 = { "p4", "revert", location + filename};
				  Thread.currentThread().sleep(3000);
				  //p2 = new P4Process(env);
				  p.exec(cmd2);
				  String[] cmd3 = { "p4", "change","-d",changeno};
				  //p3 = new P4Process(env);
				  Thread.currentThread().sleep(3000);
				  p.exec(cmd3);
				  p.close();
				  return false;
			  }
			  else
			  {
				  p.close();
				  return true;
		  	  }
		 }
		catch (PerforceException pex)
		{
				//log
			if (pex.getMessage().startsWith("No output"))
			{
				//System.out.println("Either port argument or sysroot argument is invalid.Please check p4.properties file");
				logger.severe(JPerforce.df.format(new Date()) + " " + "Either port argument or sysroot argument is invalid.Please check p4.properties file.");
			}
			else
			{
				//System.out.println(pex.getMessage());
				logger.severe(JPerforce.df.format(new Date()) + " " + pex.getMessage());
			}
			return false;
		}
  		 catch(IOException ioEx)
  		 {
			//System.out.println(ioEx.getMessage());
			logger.severe(JPerforce.df.format(new Date()) + " " + ioEx.getMessage());
			return false;
		 }
		 catch(Exception ex)
		 {
			 //System.out.println(ex.getMessage());
			 logger.severe(JPerforce.df.format(new Date()) + " " + ex.getMessage());
			 return false;
		 }

	}


	//make sure the System.out lines are replaced in the methods.

	public boolean p4CheckOut(StringBuffer FileName)
	{
		 String l,location, filename, workspaceloc, client,s ;

		 P4Process p = null;

		 location = prp.getProperty("p4.Location");
		 filename = prp.getProperty("p4.CheckOutFile");
		 workspaceloc = prp.getProperty("p4.WorkspaceLocation");
		 client = prp.getProperty("p4.client");



		 if (location==null)
		 {
			 //System.out.println("Location argument cannot be null. Please check p4.properties file.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "Location argument cannot be null. Please check p4.properties file.");
			 return false;
		 }
		 if (filename==null)
		 {
			 //System.out.println("Filename argument cannot be null. Please check p4.properties file.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "Filename argument cannot be null. Please check p4.properties file.");
			 return false;
		 }
		 if (workspaceloc==null)
		 {
			 //System.out.println("Workspace location is the client workspace root and is required argument. It cannot be null. Please check p4.properties file.");
			 logger.severe(JPerforce.df.format(new Date()) + " " + "Workspace location is the client workspace root and is required argument. It cannot be null. Please check p4.properties file.");
			 return false;
		 }
		 s = location;
		 s =location.replace("//" + client , workspaceloc );
		 FileName.append(s.replace("/" ,"\\"));
		 FileName.append(filename);

		 try
		 {
			  //CHECK OUT
				env.checkValidity();
				String[] cmd = { "p4", "edit", location + filename};
				p = new P4Process(env);
				p.exec(cmd);
				while (null != (l = p.readLine()))
			    {
				  if (l.startsWith("Client 'robot-client'"))
				  {
					  //System.out.println("Client argument cannot be null. Please check p4.properties file.");
					  logger.severe(JPerforce.df.format(new Date()) + " " + "Client argument cannot be null. Please check p4.properties file.");
					  return false;
				  }
				  if (l.startsWith("Access for user 'robot'"))
				  {
					  //System.out.println("User argument cannot be null. Please check p4.properties file.");
					  logger.severe(JPerforce.df.format(new Date()) + " " + "User argument cannot be null. Please check p4.properties file.");
					  return false;
				  }
				  // Parse the output.
				 // System.out.println(l);
				 logger.info(JPerforce.df.format(new Date()) + " " + l);
				  if (l.endsWith("opened for edit"))
				  {
					  p.close();
					  return true;
				  }
			    }
			 	p.close();

				return false;
 		 }
		 catch (PerforceException pex)
		 {
			//log
			//System.out.println("P4:");
			//System.out.println(pex.getMessage());
			logger.severe(JPerforce.df.format(new Date()) + " " + pex.getMessage());
			return false;
		 }
		 catch(IOException ioEx)
		  {
			//System.out.println(ioEx.getMessage());
			logger.severe(JPerforce.df.format(new Date()) + " " + ioEx.getMessage());
			return false;
		 }


	}

}