<%@ page import="java.net.URLEncoder,java.util.Hashtable,java.util.ArrayList,
                com.businessobjects.rebean.wi.*" %>


<%
	
try 
{ 
	String incident = request.getParameter("Incidents");
	incident = incident.substring(0, incident.length()-1);
	session.setAttribute("mapincident",incident);
	DocumentInstance doc = (DocumentInstance)session.getAttribute("replymapdoc");
	Report report = doc.getReports().getItem(0);

	DrillBar drillbar = null;
	String drillvalue = null;
	System.err.println("REPLYPATTERN --- " + report.getReportMode().toString());
	if (report.getReportMode().toString().equals("Analysis"))
	{
		DrillInfo objDrillInfo = (DrillInfo)report.getNamedInterface("DrillInfo");
		if (session.getAttribute("replymapdrillbar") != null)
		drillbar = (DrillBar)session.getAttribute("replymapdrillbar");
		session.setAttribute("replymapdrillbar",null);
		if (drillbar == null)
		drillbar = objDrillInfo.getDrillBar();
		//drillbar.add("Arrest Number");
		//System.err.println("ReplyPattrn 2");
		//objDrillInfo.executeDrill();
		out.write("DRILLCOUNT "+drillbar.getCount());
		if (drillbar != null)
		{
		
		String sEntry = session.getAttribute("mapstrEntry").toString();
		String dimensionID = session.getAttribute("dimensionID").toString();
		String dpID = session.getAttribute("dpID").toString();
		String url = "processDrillbar.jsp?iViewerID=1&sEntry="+sEntry+"&iReport=0&sPageMode=QuickDisplay&sReportMode=Analysis&iPage=1&zoom=100&isInteractive=false&addFilter=DP"+dpID+"."+dimensionID;
		session.setAttribute("mapfilterdrill",true);
		
%>
<script>
parent.parent.frames[0].document.frmDrillbar.target = "_parent";
parent.parent.frames[0].document.frmDrillbar.action = "<%=url%>";
parent.parent.frames[0].document.frmDrillbar.submit();
</script>
<%
		}
	}
	else
	{
		session.setAttribute("maphasdrillbar","true");
		session.setAttribute("mapadddrillfilter",true);
		String sEntry = session.getAttribute("mapstrEntry").toString();
		String url = "processDrill.jsp?iViewerID=1&sEntry="+sEntry+"&iReport=0&sPageMode=QuickDisplay&sReportMode=Analysis&iPage=1&zoom=100&isInteractive=false&bids=&sDrillMode=drill";
%>
<script>
var l = parent.location;
var urljs = "<%=url%>"
l.replace(urljs);
</script>
<%
	}
	
} 
catch (Exception e) 
{
	System.err.println("replyPattern.jsp Exception caught");
	e.printStackTrace(System.err);
}
finally
{
	//System.err.println("FINALLY->"+url.toString());
	
}
%>