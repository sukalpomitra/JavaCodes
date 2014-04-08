<%@page language="java"%>
<%
    //The value to display on the meter
    String[]  Incidents = request.getParameter("Incidents").split(":");
    String[]  GeoX = request.getParameter("GeoX").split(":");
    String[]  GeoY = request.getParameter("GeoY").split(":");
     

    if (Incidents == null)
        Incidents = new String[0];
    if (GeoX == null)
        GeoX = new String[0];
    if (GeoY == null)
        GeoY = new String[0];
   StringBuffer MyIncidents =new StringBuffer();
   StringBuffer MyGeoX =new StringBuffer();
   StringBuffer MyGeoY =new StringBuffer();
    
%>

<html>
<head>
<!--
<%=request.getQueryString()%>
-->
</head>

<body>

<p><font size="2" color="red" face="verdana" >The Incidents in Arrest Summary are shown with Pins: </font> <font size="4" color="blue" face="verdana" >
<% 

     for(int i=0; i<Incidents.length; i++)
   {
      MyIncidents.append(Incidents[i]);
      MyIncidents.append(";");
    }
  for(int i=0; i<GeoX.length; i++)
   {
      MyGeoX.append(GeoX[i]);
      MyGeoX.append(";");
    }
  for(int i=0; i<GeoY.length; i++)
   {
      MyGeoY.append(GeoY[i]);
     MyGeoY.append(";");
    }
 %>

</font></p>

<table width=950 height =600 border="1"  cellpadding="0px" cellspacing="0px">
<tr>
<td width=950  height = 600 align="center">

<!-- <IFRAME SRC="http://bidev02:81/1LEADS_DemoMap/Default.aspx?Incidents='<%=MyIncidents%>'&GeoX='<%=MyGeoX%>'&GeoY='<%=MyGeoY%>'" WIDTH=940 HEIGHT= 600 FRAMEBORDER=1 align="center" ></IFRAME> -->

<form method="post" action="http://bidev02:81/1LEADS_DemoMap_HOTSPOT/Default.aspx">
<input type="hidden" name="Incidents" id="Incidents" value="<%= MyIncidents.toString() %>"></input>
<input type="hidden" name="GeoX" id="GeoX" value="<%= MyGeoX.toString() %>"></input>
<input type="hidden" name="GeoY" id="GeoY" value="<%= MyGeoY.toString() %>"></input>
</form>


</td>
</tr>

<script>
document.forms[0].submit();
</script>

</body>
</html>