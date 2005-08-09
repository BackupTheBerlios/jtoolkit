/*
 * @(#)BeanGen.java        1.00 03/22/02
 *
 * 
 */

package jtoolkit.db;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.sql.DataSource;
import javax.naming.NamingException;

//import gov.ca.lc.framework.util.J2eeServices;
//import gov.ca.lc.framework.util.JNDIResourceFactory;

/**
 * BeanGen is a utility to generate source code for JavaBean class.
 * By default when you run java BeanGen, the utility connects to
 * oracle database and retrieve information from login_tbl and 
 * generate LoginBean.java file in current directory.
 *
 * @version 1.00 22 March 2002
 * @author Vikrant Sawant
 */

public class BeanGen extends JFrame
{
	public static String nl     = "\r\n";
	public static String tab    = "\t";
	
	// gui fields
	
	JButton okButton          	= new JButton("Generate");
	JButton cancelButton      	= new JButton("Cancel");
	//JTextField dbUserName     	= new JTextField("senate_districts");
	//JPasswordField dbPassword 	= new JPasswordField("senatb");	
	//JTextField dbInstance     	= new JTextField("DECY2KCWEBDB");
	JTextField poolName     	= new JTextField("CWebJtsDataSource");
	JTextField tableField    	= new JTextField("list_case_tbl");
	JTextField beanField     	= new JTextField("ListCaseBean");
	//JLabel usernameLabel      	= new JLabel("User Name");
	//JLabel passwordLabel      	= new JLabel("Password");
	//JLabel instanceLabel      	= new JLabel("Database");
	JLabel poolLabel      		= new JLabel("Pool Name");	
	JLabel beanLabel          	= new JLabel("Bean Name");	
	JLabel tableLabel          	= new JLabel("Table Name");	

	public static String header =
		"/* "																+ nl +
		" * {0}.java        1.00 {1} "										+ nl +
		" * "																+ nl +
		" * Copyright 2002 Legislative Data Center. All Rights Reserved. "	+ nl +
		" * "																+ nl +
		" * This software is the proprietary information of LDC. "			+ nl +
		" * Use is subject to license terms. "								+ nl +
		" * "																+ nl +
		" */ "																+ nl + nl +
		"package gov.ca.lc.framework.beans; "								+ nl + nl +
		"import java.io.Serializable; "										+ nl + 
		"import java.io.CharArrayWriter; "									+ nl + 
		"import gov.ca.lc.framework.beans.XmlBean; "						+ nl + 	nl + nl +	
		"/** "																+ nl +
		" * {0} "															+ nl +
		" * "																+ nl +
		" * Use this class to set or retrieve the information about "		+ nl +
		" * {0}. A typical example would be to populate form fields. "		+ nl +
		" * "																+ nl +
		" * @version 1.00 {1} "												+ nl +
		" * @author  BeanGen "												+ nl +
		" */ "																+ nl +
		"public class {0} implements Serializable, XmlBean "				+ nl ;

	public BeanGen()
	{
		super("Java Bean Generator.");
	}

	public static void main( String[] args)
	{
		BeanGen gen = new BeanGen();
		gen.createUI();
		gen.setSize(300, 500);
		gen.setVisible(true);
		gen.pack();
		gen.show();
	}

	public void generate(  String fileName, ArrayList types, ArrayList columns)
	{
		StringBuffer code = new StringBuffer();

		SimpleDateFormat formatter
		     = new SimpleDateFormat ("MM/dd/yyyy");
		 Date currentTime_1 = new Date();
		 String dateString = formatter.format(currentTime_1);
 
		Object[] testArgs = { fileName, dateString };
 
		MessageFormat form = new MessageFormat( header );
 		code.append(form.format(testArgs));
		code.append("{" + nl + nl );
		code.append(tab + "/** "							+ nl);
		code.append(tab + " * Member variables of "+fileName+". "	+ nl);
		code.append(tab + " */ "							+ nl);
		

		for ( int i=0; i < types.size() ; i++ )
		{
			code.append(tab + "private" + tab + types.get(i) + tab + tab + columns.get(i) +";"+nl);
		}
		code.append(tab + "private" + tab + "String" + 	tab + tab + 	"xml;"+nl+nl);


		for ( int i=0; i < types.size() ; i++ )
		{
			String v = (String)columns.get(i);
			String c = initCap(v);
			String t = (String)types.get(i);

			code.append(tab + "/** "												+ nl );
			code.append(tab + " * Get value of " + c + " attribute. "				+ nl );
			code.append(tab + " * @return " + t + " value of " + c + " attribute. "	+ nl );
			code.append(tab + " * @see set" + c + "(" + t + " " + v + "). "			+ nl );
			code.append(tab + " */ "												+ nl );
			code.append(tab + " public " + t + " get" + c + "() "					+ nl );
			code.append(tab + " { "													+ nl );
			code.append(tab + "     return " + v + "; "								+ nl );
			code.append(tab + " } "													+ nl+nl );
		}


		String beanTag  = fileName.substring(0, fileName.indexOf("Bean")).toLowerCase() ;

		code.append(tab + "/** "												+ nl );
		code.append(tab + " * Get Xml for current Bean. " 						+ nl );
		code.append(tab + " * @return xml string "								+ nl );
		code.append(tab + " * @see setXml()" 									+ nl );
		code.append(tab + " */ "												+ nl );
		code.append(tab + " public String getXml() "							+ nl );
		code.append(tab + " { "													+ nl );
		code.append(tab + tab +"xml  = \"<"+beanTag+">\";"+ nl);
		for ( int i=0; i < types.size() ; i++ )
		{
			String v = (String)columns.get(i);
			String c = initCap(v);
			String t = (String)types.get(i);
			code.append(tab + tab + "xml += \"<"+v+">\"+get"+c+"()+\"</"+v+">\";"+ nl);
		}		
		code.append(tab + tab +"xml += \"</"+beanTag+">\";"+ nl);		
		
		code.append(tab +  tab + "return xml; "							+ nl );
		code.append(tab + " } "											+ nl+nl );


		for ( int i=0; i < types.size() ; i++ )
		{
			String v = (String)columns.get(i);
			String c = initCap(v);
			String t = (String)types.get(i);

			code.append(tab + "/** "												+ nl );
			code.append(tab + " * Set value of " + c + " attribute. "				+ nl );
			code.append(tab + " * @param " + c + " attribute type " + t + ". "		+ nl );
			code.append(tab + " * @see get" + c + "(). "							+ nl );
			code.append(tab + " */ "												+ nl );
			code.append(tab + " public void set" + c + "(" + t + " " + v + ") "		+ nl );
			code.append(tab + " { "													+ nl );
			code.append(tab + "     this." + v + "=" + v + "; "						+ nl );
			code.append(tab + " } "													+ nl+nl );
		}

		code.append(tab + "/** "												+ nl );
		code.append(tab + " * Set Xml for current Bean. " 						+ nl );
		code.append(tab + " * @see getXml()" 									+ nl );
		code.append(tab + " */ "												+ nl );
		code.append(tab + " public void setXml(String localName, CharArrayWriter contents ) "	+ nl );
		code.append(tab + " { "															+ nl );
		code.append(tab + "    // implementation goes here  "							+ nl );

		for ( int i=0; i < types.size() ; i++ )
		{
			String v = (String)columns.get(i);
			String c = initCap(v);
			String t = (String)types.get(i);
		
			code.append(tab + tab + (i>0?"else ":"") + "if ( localName.equals( \""+ v +"\" ) )" + nl );
			code.append(tab + tab + "{" );
			
			String 	contentStr = "( contents.toString() )";
			if ( t.equals("int") )			
				contentStr = "Integer.parseInt(contents.toString())";
			else if ( t.equals("long") )			
				contentStr = "Long.parseLong(contents.toString())";
			else if ( t.equals("double") )			
				contentStr = "Double.parseDouble(contents.toString())";
			else if ( t.equals("java.sql.Date") )			
				contentStr = "java.sql.Date.valueOf(contents.toString())";
				
			
			code.append(tab + tab + "set" + c + "(" + contentStr + ");" + nl );
			code.append(tab + tab + "}"+ (i==types.size()-1?";":"")+ nl );
		}		
		
		code.append(tab + " } "															+ nl +nl);

		code.append("} "+ nl );
		
		try
		{
			PrintWriter out
			= new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".java")));
			out.print(code.toString());
			out.close();
		}
		catch ( Throwable th )
		{
			th.printStackTrace();
		}
	}

	private String initCap( String s )
	{
		if ( s== null || s.length() == 0 ) return s;
		if ( s.length() > 1 ) return s.substring(0,1).toUpperCase() + s.substring(1);
		return s.substring(0,1).toUpperCase();
	}
	
		
	public void createUI()
	{
		System.out.println("CWeb Utility: BeanGen, Version 1.0");
		JLabel multilineLbl = new JLabel();
		multilineLbl.setHorizontalAlignment(SwingConstants.CENTER);		
		multilineLbl.setText("<html><b><font face='arial' color=green> Java Bean Generator</font></b></html>"); 

		okButton.addActionListener(new java.awt.event.ActionListener() 
		{
		  public void actionPerformed(ActionEvent e) 
		  {
		  	okBtn_actionPerformed(e);
		  }
		}
		);
		
		cancelButton.addActionListener(new java.awt.event.ActionListener() 
		{
		  public void actionPerformed(ActionEvent e) 
		  {
		  	cancelBtn_actionPerformed(e);
	    	System.exit(0);
		  }
		}
		);
		JPanel panel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();	
		panel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

	    c.gridwidth = GridBagConstraints.REMAINDER;	//end row
	    c.fill = GridBagConstraints.HORIZONTAL;

	    gridbag.setConstraints(multilineLbl, c);
	    panel.add(multilineLbl);

		/*
		// user name
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(usernameLabel, c);
		panel.add(usernameLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 40;
		
		gridbag.setConstraints(dbUserName, c);
		panel.add(dbUserName);


		// password
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(passwordLabel, c);
		panel.add(passwordLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;

		gridbag.setConstraints(dbPassword, c);
		panel.add(dbPassword);


		// db instance
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(instanceLabel, c);
		panel.add(instanceLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;
		
		gridbag.setConstraints(dbInstance, c);
		panel.add(dbInstance);

		*/

		// pool name
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(poolLabel, c);
		panel.add(poolLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;
		
		gridbag.setConstraints(poolName, c);
		panel.add(poolName);


		// table info
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(tableLabel, c);
		panel.add(tableLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;

		gridbag.setConstraints(tableField, c);
		panel.add(tableField);


		// bean info.
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		gridbag.setConstraints(beanLabel, c);
		panel.add(beanLabel);

		c.gridwidth = GridBagConstraints.REMAINDER; 	
		c.fill = GridBagConstraints.HORIZONTAL;
		
		gridbag.setConstraints(beanField, c);
		panel.add(beanField);


		// buttons
		c.gridwidth = GridBagConstraints.RELATIVE; 	
		c.fill = GridBagConstraints.NONE;      		//reset to default

		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(okButton, c);
		panel.add(okButton);

		c.anchor = GridBagConstraints.WEST;
		gridbag.setConstraints(cancelButton, c);
		panel.add(cancelButton);
		
		panel.setBorder(
		        BorderFactory.createCompoundBorder(
		                        BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED),"CWeb Utility",0,0,new Font("Arial",Font.BOLD,12), Color.black ),
		                        BorderFactory.createEmptyBorder(5,5,5,5)));
						
		getContentPane().add(panel);		
		
		panel.setPreferredSize(new Dimension(400,175));	
		
		addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent e) { System.exit(0);}
		});
			
	}

	/*
	public String getPassword()
	{
		char [] charArr = dbPassword.getPassword();
		StringBuffer aStringBuff = new StringBuffer(charArr.length);
		
		for (int i = 0; i < (charArr.length); i++){
		 aStringBuff.append(charArr[i]);
		}
		return aStringBuff.toString();
	}
	*/
	
	public void okBtn_actionPerformed(ActionEvent e) 
	{
		BeanGenDAO dao = new BeanGenDAO();
		dao.getMetaData(tableField.getText() ); //, dbUserName.getText(), getPassword() );
		BeanGen bg = new BeanGen();
		bg.generate( beanField.getText(), dao.types, dao.columns);
		System.out.println(beanField.getText()+".java File Is Generated.");
		JOptionPane.showMessageDialog(this, 
		beanField.getText()+".java File Is Generated.", "Bean Generator Message",JOptionPane.INFORMATION_MESSAGE);
	}

  	public void cancelBtn_actionPerformed(ActionEvent e) 
	{
		this.dispose();
	}
	//
	// inner class BeanGenDAO	
	public class BeanGenDAO 
	{
		public ArrayList types 	= new ArrayList();
		public ArrayList columns = new ArrayList();

		public BeanGenDAO()
		{
		
		}
		
		private Connection getConnection( String poolName ) throws NamingException, SQLException
		{
			/*
			//parameters , String userName, String password
	        String url = new String("jdbc:oracle:thin:" + userName + "/" +
	                                password + "@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(PORT=" +
	                                "1526)(HOST=165.107.32.105" +
	                                ")))(CONNECT_DATA=(SID=CWEBDB)))");


			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			return DriverManager.getConnection(url, userName, password);
			*/
			//DataSource ds 	= (DataSource)JNDIResourceFactory.getFactory(true).lookup( poolName );
			//return ds.getConnection();
                        return null;
		}
		
		
		
		public void getMetaData( String tableName ) //, String userName, String password)
		{
			Connection conn		= null;
			Statement stmt		= null;
			ResultSet rs	= null;
			
		    try 
			{
			  System.out.println ( "Retrieving Database Information, Please Wait...!");			  			
		      conn = getConnection(poolName.getText());
		      stmt = conn.createStatement();

		      rs = stmt.executeQuery("SELECT * FROM " + tableName );
		      ResultSetMetaData meta = rs.getMetaData();

		      int nullCount = 0;
		      int numbers = 0;
		      int columnsCount = meta.getColumnCount();
			  System.out.println ( "Database Columns ");			  
		      for (int i=1;i<=columnsCount;i++) 
			  {
				String typeName = meta.getColumnTypeName(i);
				int precision = meta.getPrecision(i);

				String 	type = "String";

				if ( typeName.equals("NUMBER") )
				{
					if ( precision >=10 )
					    type = "long";
					else
						type = "int";
				}
				else if ( typeName.equals("DATE") )
				{
				    type = "java.sql.Date";
				}
			  
			  	types.add(type);
				
				columns.add(meta.getColumnLabel(i).toLowerCase());
			  
		        System.out.println (meta.getColumnLabel(i) + "\t\t\t"
		                          + meta.getColumnTypeName(i) +"\t\t\t" + precision );

		        //if (meta.isNullable(i) == ResultSetMetaData.columnNullable)
		          //nullCount++;
		        //if (meta.isSigned(i))
		          //numbers++;
		      }
		      //System.out.println ("Columns: " + columns + " Nullable: " + nullCount + " Numeric: " + numbers);

		    }
		    catch (Exception e) 
			{
		      e.printStackTrace();
		    }
			finally
			{
				try
				{
					if ( rs != null ) rs.close();		
					if ( stmt != null ) stmt.close();
					if ( conn != null ) conn.close();
				}
				catch ( Exception ex )
				{
					//ignore
				}
			}
			
		}

	}
}