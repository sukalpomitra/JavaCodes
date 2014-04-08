<%@ page import="java.net.URLEncoder,java.util.Hashtable,java.util.ArrayList,
                com.businessobjects.rebean.wi.*" %>


<%
	StringBuffer url = null;
	StringBuffer Incidents=null;
	StringBuffer GeoX=null;
	StringBuffer GeoY=null;
	ArrayList arrDrillIndex = null;
	Hashtable arrDrillValue = null;
try {   
	String incident = request.getParameter("Incidents");
	String dimensionName = incident.substring(0, incident.length()-1);
	dimensionName = dimensionName.substring(1, dimensionName.length());
	String geox = request.getParameter("GeoX");
	String geoy = request.getParameter("GeoY");
	String pattern = "&Incidents="+incident+"&GeoX="+geox+"+&GeoY="+geoy;

	int dpId = 0;
	
	DocumentInstance doc = (DocumentInstance)session.getAttribute("mapdoc");
	Report report = doc.getReports().getItem(0);
	System.err.println("APPLYPATTERN --- " + report.getReportMode().toString());
	session.setAttribute("replymapdoc",doc);
	
	DrillBar drillbar = null;
	String drillvalue = null;
	
	session.setAttribute("mapdoc",null);

	DataProviders dataProviders = doc.getDataProviders();
	
	url = new StringBuffer();
	Incidents = new StringBuffer();
	GeoX = new StringBuffer();
	GeoY = new StringBuffer();
	System.err.println("APPLYPATTERN --- BEFORE");
	if (dataProviders.getCount() >= 1) 
		{
		System.err.println("APPLYPATTERN --- AFTER");
		DataProvider dataProvider = dataProviders.getItem(dpId);
		
		int objectCount = dataProvider.getQuery().getResultObjectCount();
		
		for (int i=0;i<=objectCount-1;i++)
		{
			if (dataProvider.getQuery().getResultObject(i).getName().equals(dimensionName))
			{
				String dimensionID = dataProvider.getQuery().getResultObject(i).getID();
				dimensionID = dimensionID.substring(4, dimensionID.length());
				session.setAttribute("dimensionID",dimensionID);
				session.setAttribute("dpID",dpId+1);
			}
		}
		System.err.println("APPLYPATTERN --- AFTER "+dataProvider.getFlowCount());
		if (dataProvider.getFlowCount() == 1) 
			{
			
			dataProvider.runQuery();
			System.err.println("APPLYPATTERN --- RUNNING QUERY");
			Recordset rs = dataProvider.getResult(0);
			System.err.println("APPLYPATTERN --- GETTING RECORDSET");
			int colsCount = rs.getColumnCount();
			System.err.println("APPLYPATTERN --- COL COUNT");
			String[] columnNames = new String[colsCount];
			String[] columnNameRegexps = new String[colsCount];

			if (report.getReportMode().toString().equals("Analysis"))
			{
				arrDrillIndex = new ArrayList();
				arrDrillValue = new Hashtable();
				if (session.getAttribute("mapdrillbar") != null)
				{
					drillbar = (DrillBar)session.getAttribute("mapdrillbar");
					System.err.println("DRILLCOUNT "+drillbar.getCount());
					session.setAttribute("replymapdrillbar",null);
					session.setAttribute("replymapdrillbar",drillbar);
					if (drillbar != null)
					{
						for (int j=0;j<=drillbar.getCount()-1;j++)
						{
							drillvalue = drillbar.getItem(j).getName(); 
							for(int z=0;z<=colsCount-1;z++)
							{
								if (drillvalue.equals(rs.getColumnName(z)))
								{
									//System.err.println(" drillbar.getItem(j).getFilter() "+drillbar.getItem(j).getFilter());
									arrDrillIndex.add(z);
									arrDrillValue.put(z,drillbar.getItem(j).getFilter());

								}
							}
						}
					}
				}
			}
		
			System.err.println("APPLYPATTERN --- NO DRILL");
			for (int i = 0; i <= colsCount-1; i++) 
			{
				columnNames[i] = "[" + rs.getColumnName(i) + "]";
				
				columnNameRegexps[i] = "\\[" + rs.getColumnName(i) + "]";
	
			}
			rs.first();
			
			String rowPatternOld="";
			int j = -1;
			do {
				String rowPattern = pattern;
				
				boolean loopthrough = true;

				if (report.getReportMode().toString().equals("Analysis") && drillbar != null)
				{
					for(int keycol=0;keycol<=arrDrillIndex.size()-1;keycol++)
					{
						String filtervalue = arrDrillValue.get(arrDrillIndex.get(keycol)).toString();
						int key = Integer.parseInt(arrDrillIndex.get(keycol).toString());
						if (filtervalue == "" )
						{
							
							arrDrillValue.remove(key);
							arrDrillIndex.remove(keycol);
							continue;
						} 
						
						if (rs.getCellObject(key).toString().equals(filtervalue) == false)
						{
							
							loopthrough = false;
							break;
						 }
					}
				}
	
			
				if (loopthrough == false)
					continue;
			
				

				for (int i = 0; i <= colsCount-1; i++) 
				{
				
						if (rowPattern.indexOf(columnNames[i]) >= 0) 
							{
								Object  o = rs.getCellObject(i);
											
								
								rowPattern = rowPattern.replaceAll(columnNameRegexps[i],o != null ? o.toString() : "");
								if (columnNameRegexps[i].equals("\\"+incident) && o!=null)
								{
									
									Incidents.append(o.toString()+":");
									
								}
								else if (columnNameRegexps[i].equals("\\"+geox) && o!=null)
								{
																	
									GeoX.append(o.toString()+":");
								}
								else if (columnNameRegexps[i].equals("\\"+geoy) && o!=null)
								{
									
									GeoY.append(o.toString()+":");
								}
								if (o != null )
								{
								
								}
													
						}
						
					
				}
				
				
				if(rowPatternOld.equals(rowPattern) )
				{
					//System.err.println("in If o");	
				}
				else
				{
					//System.err.println("in else");
					url.append(rowPattern);
				}
				rowPatternOld = rowPattern;	
				
			} while (rs.next());

		}
		
	}
	Incidents=Incidents.deleteCharAt(Incidents.length()-1);
	
	GeoX=GeoX.deleteCharAt(GeoX.length()-1);
	GeoY=GeoY.deleteCharAt(GeoY.length()-1);
	
	System.err.println("INCIDENTPATTERN->"+Incidents.toString());
	System.err.println("GEOXPATTERN->"+GeoX.toString());
	System.err.println("GEOYPATTERN->"+GeoY.toString());
%>
	<form name="form1" id="form1" method="post" action="http://bidev02:8080/IntergraphGeospatial/BR2_DisplayMap1_LEADSDEMO.jsp">
	<input type = "hidden" name="Incidents" id="Incidents" value="<%= Incidents.toString() %>"></input> 
	<input type = "hidden" name="GeoX" id="GeoX" value="<%= GeoX.toString() %>"></input> 
	<input type = "hidden" name="GeoY" id="GeoY" value="<%= GeoY.toString() %>"></input> 
	</form>
	<script>
	//var t=setTimeout("document.forms[0].submit()",10000);
	document.forms[0].submit();
	</script>
<%

	
} catch (Exception e) {
	System.err.println("applyPattern.jsp Exception caught");
	e.printStackTrace(System.err);
}
finally
{
	//System.err.println("FINALLY->"+url.toString());
	
}
%>