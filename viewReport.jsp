<%@ include file="wistartpage.jsp" %>
<%@ include file="incWom.jsp" %>
<%@ page import="com.businessobjects.adv_ivcdzview.Drill"%>
<%
response.setDateHeader("expires", 0);
response.setContentType("text/html;charset=UTF-8");
try
{
_logger.info("-->viewReport.jsp");
String strEntry = requestWrapper.getQueryParameter("sEntry", true);
_logger.info("strEntry IN = " + strEntry );
String strViewerID = requestWrapper.getQueryParameter("iViewerID", true);
String iReport = requestWrapper.getQueryParameter("iReport", true);
int nReportIndex = Integer.parseInt(iReport);
String sIsInteractive = requestWrapper.getQueryParameter("isInteractive", true);
boolean isInteractive = Boolean.valueOf(sIsInteractive).booleanValue();
String strPageMode = requestWrapper.getQueryParameter("sPageMode", false, "");
String strReportMode = requestWrapper.getQueryParameter("sReportMode", false, "");
String strPath = requestWrapper.getQueryParameter("sPath", false, "");
String iPage = requestWrapper.getQueryParameter("iPage", false, "");
if (iPage.equals(""))
iPage = requestWrapper.getQueryParameter("PageNumber", false, "");
String strNbPages = requestWrapper.getQueryParameter("nbPage", false, "NaN");
String strUndoEnabled = requestWrapper.getQueryParameter("sUndoEnabled", false, "true");
String strRequestNewReport = requestWrapper.getQueryParameter("sRequestNewReport", false, "false");
String sZoom = requestWrapper.getQueryParameter("zoom", false, "100");
float zoom = Float.parseFloat(sZoom) / 100;
String strZoom = String.valueOf(zoom);
String strRestoreScrollingInfo = requestWrapper.getQueryParameter("sRSI", false, "true");
String strIdRef = requestWrapper.getQueryParameter("idRef", false, "");
String strFollowBid = requestWrapper.getQueryParameter("sFollowBid", false, "");
boolean blnGetStorageToken = false;
DocumentInstance doc = reportEngines.getDocumentFromStorageToken(strEntry);

//Begining of the BI-GIS change
  if (session.getAttribute("mapdoc") == null)                                     
    session.setAttribute("mapdoc",doc);
//End of the BI-GIS change

ImageOption objImageOption = null;
objImageOption = doc.getImageOption();
objImageOption.setImageCallback("getImage.jsp");
objImageOption.setImageNameHolder("name");
objImageOption.setStorageTokenHolder("sEntry");
Reports objReports = doc.getReports();
int reportCount = objReports.getCount();
Report objReport =  null;
if (reportCount > 0)
{
if (!strPath.equals(""))
{
try
{
objReport = doc.setPath(strPath);
}
catch(Exception e1)
{
objReport = objReports.getItem(nReportIndex);
}
}
else
{
objReport = objReports.getItem(nReportIndex);
}
if (strReportMode.equals(""))
{
strReportMode = objReport.getReportMode().toString();
}
else if (!strReportMode.equals(objReport.getReportMode().toString()))
{
blnGetStorageToken = true;
}
if (strPageMode.equals(""))
strPageMode = objReport.getPaginationMode().toString();
PageNavigation objPageNav = null;
boolean isLastPage=true;
boolean isFirstPage=true;
if (strPageMode.equals("Page") || strPageMode.equals("QuickDisplay"))
{
if (!objReport.getPaginationMode().toString().equals(strPageMode))
blnGetStorageToken = true;
if(strPageMode.equals("Page"))
objReport.setPaginationMode(PaginationMode.Page);
else
objReport.setPaginationMode(PaginationMode.QuickDisplay);
objPageNav = objReport.getPageNavigation();
if ( iPage.equals("") )
{
iPage = Integer.toString(objPageNav.getCurrent());
_logger.info("Get page number from the current node iPage="+iPage);
}
else if ( iPage.equals("first") )
objPageNav.first();
else if ( iPage.equals("previous") )
objPageNav.previous();
else if ( iPage.equals("next") )
objPageNav.next();
else if ( iPage.equals("last") )
{
objPageNav.last();
iPage = Integer.toString(objPageNav.getCurrent());
strNbPages = iPage;
}
else
{
int pageNb = Integer.parseInt( iPage );
try
{
_logger.info("Setting page number to pageNb="+pageNb);
objPageNav.setTo( pageNb );
}
catch(REException ree)
{
ree.printStackTrace();
objPageNav.last();
iPage = Integer.toString(objPageNav.getCurrent());
strNbPages = iPage;
_logger.info("REException : could not set page to (" + pageNb + "). Set to last page (" + iPage + ")");
}
}
}
else if (strPageMode.equals("Listing"))
{
if (!objReport.getPaginationMode().toString().equals(strPageMode))
blnGetStorageToken = true;
objReport.setPaginationMode(PaginationMode.Listing);
}
StringBuffer sbMenuCode = new StringBuffer();
boolean isDrillLite = false;
if (strReportMode.equals("Analysis"))
{
_logger.info(" set to Drill mode");
DrillInfo objDrillInfo = (DrillInfo)objReport.getNamedInterface("DrillInfo");
DrillOption objDrillOption = objDrillInfo.getDrillOption();
isDrillLite = !objDrillOption.getGenerateDrillOutput();
objDrillOption.setCallBackScript("executeDrillAction.jsp?iReport=" + iReport + "&iViewerID=" + strViewerID + "&sReportMode=Analysis" + "&sPageMode=" + strPageMode + "&sDrillAction=yes");
objDrillOption.setCallBackFrame("_parent");
objDrillOption.setAmbiguousDrillCallBackScript("getAmbiguousDrill.jsp?iReport=" + iReport + "&iViewerID=" + strViewerID + "&sReportMode=Analysis" + "&sPageMode=" + strPageMode + "&sDrillAction=yes");
objDrillOption.setDrillActionHolder("action");
objDrillOption.setBlockHolder("block");
objDrillOption.setFromHolder("from");
objDrillOption.setToHolder("to");
objDrillOption.setHierarchyHolder("hier");
objDrillOption.setFilterHolder("filter");
objDrillOption.setStorageTokenHolder("sEntry");
sbMenuCode = Drill.getDrillBarMenu();
}
boolean blnReInitRMNode = false;
boolean blnHasChildren = false;
String strDrillAction = requestWrapper.getQueryParameter("sDrillAction", false, "no");
String strRefreshRepMapNode = requestWrapper.getQueryParameter("sRefreshRMNode", false, "no");
if (strDrillAction.equals("yes") || strRefreshRepMapNode.equals("yes"))
{
ReportMap objReportMap = doc.getReportMap();
objReportMap.refresh();
blnReInitRMNode = true;
if (strRefreshRepMapNode.equals("yes"))
{
ReportMapNodes objNodes = objReportMap.getStructure();
try
{
if (!objNodes.getChildAt(nReportIndex).isLeaf())
blnHasChildren = true;
}
catch(Exception e)
{
}
objNodes = null;
}
objReportMap = null;
}
String strNewChartBID = requestWrapper.getQueryParameter("sNewChartBid", false, "");
String strUserAgent = request.getHeader("User-Agent");
try
{
_logger.info("    Follow object :  strIdRef="+strIdRef+ "  strFollowBid="+strFollowBid);
if(!strIdRef.equals(""))
{
objPageNav.navigateToReportElement(strIdRef,(strFollowBid.equals(""))?null:strFollowBid);
}
}
catch(Exception e)
{ 
}
_logger.info("Trying to get objHTMLView.");
HTMLView objHTMLView = (HTMLView) objReport.getView(OutputFormatType.DHTML);
objHTMLView.setUserAgent(strUserAgent);
_logger.info("objHTMLView is retrieved.");
if ( !strPageMode.equals("Listing") && objPageNav != null )
{
iPage = Integer.toString( objPageNav.getCurrent() );
_logger.info("Final iPage=" + iPage);
isFirstPage = objPageNav.isFirst();
isLastPage = objPageNav.isLast();
if (isLastPage)
strNbPages = iPage;
objPageNav = null;
}
if (blnGetStorageToken)
{
strEntry = doc.getStorageToken();
objUtils.setSessionStorageToken(strEntry, strViewerID, session);
_logger.info("Get the new Token:" + strEntry);
}
Properties docProps = doc.getProperties();
String strLastRefreshDate = docProps.getProperty("lastrefreshdate");
if(strLastRefreshDate == null) strLastRefreshDate="";
String strLastRefreshTime = docProps.getProperty(PropertiesType.LAST_REFRESH_DATE, "0");
long lLastRefreshMilliSecDate = Long.parseLong(strLastRefreshTime) * 1000;
String strLastRefreshDuration = docProps.getProperty(PropertiesType.LAST_REFRESH_DURATION, "0");
String sbPartialResult = docProps.getProperty(PropertiesType.IS_PARTIALLY_REFRESHED, "false");
sbPartialResult = sbPartialResult.toLowerCase();
String sbSampleResult = docProps.getProperty(PropertiesType.HAS_SAMPLING_RESULTS, "false");
sbSampleResult = sbSampleResult.toLowerCase();
String sbUseQueryDrill = docProps.getProperty(PropertiesType.DATABASE_CONNECTED_DRILL, "false");
sbUseQueryDrill = sbUseQueryDrill.toLowerCase();
String sbMergeDimension = "false";
String strDocType = "";
String strKind = requestWrapper.getQueryParameter("kind", false, "");
if (!strKind.equals(""))
strDocType = ViewerTools.convertKindToDocType(strKind);
else
    strDocType = requestWrapper.getQueryParameter("doctype", false, "");
if (strDocType.equals(""))
strDocType = docProps.getProperty(PropertiesType.DOCUMENT_TYPE, "wid");
StringBuffer sbEventCode = new StringBuffer(12288);     
sbEventCode.append("<META HTTP-EQUIV=\"imagetoolbar\" CONTENT=\"no\">");
sbEventCode.append(sbMenuCode.toString());
if (isInteractive)
 sbEventCode.append("<script language=\"javascript\" src=\"scripts/ReportWom.js\"></script>");
sbEventCode.append("<script language=\"javascript\">\n");
if (strReportMode.equals("Viewing"))
sbEventCode.append("var p = parent;\n");
else
sbEventCode.append("var p = parent.parent;\n");
sbEventCode.append("var blnLoaded = false;\n");
sbEventCode.append("function newArrSelTab() { window.__arrSelTab = new Array(); }\n");
sbEventCode.append("function localKeyCB(e){ return p.reportKeyCB(e) };\n");
if (isInteractive) 
printDocument(sbEventCode, doc, nReportIndex);
sbEventCode.append("function onInit() {\n");
sbEventCode.append("  p.setKeyCB(this, true);\n");
sbEventCode.append("  var strEntry = \"" + strEntry + "\";\n");

//Begining of the BI-GIS change
session.setAttribute("mapstrEntry",strEntry);
//End of the BI-GIS change

sbEventCode.append("  p.changeEntry(strEntry);\n");
sbEventCode.append("  p.iReportID = \"" + iReport + "\";\n");
sbEventCode.append("  p.strPageMode = \"" + strPageMode + "\";\n");
sbEventCode.append("  p.strReportMode = \"" + strReportMode + "\";\n");
sbEventCode.append("  p.iPage = " + (iPage.equals("")?"1":iPage) + ";\n");
sbEventCode.append("  p.nbPage = " + strNbPages + ";\n");
sbEventCode.append("  p.lastRefreshDuration = " + strLastRefreshDuration + ";\n");
sbEventCode.append("p._lLastRefreshMilliSecDate=" + lLastRefreshMilliSecDate + ";");
if(!strLastRefreshDate.equals(""))
{
sbEventCode.append("  p.strLastRefreshDate = \"" + ViewerTools.escapeQuotes(strLastRefreshDate) + "\";\n");
}
else
{
if (lLastRefreshMilliSecDate != 0) 
{
sbEventCode.append("  p.dtDate = new Date(" + lLastRefreshMilliSecDate + ");\n");
sbEventCode.append("  p.strLastRefreshDate = p.formatDate(p.dtDate, p.strDateFormat);\n");
} 
else 
{
sbEventCode.append("  p.dtDate = \"\";\n");
sbEventCode.append("  p.strLastRefreshDate = p._noRefreshYet;\n");
}
}
sbEventCode.append("  p.blnPartialResult = " + sbPartialResult + ";\n");
sbEventCode.append("  p.hasSampleResult = " + sbSampleResult + ";\n");
sbEventCode.append("  p.bUndoableAction = " + strUndoEnabled + ";\n");
sbEventCode.append("  p.isFirstPage = " + isFirstPage + ";\n");
sbEventCode.append("  p.isLastPage = " + isLastPage + ";\n");
sbEventCode.append("  p.bUseQueryDrill = " + sbUseQueryDrill + ";\n");
sbEventCode.append("  p.bMergeDimensions = " + sbMergeDimension + ";\n");
sbEventCode.append("  if (!p._doNotHideOnLoadReport) p.hideBlockWhileWaitWidget();\n");
sbEventCode.append("  p._disableBlockFrame=false;\n");
sbEventCode.append("  p.sNewChartBID = \"" + strNewChartBID + "\";\n");
if (strRequestNewReport.equals("true"))
{
int iReportIndex = doc.getReports().getCount() - 1;
Report rep = doc.getReports().getItem( iReportIndex );
String sNewRepName=rep.getName();
String sPageMode = rep.getPaginationMode().toString();
String sRepMode = rep.getReportMode().toString();
sbEventCode.append("  if (p.requestNewReport)\n");
sbEventCode.append("    p.requestNewReport(\"" + ViewerTools.escapeQuotes(sNewRepName) + "\", \"" + sPageMode + "\", \"" + sRepMode + "\") \n\n");
}
if (blnReInitRMNode)
{
if (strRefreshRepMapNode.equals("yes"))
{
String strHasChildren = (blnHasChildren)?"true":"false";
sbEventCode.append("  p.eventManager.notify(p._EVT_REINIT_REPORTMAP_NODE," + strHasChildren + ");\n");
}
else
sbEventCode.append("  p.eventManager.notify(p._EVT_REINIT_REPORTMAP_NODE);\n");
}
if (strReportMode.equals("Analysis"))
{
sbEventCode.append("  if (p.filldrillFilterMenu)\n");
sbEventCode.append("    p.filldrillFilterMenu();\n");
}
boolean bTdcActivate = "true".equals(docProps.getProperty(PropertiesType.IS_TRACK_DATA_ACTIVATED, "false"));
sbEventCode.append("  p._bTdcActivate = " + bTdcActivate + ";");
_logger.info("Track Data Changes :" + (bTdcActivate?"ON":"OFF"));
String bTDCAutoUpdateDataOnRefresh = docProps.getProperty(PropertiesType.IS_TRACK_DATA_MODE_AUTO_USED, "false");
sbEventCode.append(" p.bTDCAutoUpdateDataOnRefresh = " + bTDCAutoUpdateDataOnRefresh + ";");
_logger.info("Auto-update data on refresh  :" + ("true".equals(bTDCAutoUpdateDataOnRefresh)?"ON":"OFF"));
 ReportEngine objReportEngine = reportEngines.getServiceFromStorageToken(strEntry);
 String tdcRefDate;
if (objReportEngine.getCanTrackData()) {
if (bTdcActivate)
{
TrackData tdc = doc.getTrackData();
tdcRefDate = ViewerTools.escapeQuotes(tdc.getFormattedReferenceDate());
if (tdcRefDate != null) {
sbEventCode.append("p._refTDCDate=\"" + tdcRefDate + "\";");
}
Date tdc_date=tdc.getReferenceDate(); 
long tdc_date_inMs=tdc_date.getTime();
sbEventCode.append("p._refTDCDateInMs=" + tdc_date_inMs + ";");
boolean mode=(tdc.getTrackDataMode()==ReferenceUpdateMode.AUTO)?true:false;
sbEventCode.append("p._bTDCAutoUpdateDataOnRefresh="+ mode+";");
for (int i = 0; i < reportCount; i++) {
objReport = objReports.getItem(i);
TrackDataInfo tdi = objReport.getTrackDataInfo();
if (tdi != null) {
sbEventCode.append(" p.arrReports[" + i + "].sc = " + tdi.isActive() + ";");
}
}
} else {
for (int i = 0; i < reportCount; i++) {
sbEventCode.append(" p.arrReports[" + i + "].sc = false;");
}
}
}
sbEventCode.append("\n  blnLoaded = true;");
sbEventCode.append("  if (p.bidTable) p.bidTable.length=0;");
sbEventCode.append("\np._bReportDrillLite="+isDrillLite+";\n");
sbEventCode.append("\np.setDrillMode();\n");
if (strPageMode.equals("Page"))
{
sbEventCode.append("  p.displayPageBorders(" + zoom +");\n");
}
if (isInteractive)
{
sbEventCode.append("\n  printWOM(p);");
}
sbEventCode.append("  p.updateWOM();\n");
sbEventCode.append("  if(!p.doRefreshOnOpen) setTimeout('if (typeof(p.hideWaitDlg) != \"undefined\") p.hideWaitDlg()', 1);\n");
sbEventCode.append("if (p._showInfoAutosave)p.showInfoAutosave();\n");
if (strRestoreScrollingInfo.equals("true"))
sbEventCode.append("  p.restoreScrollingInfo();\n");
sbEventCode.append("  p.eventManager.notify(p._EVT_PAGE_LOADED);");
sbEventCode.append("  p.eventManager.notify(p._EVT_REP_TDC_ACTIVATED," + iReport + ");\n");
sbEventCode.append("}\n");
sbEventCode.append("</script>\n");
sbEventCode.append("<style type=\"text/css\">\n");
sbEventCode.append("div#pageContainer\n");
sbEventCode.append("{\n");
sbEventCode.append("zoom:" + strZoom + ";\n");
sbEventCode.append("background-color:white;\n");
if (strPageMode.equals("Page"))
{
sbEventCode.append("top:15px;\n");
sbEventCode.append("left:15px;\n");
}
else
{
sbEventCode.append("top:0px;\n");
sbEventCode.append("left:0px;\n");
}
sbEventCode.append("}\n");
sbEventCode.append("</style>\n");
String bodyStyle = "background-color:" + (((!strDocType.equalsIgnoreCase("rep")) && (strPageMode.equals("Page"))) ? "#CECED0" : "white") + ";";
if (strDocType.equalsIgnoreCase("rep"))
bodyStyle += "zoom:" + strZoom + ";";
String strBodyAttrs = " leftmargin=\"0\" topmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" style=\"" + bodyStyle + "\" ";
if (isInteractive)
strBodyAttrs += "onselectstart=\"return p.CancelSelectStartCB()\" ";
objHTMLView.getContent(out, sbEventCode.toString(), strBodyAttrs);
out.println("<script language=\"javascript\">setTimeout('onInit()',1)</script>");
sbMenuCode = null;
sbEventCode = null;
objPageNav = null;
objReport = null;
objHTMLView = null;
}
objReports = null;
doc = null;
session.setAttribute("CDZ." + strViewerID + ".DocInstance", null);
_logger.info("<--viewReport.jsp");

//Begining of the BI-GIS change
 Boolean adddrillfilter = (Boolean)session.getAttribute("mapadddrillfilter");
 if (adddrillfilter != null && adddrillfilter == true)
 { 
   session.setAttribute("mapadddrillfilter",false);
   String sEntry = session.getAttribute("mapstrEntry").toString();
   session.setAttribute("mapfilterdrill",true);
   String dimensionID =    session.getAttribute("dimensionID").toString();
   String dpID = session.getAttribute("dpID").toString();
   String url = "processDrillbar.jsp?iViewerID=1&sEntry="+sEntry+"&iReport=0&sPageMode=QuickDisplay&sReportMode=Analysis&iPage=1&zoom=100&isInteractive=false&addFilter=DP"+dpID+"."+dimensionID;
 %> 
 <script> self.parent.frames[0].document.frmDrillbar.target = "_parent";
 self.parent.frames[0].document.frmDrillbar.action = "<%=url%>";
 self.parent.frames[0].document.frmDrillbar.submit();
	 </script> 
<% } 
   Boolean drillfilter = (Boolean)session.getAttribute("mapfilterdrill");
   System.err.println("VIEWREPORT VALUES:- "+drillfilter + " " + adddrillfilter);
   if (drillfilter != null && drillfilter == true && (adddrillfilter == null || adddrillfilter == false))
   {  
   	session.setAttribute("mapfilterdrill",false);
   	String sEntry = session.getAttribute("mapstrEntry").toString();
  	String incident = session.getAttribute("mapincident").toString();
   
   	String url = "processDrillFilters.jsp?iViewerID=2&sEntry="+sEntry+"&iReport=0&sPageMode=QuickDisplay&sReportMode=Analysis&iPage=&zoom=100&isInteractive=false&sID=DP1.DO122a&sFilter="+incident+"&sDrillAction=yes";
 %> 
 
 <script> 
 try
 {
 self.parent.frames[0].document.frmDrillbar.target = "_parent";
 self.parent.frames[0].document.frmDrillbar.action = "<%=url%>";
 self.parent.frames[0].document.frmDrillbar.submit();
 }
 catch(err)
 {
 }
 </script> 
 <% }

//End of the BI-GIS change

}
catch(Exception e)
{
String strNewDoc = requestWrapper.getQueryParameter("sNewDoc", false, "false");
String strNEV = requestWrapper.getQueryParameter("sNEV", false, "no");
out.clearBuffer();
response.setContentType("text/html;charset=UTF-8");
out.println("<html><head><script language=\"javascript\">");
out.println("function okCB() {");
out.println("   var topf=getTopViewerFrameset();");
if (strNEV.equals("yes") || strNewDoc.equals("true"))
out.println("   if (topf!=null) topf.backToParent();");
else
out.println("   if (topf!=null) topf.restoreAfterError();");
out.println("}");
out.println("</script>");
objUtils.displayErrorMsg(e, "_ERR_REPORT",false, "okCB", out, session);
}
%>