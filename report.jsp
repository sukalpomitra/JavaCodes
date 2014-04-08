--><%@ include file="wistartpage.jsp" %>
<%
response.setDateHeader("expires", 0);
try
{
_logger.info("-->report.jsp");
String useCustomPrompts = requestWrapper.getQueryParameter("useCustomPrompts", false, "");
if (!useCustomPrompts.equals(""))
{
Properties customProperties = (Properties) session.getAttribute("BO_CUSTOM_PROPS");
if (customProperties == null)
customProperties = new Properties();
customProperties.setProperty("useCustomPrompts", useCustomPrompts);
session.setAttribute("BO_CUSTOM_PROPS", customProperties);
requestWrapper.removeQueryParameter("useCustomPrompts");
}
String strViewType = requestWrapper.getQueryParameter("viewType", false, "");
_logger.info("strViewType = " + strViewType);
String strForwardTo = requestWrapper.getQueryParameter("sForwardTo", false, "");
_logger.info("sForwardTo = " + strForwardTo);
System.err.println("sForwardTo = " + strForwardTo);
if (!strForwardTo.equals(""))
{
requestWrapper.removeQueryParameter("sForwardTo");
strForwardTo += ".jsp";
out.clearBuffer();
%>
<jsp:forward page="<%=strForwardTo%>"/>
<%
}
else if (strViewType.equals("P"))
{
out.clearBuffer();
%>
<jsp:forward page="getPDFView.jsp"/>
<%
}
String strReportMode = requestWrapper.getQueryParameter("sReportMode", true);
_logger.info("sReportMode = " + strReportMode);
String strReportID = requestWrapper.getQueryParameter("iReport", true);
if (strReportMode.equals("Analysis"))
{
System.err.println("DRILLING NOW");
String strQueryString = requestWrapper.getQueryString();

System.err.println(" strQueryString "+ strQueryString);

String sWorkInDrillMode = objUserSettings.getUserRight("WORK_IN_DRILLMODE");
{
String strBookmark = requestWrapper.getQueryParameter("sBookmark", false, "");
if (!strBookmark.equals(""))
{
strBookmark = "&1#" + strBookmark;
strQueryString = ViewerTools.removeQueryParameter(strQueryString, "sBookmark");
}
String strDrillbarSrc = "viewDrillbar.jsp?" + strQueryString;
String strReportblocSrc = "viewReport.jsp?" + strQueryString + strBookmark;
System.err.println("DEBUG "+strDrillbarSrc+":::"+strReportblocSrc);
String strDrillAction = requestWrapper.getQueryParameter("sDrillAction", false, "no");
strDrillbarSrc = ViewerTools.escapeQuotes(strDrillbarSrc);
strReportblocSrc = ViewerTools.escapeQuotes(strReportblocSrc);
String strmaphasdrillbar = null;
if (session.getAttribute("maphasdrillbar") != null)
{
strmaphasdrillbar = session.getAttribute("maphasdrillbar").toString();
session.setAttribute("maphasdrillbar",null);
}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="javascript" src="lib/dom.js"></script>
<script language="javascript">initDom(parent._skin,parent._lang)</script>
<script language="javascript">styleSheet()</script>
<script language="javascript">
var maphasDrillBar = "<%=strmaphasdrillbar%>"
var hasDrillBar=parent._showDrillBar; 
if (maphasDrillBar != undefined)
hasDrillBar = maphasDrillBar;
var sDrillbarSrc=hasDrillBar?"<%=strDrillbarSrc%>":"lib/empty.html";
var arrHierarchy = new Array();
var arrDimension = new Array();
var arrFreeDimension = new Array();
var arrFreeDetail = new Array();
var blnDrillAction = <%=(strDrillAction == "yes")?"true":"false"%>;
if (blnDrillAction && typeof(parent.ReportToolbar) != "undefined")
{
if (typeof(parent.ReportToolbar.initWaitMsg) != "undefined")
parent.ReportToolbar.initWaitMsg();
}
isPageLoaded=false;
drillFrame=newWidget("idDrillbar");
reportFrame=newWidget("idReportbloc");
reportBorder=newWidget("reportBorder");
function resizeCB(h)
{
if (isPageLoaded)
{
var w=winWidth()
var drillH=27
if(h && h > drillH) drillH=h
if (hasDrillBar)
{
drillFrame.move(0,0)
drillFrame.resize(w,drillH)
}
else
drillH=-1
reportBorder.move(0,drillH)
reportFrame.resize(w,Math.max(0,winHeight()-drillH-1))
}
}
function loadCB()
{
isPageLoaded=true
drillFrame.init()
reportFrame.init()
reportBorder.init()
setTimeout("resizeCB()",1)
}
function writeBody()
{
if(window.name=="Report")
{

document.write('<body onselectstart="return false" ondragstart="return false" style="cursor:default;overflow:hidden" onresize="setTimeout(\'resizeCB()\',1)" onload="setTimeout(\'resizeCB()\',1)" marginheight="0" marginwidth="0" leftmargin="0" topmargin="0">');
document.write('<iframe style="position:absolute;left:-100px;top:-1000px;width:100px;height:24px" id="idDrillbar" name="Drillbar" frameborder="0" src="'+ sDrillbarSrc +'"></iframe>');
document.write('<div class="treeZone" style="display:block;position:absolute;left:-1000px;top:25px;border-right-width:0px;border-left-width:0px;border-bottom-width:0px;" id="reportBorder">'
+'<iframe style="width:100px;height:100px" id="idReportbloc"  name="Reportbloc" frameborder="0" src="<%=strReportblocSrc%>"></iframe>'
+'</div>');
document.write('</body>');
setTimeout("loadCB()",1)
}
else if(window.name=="Reportbloc") 
{

window.location.replace("<%=strReportblocSrc%>");
}
else if(window.name=="Drillbar") 
{

window.location.replace("<%=strDrillbarSrc%>");
setTimeout("loadCB()",1)
}
}
</script>
</head>
<script language="javascript">writeBody()</script>
</html>
<%
_logger.info("<--report.jsp");
}
}
else
{
    _logger.info("<--report.jsp");
    out.clearBuffer();
	System.err.println("FORWARDING TO VIEWREPORT");
%>
<jsp:forward page="viewReport.jsp"/>
<%
}
}
catch(Exception e)
{
String strNewDoc = requestWrapper.getQueryParameter("sNewDoc", false, "false");
String strNEV = requestWrapper.getQueryParameter("sNEV", false, "no");
out.println("<html><head><script language=\"javascript\">");
if (strNEV.equals("yes") || strNewDoc.equals("true"))
out.println("function okCB() {parent.backToParent()}");
else
out.println("function okCB() {parent.restoreAfterError()}");
out.println("</script>");
objUtils.displayErrorMsg(e, "_ERR_REPORT",false, "okCB", out, session);
}
%>
