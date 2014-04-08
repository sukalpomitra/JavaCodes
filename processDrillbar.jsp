<!--

--><%@ include file="wistartpage.jsp" %>
<%
response.setDateHeader("expires", 0);
try
{
String strEntry = requestWrapper.getQueryParameter("sEntry", true);
String strViewerID = requestWrapper.getQueryParameter("iViewerID", true);
String iReport = requestWrapper.getQueryParameter("iReport", false, "0");
int iReportIndex = Integer.parseInt(iReport);
String strAddFilterID = requestWrapper.getQueryParameter("addFilter", false, "");
String strRemoveFilterID = requestWrapper.getQueryParameter("removeFilter", false, "");
System.err.println(" strAddFilterID:: "+strAddFilterID + "strRemoveFilterID :: "+strRemoveFilterID);
DocumentInstance doc = reportEngines.getDocumentFromStorageToken(strEntry);
Report objReport = doc.getReports().getItem(iReportIndex);
DrillInfo objDrillInfo = (DrillInfo)objReport.getNamedInterface("DrillInfo");
DrillBar objDrillBar = objDrillInfo.getDrillBar();
if (!strAddFilterID.equals(""))
objDrillBar.add(strAddFilterID);
else if (!strRemoveFilterID.equals(""))
objDrillBar.remove(strRemoveFilterID);
DrillPath objDrillPath = objDrillInfo.getDrillPath();
objDrillPath.setAction(DrillActionType.SLICE);
objDrillInfo.executeDrill();

System.err.println("REMOVE");

DrillBar mapDrillBar = objDrillInfo.getDrillBar();
System.out.println("PROCESSDRILLBAR ");
session.setAttribute("mapdrillbar",mapDrillBar);
strEntry = doc.getStorageToken();
objUtils.setSessionStorageToken(strEntry, strViewerID, session);
requestWrapper.setQueryParameter("sEntry", strEntry);
out.clearBuffer();
%>
<jsp:forward page="report.jsp"/>
<%
}
catch(Exception e)
{
objUtils.displayErrorMsg(e, "_ERR_DRILL", true, out, session);
}
%>