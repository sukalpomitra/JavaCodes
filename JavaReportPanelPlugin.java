package Maps;

//
//Java class sample
//
import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.io.*;

import org.w3c.dom.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;

import java.util.Vector;

import java.text.ParseException;

import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.ListCellRenderer;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JOptionPane;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.ImageIcon;

import com.businessobjects.jrp.plugin.IDndListener;
import com.businessobjects.jrp.plugin.IFrame;
import com.businessobjects.jrp.plugin.IJavaReportPanel;
import com.businessobjects.jrp.plugin.IJavaReportPanelPlugin;
import com.businessobjects.jrp.plugin.IReportingPanel;
import com.businessobjects.jrp.plugin.reporting.IFreeCell;
import com.businessobjects.jrp.plugin.reporting.IDecoration;
import com.businessobjects.jrp.plugin.reporting.IReportPanel;
import com.businessobjects.jrp.plugin.reporting.IReportBody;
import com.businessobjects.jrp.plugin.reporting.IReport;
import com.businessobjects.jrp.plugin.reporting.IReportingListener;
import com.businessobjects.jrp.plugin.reporting.ReportActionEvent;
import com.businessobjects.jrp.plugin.reporting.ReportEvent;
import com.businessobjects.jrp.plugin.reporting.ReportSelectionEvent;


public class JavaReportPanelPlugin implements IJavaReportPanelPlugin,TableModelListener{
	IJavaReportPanel _jrp;
	IFrame 			_queryFrame;
	IReportPanel		_rp;
	IReportingPanel 	_irp;
	String docName = null;

	public JavaReportPanelPlugin(){
		System.out.println("Plugin loaded...");

	}

	/**
	 * Start method of IJavaReportPanelPlugin interface
	 */
	public void start(IJavaReportPanel jrp)
	{
		_jrp=jrp;

		System.out.println("Plugin Started with argument Java Report Panel version:"+jrp.getVersion());
		_irp=jrp.getReportingPanel();
		// Get active report panel
		_rp = _irp.getActiveReportPanel();

		docName = _jrp.getName();
		//
		// Customize Reporting Panel
		//
		//final IReportingPanel reporting =jrp.getReportingPanel();
		// Add custom frame

		JLabel label = new JLabel("<html>Please white while the <BR>Map Components are <BR>being loaded.</html>",JLabel.CENTER);
		JScrollPane scrLabel = new JScrollPane(label);
		final IFrame tempFrame = _irp.addReportingFrame("MapsWait","Intergraph Maps",scrLabel, null,IReportingPanel.DOCK_SIDE_WEST);

		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Maps");
		DefaultMutableTreeNode child1 = new DefaultMutableTreeNode("Pin");
		root.add(child1);
		DefaultMutableTreeNode child2 = new DefaultMutableTreeNode("HotSpot");
		root.add(child2);

		final JTree tree = new JTree(root);

		java.net.URL imgURL = this.getClass().getClassLoader().getResource("../images/hotspot.gif");
		java.net.URL imgrootURL = this.getClass().getClassLoader().getResource("../images/Maps.gif");

		ImageIcon leafIcon = new ImageIcon(imgURL);
		ImageIcon rootIcon = new ImageIcon(imgrootURL);
		if (leafIcon != null && rootIcon != null)
		{
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			renderer.setLeafIcon(leafIcon);
			renderer.setOpenIcon(rootIcon);
			tree.setCellRenderer(renderer);
        }

		JScrollPane scr = new JScrollPane(tree);

		UrlConnectionManager url = new UrlConnectionManager();
		HttpURLConnection httpUrl = null;
		try
		{
			httpUrl = url.getHttpUrlconnection("http://bidev03:8080/Map/mapServlet?DocName="+URLEncoder.encode(docName,"UTF-8"),false);
		}
		catch(UnsupportedEncodingException uee)
		{
			message("Unsupported URL");
		}
		String response = "Error;Error";
		try
		{
			Object obj = url.readResponse(httpUrl);
			response = obj.toString();
		}
		catch(IOException ex)
		{
			JOptionPane.showMessageDialog(null,"ERROR"+ex.getMessage());
		}

		String[] propVal = response.split(";");
		//Property sheet
		Object[][] data = new Object[2][2];
		data[0][0] = "Color";
		if (propVal[0].equals("Red") == true)
		{
			data[0][1] = new Color(153, 0, 153);
		}
		else
		{
			int rgb = Integer.parseInt(propVal[0]);
			data[0][1] = new Color(rgb);
		}
		data[1][0] = "Rotation in Degrees";
		data[1][1] = Integer.parseInt(propVal[1]);
		String[] datacol = {"",""};
		TableModel myData = new MyTableModel(data,datacol);
		myData.addTableModelListener(this);
		final JTable dataList = new JTable(myData);
		setUpColorRenderer(dataList);
		setUpColorEditor(dataList);

		dataList.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
			    }

			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent().getClass().getName().startsWith("javax.swing.JFormattedTextField") == false && e.getOppositeComponent().getClass().getName().startsWith("Maps.JavaReportPanelPlugin") == false)
				{
					if (dataList.getCellEditor() != null) {
							dataList.getCellEditor().stopCellEditing();
						}
					}
				}
		});

		JScrollPane scr1 = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//scr1.getViewport().setView(dataList);

		/************* Adding code to add Panel to the ScrollPane ******************/
		GridBagLayout gbl = new GridBagLayout();
		JPanel panel = new JPanel(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.ipady = 0;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(dataList,c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 1.0;
		c.weighty = 0.5;
		c.insets = new Insets(2,0,0,0);
		c.gridheight = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LAST_LINE_START;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		final JLabel lab = new JLabel("<html><B>(Name)</B></html>",JLabel.LEFT);
		lab.setVerticalAlignment(JLabel.TOP);
		lab.setBorder(BorderFactory.createMatteBorder(1,0,0,0,dataList.getGridColor()));
		Object[][] newData = new Object[2][1];
		newData[0][0] = "";
		newData[1][0] = "(Description)";
		String[] newDataCol = {""};
		TableModel newMyData = new HelpTableModel(newData,newDataCol);
		newMyData.addTableModelListener(this);
		JTable  labelData = new JTable(newMyData);
		labelData.setDefaultRenderer(String.class, new LineWrapCellRenderer(lab));
		labelData.setShowGrid(false);
		labelData.setBackground(panel.getBackground());
		labelData.setBorder(BorderFactory.createMatteBorder(1,0,0,1,dataList.getGridColor()));
		labelData.getColumnModel().getColumn(0).setPreferredWidth(100);
		panel.add(labelData,c);
		scr1.getViewport().setView(panel);

		SelectionListener listener = new SelectionListener(dataList,lab,labelData);
		dataList.getSelectionModel().addListSelectionListener(listener);
		dataList.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		/************* End code to add Panel to the ScrollPane ******************/

		//End Property sheet
		_irp.removeReportingFrame("MapsWait");
		final IFrame frame = _irp.addReportingFrame("Maps","Intergraph Maps",scr, null,IReportingPanel.DOCK_SIDE_WEST);
		final IFrame frame1 = _irp.addReportingFrame("Maps1","Intergraph Map properties",scr1, null,IReportingPanel.DOCK_SIDE_WEST);
		frame1.setVisible(false);

		_irp.addReportingListener(new IReportingListener()
		{
			public void reportChanged(ReportEvent e)
			{

			}

			public void reportSelectionChanged(final ReportSelectionEvent e)
			{
				if (e.getReportElementSelection().isMultiSelection() == false)
				{
					if(e.getReportElementSelection().getReportElement(0) instanceof IFreeCell)
					{
						//message(e.getReportElementSelection().getSelectedValue(0));
						IFreeCell cell = (IFreeCell)e.getReportElementSelection().getReportElement(0);
						if (cell.getText().contains("document.all.map.src"))
						{
							frame1.setVisible(true);
						}
						else
						{
							frame1.setVisible(false);
						}
						//cell.getDecoration().setBackgroundImageFromUrl(getFullPath("img/pinmap.png"),IDecoration.IMAGE_STRETCH);
						//_irp.applyFormat();

					}
					else
					{
						frame1.setVisible(false);
					}
				}
				else
				{
					frame1.setVisible(false);
				}
			}

			public void reportActionPerformed(ReportActionEvent e)
			{

			}
		});


		// Register this custom control in the internal Dnd system
		_jrp.registerDndListener(new IDndListener()
		{

			public void cancelDnd(Object dndObject)
			{
				//message("Dnd has been cancelled");
			}

			public void dragOver(Object dndObject)
			{
				//message("Mouse over me!");
			}

			public void drop(Object dndObject)
			{
				//message("Drop on me!");
			}

			public void dropEnd(Object dndObject, Component destination)
			{
				//message("End of Dnd");
			}

			public Object startDrag()
			{
				//message("Mouse drag gesture reconized, prepare object(s) to be dragged");
				//System.setProperty("JavaReportPanel.reporting.attachment.promptInvalidPosition","false");
				//message("Done");
				IReport report = _rp.getReport();
				IReportBody body = report.getReportBody();
				int count = body.getReportElementCount();
				IFreeCell cell = body.createFreeCell("");
				cell.setText("\"<script>var t = setTimeout(\"document.all.map.src='http://bidev03:8080/AnalyticalReporting/viewers/cdz_adv/applyPattern.jsp?&Incidents=[Formatted Incident ID]&GeoX=[Geolocation X Coordinate]&GeoY=[Geolocation Y Coordinate]'\",10000)</script><IFRAME id=\"map\" name=\"map\"  WIDTH= \"100%\" HEIGHT=  670 FRAMEBORDER=0 scrolling=\"no\"></IFRAME>\"");
				cell.setMinimumSize(550, 550);
				//cell.getDecoration().setBackgroundImageFromUrl(getFullPath("img/pinmap.png"),IDecoration.IMAGE_STRETCH);
				//cell.getDecoration().setBackground(Color.BLACK);
				cell.moveBy(100,100);

				//System.setProperty("JavaReportPanel.reporting.attachment.promptInvalidPosition","true");
				//message("Done");
				TreePath tp  = tree.getSelectionPath();
				TreeNode tn = null;
				Vector v=null;
				if(tp!=null)
				{
					tn = (TreeNode)tp.getLastPathComponent();
					//v = new Vector(2);
					//v.addElement(unvPanel.getDataSourceObject(tn.toString()));
				}
				if (tn.toString().compareTo("Pin")==0)
				{
					//cell.getDecoration().setBackgroundImageFromUrl("http:/"+getFullPath("img/pinmap.png"),IDecoration.IMAGE_STRETCH);
				}
				else
				{
					//cell.getDecoration().setBackgroundImageFromUrl("http:/"+getFullPath("img/pointmap.png"),IDecoration.IMAGE_STRETCH);
				}
				_irp.applyFormat();
				return tn.toString();
			}
			public Component getUI()
			{
				// returns the graphical component where the mouse event are listened from
				return tree;
			}
		});


	}


	/************TABLE CHANGED EVENT *******************/
	public void tableChanged(TableModelEvent e)
	{
		try
		{
			int row = 0;
			int column = 1;
			TableModel model = (TableModel)e.getSource();
			Object data = model.getValueAt(row, column);
			row = 1;
			column = 1;
			Object data1 = model.getValueAt(row, column);
			UrlConnectionManager url = new UrlConnectionManager();
			HttpURLConnection httpUrl = null;
			try
			{
				httpUrl = url.getHttpUrlconnection("http://bidev03:8080/Map/mapServlet?DocName="+URLEncoder.encode(docName,"UTF-8"),true);
			}
			catch(UnsupportedEncodingException uee)
			{
				message("Unsupported URL");
			}
			String response = "Error*Error";
			try
			{
				Color color = (Color)data;
				url.sendRequest(httpUrl,color.getRGB()+";"+data1);
				Object obj = url.readResponse(httpUrl);
			}
			catch(IOException ex)
			{
			}
		}
		catch(Exception ex)
		{
			message(ex.getMessage());
		}
	}


	private void message(String mess)
	{
		JOptionPane.showMessageDialog(null, mess);
	}

	private void setUpColorRenderer(JTable table) {
		ColorRenderer colorRenderer = new ColorRenderer(true);
		table.setDefaultRenderer(Color.class,
								 colorRenderer);
    }

	//Set up the editor for the Color cells.
	private void setUpColorEditor(JTable table) {
		final JTable finTable = table;
		//First, set up the button that brings up the dialog.
		final JButton button = new JButton("") {
			public void setText(String s) {
				//Button never shows text -- only color.
			}
		};
		button.setBackground(Color.white);
		button.setBorderPainted(false);
		button.setMargin(new Insets(0,0,0,0));

		//Now create an editor to encapsulate the button, and
		//set it up as the editor for all Color cells.
		final ColorEditor colorEditor = new ColorEditor(button);
		table.setDefaultEditor(Color.class, colorEditor);
		final SpinnerEditor spinnerEditor = new SpinnerEditor(0,360);
		table.setDefaultEditor(Integer.class, spinnerEditor);

		//Set up the dialog that the button brings up.
		final JColorChooser colorChooser = new JColorChooser();
		//XXX: PENDING: add the following when setPreviewPanel
		//XXX: starts working.
		JComponent preview = new ColorRenderer(false);
		preview.setPreferredSize(new Dimension(50, 10));
		colorChooser.setPreviewPanel(preview);
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				colorEditor.currentColor = colorChooser.getColor();
			}
		};
		final JDialog dialog = JColorChooser.createDialog(button,
										"Pick a Color",
										true,
										colorChooser,
										okListener,
										null); //XXXDoublecheck this is OK

		//Here's the code that brings up the dialog.
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button.setBackground(colorEditor.currentColor);
				colorChooser.setColor(colorEditor.currentColor);
				//Without the following line, the dialog comes up
				//in the middle of the screen.
				//dialog.setLocationRelativeTo(button);
				dialog.show();
				if (finTable.getSelectedRow() == 0 && finTable.getSelectedColumn() == 1)
				{
					finTable.updateUI();
				}
			}
		});
    }

	private String getFullPath(String relativePathToResource)
	{
		String fullpath=relativePathToResource;
		URL urlToJar = this.getClass().getClassLoader().getResource(relativePathToResource);		// Get the absolute path to this resource
		if(urlToJar!=null){
			fullpath = urlToJar.toExternalForm();
			fullpath=fullpath.substring(6);	// remove "file://"
		}
		return fullpath;
	}



	/**
	 * Stop method of IJavaReportPanelPlugin interface
	 */
	public void stop() {
		System.out.println("Plugin stopped...");
	}

	class ColorRenderer extends JLabel
	                        implements TableCellRenderer {
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			super();
			this.isBordered = isBordered;
			setOpaque(true); //MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(
								JTable table, Object color,
								boolean isSelected, boolean hasFocus,
								int row, int column) {
			setBackground((Color)color);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
												  table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
												  table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}
			return this;
		}
    }

	/*
	 * The editor button that brings up the dialog.
	 * We extend DefaultCellEditor for convenience,
	 * even though it mean we have to create a dummy
	 * check box.  Another approach would be to copy
	 * the implementation of TableCellEditor methods
	 * from the source code for DefaultCellEditor.
	 */
	class ColorEditor extends DefaultCellEditor {
		Color currentColor = null;

		public ColorEditor(JButton b) {
				super(new JCheckBox()); //Unfortunately, the constructor
										//expects a check box, combo box,
										//or text field.
			editorComponent = b;
			setClickCountToStart(0); //This is usually 1 or 2.

			//Must do this so that editing stops when appropriate.
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}

		public Object getCellEditorValue() {
			return currentColor;
		}

		public Component getTableCellEditorComponent(JTable table,
													 Object value,
													 boolean isSelected,
													 int row,
													 int column) {
			((JButton)editorComponent).setText(value.toString());
			currentColor = (Color)value;
			return editorComponent;
		}
    }

    public class HelpTableModel extends AbstractTableModel
	{
		private String[] columnNames;

		private Object[][] data;

		int row;

		public HelpTableModel(Object[][] data,String[] columnNames)
		{
			this.columnNames = columnNames;
			this.data = data;
		}

		public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				return data.length;
			}

			public String getColumnName(int col) {
				return columnNames[col];
			}


		public Object getValueAt(int row, int col) {
				this.row = row;
				return data[row][col];
			}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(row, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
				if (col == 0) {
					return false;
				} else {
					return true;
				}
			}

		public void setValueAt(Object value, int row, int col) {
				data[row][col] = value;
			}


	}

	public class MyTableModel extends AbstractTableModel
	{
		private String[] columnNames;

    	private Object[][] data;

    	int row;

    	public MyTableModel(Object[][] data,String[] columnNames)
    	{
			this.columnNames = columnNames;
			this.data = data;
		}

		public int getColumnCount() {
		        return columnNames.length;
		    }

		    public int getRowCount() {
		        return data.length;
		    }

		    public String getColumnName(int col) {
		        return columnNames[col];
		    }


		public Object getValueAt(int row, int col) {
				this.row = row;
		        return data[row][col];
		    }

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(row, c).getClass();
        }

		public boolean isCellEditable(int row, int col) {
		        if (col == 0) {
		            return false;
		        } else {
		            return true;
		        }
		    }

		public void setValueAt(Object value, int row, int col) {
		        data[row][col] = value;
		        fireTableCellUpdated(row, col);
		    }


	}

	public class SpinnerEditor extends AbstractCellEditor
	             implements TableCellEditor {
	         final JSpinner spinner;
	         private JTable currentTable;
	         private int selectedRow;
	         private int selectedColumn;
	         // Initializes the spinner.
	         public SpinnerEditor(int min, int max) {
	             spinner = new JSpinner(new SpinnerNumberModel(min, min, max,
	1));
	             spinner.setFocusable(true);//This alone does not fix the issue
	             //List all of the components and make them focusable
	             //then add an empty focuslistener to each
	             for(Component tmpComponent:spinner.getComponents()){
	                 tmpComponent.setFocusable(true);
	                 tmpComponent.addFocusListener(new FocusAdapter(){
	                 @Override
	                 public void focusLost(FocusEvent fe){
	                 }});
	             }

	             ChangeListener listener = new ChangeListener() {
				 			       public void stateChanged(ChangeEvent e) {
				 			         spinner.updateUI();
				 			       }
    		};

    		spinner.addChangeListener(listener);
}



	         public Component getTableCellEditorComponent(JTable table, Object
	value,
	                 boolean isSelected, int row, int column) {
	             spinner.setValue(value);
	             currentTable = table;
	             selectedRow = row;
	             selectedColumn = column;
	             return spinner;
	         }
	         public Object getCellEditorValue() {
				 try
				 {
					 spinner.commitEdit();
				 }
				 catch(ParseException pe)
				 {
				 }
	             return spinner.getValue();
	         }
     }

     public class SelectionListener implements ListSelectionListener
     {
		 JTable table;
		 JLabel label;
		 JTable helpTable;

		 // It is necessary to keep the table since it is not possible
		 // to determine the table from the event's source
		 SelectionListener(JTable table,JLabel label,JTable helpTable) {
			 this.table = table;
			 this.label = label;
			 this.helpTable = helpTable;
		 }
		 public void valueChanged(ListSelectionEvent e)
		 {
			 // If cell selection is enabled, both row and column change events are fired
			 if (e.getSource() == table.getSelectionModel()
				   && table.getRowSelectionAllowed()) {
				 // Column selection changed
				if(e.getSource().toString().contains("{0}"))
				{
					label.setText("<html><B>Color</B></html>");
					helpTable.getModel().setValueAt("The background color for the map element.",1,0);
				}
				else if(e.getSource().toString().contains("{1}"))
				{
					label.setText("<html><B>Rotation in degrees</B></html>");
					helpTable.getModel().setValueAt("Taking Y-axis as reference the angle in which the north arrow should rotate.",1,0);
				}
				else
				{
					label.setText("<html><B>(Name)</B></html>");
					helpTable.getModel().setValueAt("(Description)",1,0);
				}
			 } else if (e.getSource() == table.getColumnModel().getSelectionModel()
					&& table.getColumnSelectionAllowed() ){
				 // Row selection changed
				 if(e.getSource().toString().contains("{0}"))
				{
					label.setText("<html><B>Color</B></html>");
					helpTable.getModel().setValueAt("The background color for the map element.",1,0);
				}
				else if(e.getSource().toString().contains("{1}"))
				{
					label.setText("<html><B>Rotation in degrees</B></html>");
					helpTable.getModel().setValueAt("Taking Y-axis as reference the angle in which the north arrow should rotate.",1,0);
				}
				else
				{
					label.setText("<html><B>(Name)</B></html>");
					helpTable.getModel().setValueAt("(Description)",1,0);
				}
			 }

			 if (e.getValueIsAdjusting()) {
				 // The mouse button has not yet been released
			 }
		 }
    }

    public class LineWrapCellRenderer  extends JTextArea implements TableCellRenderer
	{
		JLabel label;

		public LineWrapCellRenderer(JLabel label)
		{
			this.label  = label;
		}

		public Component getTableCellRendererComponent(
		JTable table,
		Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column)
		{
			if (row == 1)
			{
				this.setText((String)value);
				this.setWrapStyleWord(true);
				this.setLineWrap(true);
				this.setBackground(new Color(236,233,216));
				table.setRowHeight(0, 15);

				int h = this.getPreferredSize().height + 2;
				int height = table.getRowHeight();
				height = Math.max(height, h);
				if (table.getRowHeight(row) != height)
				{
					table.setRowHeight(row, height);
				}

				return this;
			}
			if (row == 0)
			{
				table.setRowHeight(0, 15);
				return label;
			}
			return this;
		}
	}

}