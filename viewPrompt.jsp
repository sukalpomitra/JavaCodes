<%@ page language="java" contentType="text/html;charset=UTF-8"%>
<%@ page import="com.businessobjects.rebean.wi.Prompts,
 com.businessobjects.rebean.wi.*,
 com.businessobjects.adv_ivcdzview.*,
 com.crystaldecisions.sdk.framework.IEnterpriseSession,
 java.util.*"%>
<jsp:useBean id="objUserSettings" class="com.businessobjects.adv_ivcdzview.UserSettings" scope="session" />
<jsp:useBean id="requestWrapper" class="com.businessobjects.adv_ivcdzview.RequestWrapper" scope="page">
<%
requestWrapper.onStart(request);
requestWrapper.setCharacterEncoding("UTF-8");
%>
</jsp:useBean>
<%
try
{
response.setDateHeader("expires", 0);
String strEntry = requestWrapper.getQueryParameter("sEntry", true);
String strViewerID = requestWrapper.getQueryParameter("iViewerID", true);
String docID = requestWrapper.getQueryParameter("action", true);
String strSession = requestWrapper.getQueryParameter("entSession", true);
String strLanguage = requestWrapper.getQueryParameter("lang");
if (strLanguage ==  null)
{
if (session.getAttribute(ViewerTools.SessionLanguage) != null)
strLanguage = (String)session.getAttribute(ViewerTools.SessionLanguage);
else
strLanguage = "en";
}
if (!strLanguage.toLowerCase().startsWith("zh"))
strLanguage = strLanguage.substring(0, 2);
session.setAttribute(ViewerTools.SessionLanguage, strLanguage);
String strSkin = requestWrapper.getQueryParameter("skin");
if (strSkin != null)
session.setAttribute(ViewerTools.SessionSkin, strSkin);
else
{
strSkin = (String)session.getAttribute(ViewerTools.SessionSkin);
if (strSkin == null)
session.setAttribute(ViewerTools.SessionSkin, "skin_standard"); 
}
String strQueryString = requestWrapper.getQueryString();
if (strQueryString.indexOf("id=") < 0)
strQueryString += "&id=" + docID;
String viewerContextPath = request.getContextPath() + "/viewers/cdz_adv";
%>
<!doctype html public "-//W3C//DTD HTML 4.0 Transitional//EN" >
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style type="text/css">
.pBorder
{
border-right: black 1px;
border-top: black 1px;
border-left: black 1px solid;
border-bottom: black 1px
}
</style>
<link rel="stylesheet" type="text/css" name="stylelink" href="../../css/cmc.css">
<script language="javascript" src="lib/dom.js"></script>
<script language="javascript" src="lib/palette.js"></script>
<script language="javascript" src="lib/menu.js"></script>
<script language="javascript">
_lang="<%=strLanguage%>";
_skinName="skin_standard";
_img="images/main/";
initDom("lib/images/skin_standard/",_lang);
</script>
<script language="javascript">styleSheet();</script>
<script language="javascript" src="lib/dialog.js"></script>
<script language="javascript" src="lib/waitdialog.js"></script>
<script language="javascript" src="language/<%=strLanguage%>/scripts/labels.js"></script>
<script language="javascript" src="language/<%=strLanguage%>/scripts/errorManager.js"></script>
<script language="javascript">
_appExt=".jsp";
_viewerContext="<%=viewerContextPath%>";
_root=_viewerContext+'/';
_BtnLabelUpdate=parent._labelUpdate;
setTopFrameset();
waitDlg=newWaitDialogBoxWidget("waitDlg",250,100,_prompts_title);
alertDlg=newAlertDialog("alertDlg","","",_ERR_OK_BUTTON_CAPTION,_promptDlgInfo);
function showAlertDialog(msg,title,iPromptType,okCB)
{
if (iPromptType==null) iPromptType=0
if (title==null) title=_prompts_title;
alertDlg.setText(msg)
alertDlg.setTitle(title)
alertDlg.setPromptType(iPromptType)
alertDlg.yesCB=(okCB)?okCB:null
alertDlg.show(true)
}
function hideWaitDlg()
{
waitDlg.show(false);
}
function showWaitDlg()
{
waitDlg.show(true);
}
function initDlgs()
{
alertDlg.init();
waitDlg.init();
}
function goFinishSchedule(strEntry)
{
parent.document.forms[0].sentry.value=strEntry;
parent.document.forms[0].iViewerID.value="<%=strViewerID%>";
parent.updatePrompt();
}
function goBackSchedule()
{
}
    function advPrompts()
{
showWaitDlg();
advPromptsFrame.location.replace("getPrompts.jsp?<%=strQueryString%>&src=BCA&webApp=cmc");
}
alertDlg.write();
waitDlg.write();
initDlgs();
</script>
</head>
<body>
<%
IEnterpriseSession entSession = (IEnterpriseSession)session.getAttribute(strSession);
ReportEngines reportEngines = null;
if (entSession != null)
{
reportEngines = (ReportEngines)session.getAttribute(ViewerTools.SessionReportEngines);
if (reportEngines == null)
{
reportEngines = (ReportEngines)entSession.getService("ReportEngines");
session.setAttribute(ViewerTools.SessionReportEngines, reportEngines);
}
}
else
{
out.clearBuffer();
%>
<jsp:forward page="invalidSession.jsp"/>
<%
}
DocumentInstance doc = reportEngines.getDocumentFromStorageToken(strEntry);
objUserSettings.init(entSession, docID);
Prompts arrPrompts = doc.getPrompts();
int iNbPrompts = arrPrompts.getCount();
String[][] arrPromptNames = new String[iNbPrompts][2];
for (int i=0; i<iNbPrompts; i++)
{
arrPromptNames[i][0] = arrPrompts.getItem(i).getName();
arrPromptNames[i][1] = "false";
for (int j=0; j<i; j++) {
if (arrPromptNames[i][0].equalsIgnoreCase(arrPromptNames[j][0]))
{
arrPromptNames[i][1] = "true";  
arrPromptNames[j][1] = "true";
break;
}
}
}
if (iNbPrompts > 0)
{
out.println("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
out.println(" <tr><td height=\"5\"></td></tr>");
out.println(" <tr>");
out.println("  <td valign=\"top\">");
out.println("   <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
out.println("    <tr>");
%>
<script language="javascript">
document.write("<td>&nbsp;</td>");
document.write("<td><img src=\"../../images/buttonl.gif\"></td>");
document.write("<td class=\"clsButton\" valign=middle nowrap background=\"../../images/buttonm.gif\">");
document.write("<div class=\"clsButton\"><a href=\"javascript:advPrompts()\">"+_schedulePromptsButton+"</a></div>");
document.write("</td>");
document.write("<td><img src=\"../../images/buttonr.gif\"></td>");
</script>
<%
out.println("    </tr>");
out.println("   </table>");
out.println("  </td>");
out.println("  <td width=\"20\"></td>");
out.println("  <td class=\"pBorder\" width=\"20\">&nbsp;</td>");
out.println("  <td>");
out.println("   <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
}
for (int i=1; i<=iNbPrompts; i++)
{
    Prompt objPrompt = arrPrompts.getItem(i-1);
if (arrPromptNames[i-1][1].equals("true"))
{
try
{
DataProvider[] arrDataProviders = objPrompt.getDataProviders();
String strDPName = " (";
for (int j=0; j<arrDataProviders.length; j++)
{
if (j == 0) strDPName += arrDataProviders[j].getName();
else strDPName += ", " + arrDataProviders[j].getName();
}
strDPName += ")";
arrPromptNames[i-1][0] += strDPName;
}
catch (Exception e1)
{
}
}
out.println("    <tr><td>");
out.println("     <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
out.println("      <tr><td valign=\"top\" class=\"schedule\"><b>" + ViewerTools.convStr(arrPromptNames[i-1][0]) + "</b></td>");
out.println("       <td width=\"5\"></td>");
String[] arrSelectedValues = null;
if (objPrompt.getCurrentValues() != null && objPrompt.getCurrentValues().length > 0)
{
arrSelectedValues = objPrompt.getCurrentValues();
}
else if (objPrompt.getPreviousValues() != null && objPrompt.getPreviousValues().length > 0)
{
arrSelectedValues = objPrompt.getPreviousValues();
}
else if (objPrompt.getDefaultValues() != null && objPrompt.getDefaultValues().length > 0)
{
arrSelectedValues = objPrompt.getDefaultValues();
}
if (arrSelectedValues != null)
{
out.print("<td class=\"schedule\">");
for (int j=0; j<arrSelectedValues.length; j++)
{
if (j > 0) out.println("<br>");
out.print(arrSelectedValues[j]);
}
out.println("</td>");
}
out.println("      </tr>");
out.println("      <tr><td colspan=\"3\" height=\"10\"></td></tr>");
out.println("     </table>");
out.println("   </td></tr>");
}
if (iNbPrompts > 0)
out.println("</table></td></tr></table>");
%>
<iframe name="advPromptsFrame" src='html/empty.html' style="position:absolute;left:-100px;top:-100px;width:50px;height:50px"></iframe>
<iframe name="loadLOV" src='html/empty.html' style="position:absolute;left:-100px;top:-100px;width:50px;height:50px"></iframe>
<%
}
catch(Exception e)
{
e.printStackTrace();
out.clearBuffer();
out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><body>");
out.println("<p><b>Internal error occured when retrieving prompts. Error: " + e.getMessage() + "</b></p>");
}
%>
</body>
</html>
