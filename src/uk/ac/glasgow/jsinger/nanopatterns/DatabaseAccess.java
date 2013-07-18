package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseAccess {

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	/** Connection to the database. Appended with the file location of the database */
	private  String connection = "jdbc:sqlite:";

	/** Driver for connecting to the database */
	private final String DRIVER = "org.sqlite.JDBC";

	/** Class to display the contents of the database and let the user choose which files to include */
	private DatabasePane dbPane;

	/** Model to hold the classes selected from the database*/
	private Model dbModel;


	/** 
	 * Constructor to initialise access to the database 
	 */
	public DatabaseAccess(File databaseLocation)
	{
		//append the locataion of the database to the connection string
		connection += databaseLocation;

		//check whether the database table exists or whether it needs created
		initDb();
	}


	/** 
	 * Checks whether the database exists, if not creates the patterns table 
	 */
	public void initDb()
	{
		Connection con = null;
		Statement stmt = null;
		try {
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			con.setAutoCommit(false);

			//create a statement to allow interatction with the database
			stmt = con.createStatement();

			//call helper method to create the columns in the table
			String sql = "CREATE TABLE IF NOT EXISTS PATTERNS " + tableSQL();

			stmt.executeUpdate(sql);
			stmt.close();

			//commit changes
			con.commit();
			con.close();
		} 
		catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}



	/**
	 * Drops any existing table and replaces it with an empty table containing
	 *  columns for storing the results of each method.
	 */
	public void resetTable()
	{
		Connection con = null;
		Statement stmt = null;
		try {
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			con.setAutoCommit(false);

			//create a statement to allow interatction with the database
			stmt = con.createStatement();

			//SQL to drop then recreate the table
			String sql = "DROP TABLE PATTERNS; " +
					"CREATE TABLE PATTERNS " + tableSQL();  

			stmt.executeUpdate(sql);
			stmt.close();

			//commit changes
			con.commit();
			con.close();

		} 
		catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}



	/** 
	 * Method to produce a String used to create the database table
	 * 
	 * @return String containing the SQL commands to generate the table
	 */
	public String tableSQL()
	{
		String table = "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
				"PACKAGE_NAME	TEXT	NOT NULL," + 
				"CLASS_NAME	TEXT	NOT NULL, " +
				"METHOD_NAME	TEXT	NOT NULL, " +
				"METHOD_DESC	TEXT	NOT NULL, " +
				"NUM_INSTRUCTIONS	INT		NOT NULL," +
				"NOPARAMS	INT		NOT NULL," +
				"VOID	INT		NOT NULL," +
				"RECURSIVE	INT		NOT NULL," +
				"SAMENAME	INT		NOT NULL," +
				"LEAF	INT		NOT NULL," +			
				"OBJECTCREATOR	INT		NOT NULL," +
				"THISINSTANCEFIELDREADER	INT		NOT NULL," +
				"THISINSTANCEFIELDWRITER	INT		NOT NULL," +
				"OTHERINSTANCEFIELDREADER	INT		NOT NULL," +
				"OTHERINSTANCEFIELDWRITER	INT		NOT NULL," +
				"STATICFIELDREADER	INT		NOT NULL," +
				"STATICFIELDWRITER	INT		NOT NULL," +
				"TYPEMANIPULATOR	INT		NOT NULL," +
				"STRAIGHTLINE	INT		NOT NULL," +		
				"LOOPER	INT		NOT NULL," +
				"SWITCHER	INT		NOT NULL," +
				"EXCEPTIONS	INT		NOT NULL," +
				"LOCALREADER	INT		NOT NULL," +
				"LOCALWRITER	INT		NOT NULL,"+		
				"ARRAYCREATOR	INT		NOT NULL," +
				"ARRAYREADER	INT		NOT NULL," +
				"ARRAYWRITER	INT		NOT NULL," +
				"POLYMORPHIC	INT		NOT NULL," +
				"SINGLERETURNER	INT		NOT NULL,"+
				"MULTIPLERETURNER	INT		NOT NULL," +
				"CLIENT	BOOLEAN		INT NULL," +
				"JDKCLIENT	INT		NOT NULL," +
				"TAILCALLER	INT		NOT NULL);" ;

		return table;
	}

	

	/**
	 * Adds new rows to the database table. Each row represents the results from the
	 * nanopattern tool of one method.
	 * 
	 * @param data datastructure holding the results to be added to the database
	 */
	public void addNewRows(Model data)
	{
		Connection con = null;
		Statement stmt = null;

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			stmt = con.createStatement();
			con.setAutoCommit(false);

			//for each class in the results
			for(int i = 0; i < data.size(); i++)
			{
				//make an arraylist to hold the current class
				ArrayList<String[]> aClass = data.getClass(i);

				//for the methods in the class
				for (int j = 0; j < aClass.size(); j++)
				{
					//array to hold the current method
					String[] method = aClass.get(j);

					String binaryInput = method[BINARY_RESULTS_STRING];
					//format the binaryResultsString, replacing spaces with commas
					//allows easy insertion to the database
					binaryInput = binaryInput.substring(0, binaryInput.length()-1);
					binaryInput = binaryInput.replaceAll(" ",",");

					//SQL statement ot add the method into the database
					String sql = "INSERT INTO PATTERNS (PACKAGE_NAME, CLASS_NAME, METHOD_NAME, METHOD_DESC, NUM_INSTRUCTIONS, NOPARAMS," + 
							"VOID, RECURSIVE, SAMENAME, LEAF, OBJECTCREATOR, THISINSTANCEFIELDREADER, THISINSTANCEFIELDWRITER," +
							" OTHERINSTANCEFIELDREADER, OTHERINSTANCEFIELDWRITER,"+
							"STATICFIELDREADER, STATICFIELDWRITER, TYPEMANIPULATOR, STRAIGHTLINE,"+
							"LOOPER, SWITCHER,EXCEPTIONS, LOCALREADER, LOCALWRITER,"+
							"ARRAYCREATOR, ARRAYREADER, ARRAYWRITER, POLYMORPHIC, SINGLERETURNER,"+
							"MULTIPLERETURNER, CLIENT, JDKCLIENT, TAILCALLER) VALUES ("+
							"'"+ method[PACKAGE_NAME]+"'," +
							"'"+method[CLASS_NAME]+"'," +
							"'"+method[METHOD_NAME]+"'," +
							"'"+method[METHOD_DESC]+"'," +
							method[NUM_INSTRUCTIONS]+"," +
							binaryInput+");";

					stmt.executeUpdate(sql);
					stmt.close();
				}
			}
			//commit changes and close
			con.commit();
			con.close();
		}
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}



	/**
	 * Creates a new database pane so the user can select the items from
	 * the database they wish to view.
	 * 
	 * @param patGui the main GUI which needs to be notified when database is selected
	 * @param theModel model to add the results from the database to
	 * @param db allows access to the database
	 */
	public void initDbSelection(PatternGUI patGui, Model theModel, DatabaseAccess db)
	{
		Connection con = null;
		Statement stmt = null;

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			stmt = con.createStatement();

			//get the package names and classs names from the database
			ArrayList<String> packages = getPackageNames(con, stmt);
			ArrayList<String> classes = getClassNames(con, stmt);

			if(packages.size() != 0)
			{
				//make a new Database pane to allow the user to select items from the datbase
				dbPane = new DatabasePane(packages, classes, patGui, theModel, db);
			}

			//close resources
			stmt.close();
			con.close();
		}
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}



	/**
	 * Allows the database to be queried and the creates a model of the results of the query.
	 * 
	 * @return Model containing the results of the query.
	 */
	public Model queryDatabase()
	{
		Connection con = null;
		Statement stmt = null;

		//model to hold the results of the query
		dbModel = new Model();

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			stmt = con.createStatement();

			//Call helper method to form the query and execute it
			ResultSet rs = stmt.executeQuery( formQuery() );			

			//for each row in the result set, make a new method
			while ( rs.next() ) {

				String[] methodResults = new String[6];

				methodResults[PACKAGE_NAME] = rs.getString("package_name");
				methodResults[CLASS_NAME] = rs.getString("class_name");
				methodResults[METHOD_NAME] = rs.getString("method_name");
				methodResults[METHOD_DESC] = rs.getString("method_desc");
				methodResults[NUM_INSTRUCTIONS] = "" + rs.getInt("num_instructions");

				//create the binary string
				String binaryResults = "";
				binaryResults += (rs.getInt("noparams")+ " ");
				binaryResults += (rs.getInt("void")+" ");
				binaryResults += (rs.getInt("recursive")+" ");
				binaryResults += (rs.getInt("samename")+" ");
				binaryResults += (rs.getInt("leaf")+" ");
				binaryResults += (rs.getInt("objectcreator")+" ");
				binaryResults += (rs.getInt("thisinstancefieldreader")+" ");
				binaryResults += (rs.getInt("thisinstancefieldwriter")+" ");
				binaryResults += (rs.getInt("otherinstancefieldreader")+" ");
				binaryResults += (rs.getInt("otherinstancefieldwriter")+" ");
				binaryResults += (rs.getInt("staticfieldreader")+" ");
				binaryResults += (rs.getInt("staticfieldwriter")+" ");
				binaryResults += (rs.getInt("typemanipulator")+" ");
				binaryResults += (rs.getInt("straightline")+" ");
				binaryResults += (rs.getInt("looper")+" ");	
				binaryResults += (rs.getInt("switcher")+" ");
				binaryResults += (rs.getInt("exceptions")+" ");
				binaryResults += (rs.getInt("localreader")+" ");
				binaryResults += (rs.getInt("localwriter")+" ");
				binaryResults += (rs.getInt("arraycreator")+" ");
				binaryResults += (rs.getInt("arrayreader")+" ");
				binaryResults += (rs.getInt("arraywriter")+" ");
				binaryResults += (rs.getInt("polymorphic")+" ");
				binaryResults += (rs.getInt("singlereturner")+" ");
				binaryResults += (rs.getInt("multiplereturner")+" ");
				binaryResults += (rs.getInt("client")+" ");
				binaryResults += (rs.getInt("jdkclient")+" ");
				binaryResults += (rs.getInt("tailcaller")+" ");

				methodResults[BINARY_RESULTS_STRING] = binaryResults;

				//add the method to the model
				dbModel.addMethod(methodResults);
			}
			//close resources
			rs.close();
			stmt.close();
			con.close();
		}
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		return dbModel;	
	}



	/**
	 * Helper method to query the database and create a list of all the packages
	 * present in the database. Used when forming the DatabasePane.
	 * 
	 * @param con the database connection
	 * @param stmt the statement to allow interaction with the database
	 * @return ArrayList<String> containing all the packages in the database
	 * @throws SQLException
	 */
	private ArrayList<String> getPackageNames(Connection con, Statement stmt) throws SQLException
	{
		stmt = con.createStatement();
		//query the database to find all the package names
		ResultSet rs = stmt.executeQuery( "SELECT DISTINCT PACKAGE_NAME FROM PATTERNS ;" );

		ArrayList<String> packages = new ArrayList<String>();
		while (rs.next())
		{
			//add each package name to the list
			packages.add(rs.getString("PACKAGE_NAME"));
		}
		rs.close();
		return packages;
	}



	/**
	 * Helper method which queries the database to retrieve a list of all the classes present.
	 * Used in building the DatabasePane
	 * 
	 * @param con the database connection
	 * @param stmt the statement to allow interaction with the database
	 * @return ArrayList<String> containing all the classes in the database
	 * @throws SQLException
	 */
	private ArrayList<String>getClassNames(Connection con, Statement stmt) throws SQLException
	{
		stmt = con.createStatement();
		//query the database to find all class names
		ResultSet rs = stmt.executeQuery( "SELECT DISTINCT CLASS_NAME FROM PATTERNS ;" );

		ArrayList<String> classes = new ArrayList<String>();
		//add results of the query to a list
		while (rs.next())
		{
			classes.add(rs.getString("CLASS_NAME"));
		}
		rs.close();
		return classes;
	}



	/**
	 * Forms an SQL query to retrieve results from the database.
	 * Gets the packages and classes the user has selected in the DatabasePane
	 * and forms the query using the selection.
	 * 
	 * @return String used to query the database
	 */
	private String formQuery()
	{
		//form the "packages" part of the query
		String query = "SELECT * FROM PATTERNS WHERE ((PACKAGE_NAME = ";

		//get the list of packages from the DatabasePane
		ArrayList<String>packages = dbPane.getPackages();
		for(int i = 0; i <packages.size(); i ++)
		{
			//if there are more packages
			if(i != packages.size()-1)
			{
				query += "'"+packages.get(i) + "' OR PACKAGE_NAME = ";
			}
			//no more packages
			else
			{
				query += "'"+packages.get(i) + "' ";
			}
		}
		query += ")";

		//get the list of classes from the DatabasePane
		ArrayList<String> classes = dbPane.getClasses();

		if (classes.size() != 0)
		{
			query += " AND (CLASS_NAME = ";
		}
		for(int i = 0; i <classes.size(); i ++)
		{
			//if there are more classes
			if(i != classes.size()-1)
			{
				query += "'"+classes.get(i) + "' OR CLASS_NAME = ";
			}
			//last of the classes
			else
			{
				query += "'"+classes.get(i) + "' ";
			}
		}
		query += "));";

		return query;
	}



	/**
	 * Returns the model produced when the database has been queried
	 * 
	 * @return Model containing the results from the database wuery
	 */
	public Model getDatabasesModel()
	{
		return dbModel;
	}

}

