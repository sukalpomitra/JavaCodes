--><%@ include file="wistartpage.jsp" %>
<%
response.setDateHeader("expires", 0);
try
{
String strEntry = requestWrapper.getQueryParameter("sEntry", true);
String strViewerID = requestWrapper.getQueryParameter("iViewerID", true);
String iReport = requestWrapper.getQueryParameter("iReport", false, "0");
int iReportIndex = Integer.parseInt(iReport);
String strID = requestWrapper.getQueryParameter("sID", false, "");
String strFilterValue = requestWrapper.getQueryParameter("sFilter", false, "");
DocumentInstance doc = reportEngines.getDocumentFromStorageToken(strEntry);
Report objReport = doc.getReports().getItem(iReportIndex);
DrillInfo objDrillInfo = (DrillInfo)objReport.getNamedInterface("DrillInfo");
DrillPath objDrillPath = objDrillInfo.getDrillPath();
objDrillPath.setAction(DrillActionType.SLICE);
DrillFromElement objDrillFromElt = (DrillFromElement)objDrillPath.getFrom().add();
objDrillFromElt.setObjectID(strID);
objDrillFromElt.setFilter(strFilterValue);
objDrillInfo.executeDrill();
DrillBar mapDrillBar = objDrillInfo.getDrillBar();
System.err.println("PROCESSDRILLFILTER ");
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