package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import javax.swing.JTextArea;

public class DatabaseAccess {

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	/** Connection to the database. Appended with the file location of the database */
	private  String connection = "jdbc:sqlite:";

	/** Driver for connecting to the database */
	private final String DRIVER = "org.sqlite.JDBC";

	/** Constructor */
	public DatabaseAccess(File databaseLocation)
	{
		//append the locataion of the database to the connection string
		connection += databaseLocation;
		
		//check whether the database table exists or whether it needs created
		initDb();
	}


	/** Checks whether the database exists, if not creates the patterns table */
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
	 * Forms a connection to the databse. Drops any existing table
	 * and replaces it with an empty table containing columns for 
	 * storing the results of each method.
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

			System.out.println("Re-creating table");

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

			System.out.println("Table created");

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
				"NO_PARAMS	INT		NOT NULL," +
				"VOID	INT		NOT NULL," +
				"RECURSIVE	INT		NOT NULL," +
				"SAME_NAME	INT		NOT NULL," +
				"LEAF	INT		NOT NULL," +			
				"OBJECT_CREATOR	INT		NOT NULL," +
				"THIS_INSTANCE_FIELD_READER	INT		NOT NULL," +
				"THIS_INSTANCE_FIELD_WRITER	INT		NOT NULL," +
				"OTHER_INSTANCE_FIELD_READER	INT		NOT NULL," +
				"OTHER_INSTANCE_FIELD_WRITER	INT		NOT NULL," +
				"STATIC_FIELD_READER	INT		NOT NULL," +
				"STATIC_FIELD_WRITER	INT		NOT NULL," +
				"TYPE_MANIPULATOR	INT		NOT NULL," +
				"STRAIGHT_LINE	INT		NOT NULL," +		
				"LOOPER	INT		NOT NULL," +
				"SWITCHER	INT		NOT NULL," +
				"EXCEPTIONS	INT		NOT NULL," +
				"LOCAL_READER	INT		NOT NULL," +
				"LOCAL_WRITER	INT		NOT NULL,"+		
				"ARRAY_CREATOR	INT		NOT NULL," +
				"ARRAY_READER	INT		NOT NULL," +
				"ARRAY_WRITER	INT		NOT NULL," +
				"POLYMORPHIC	INT		NOT NULL," +
				"SINGLE_RETURNER	INT		NOT NULL,"+
				"MULTIPLE_RETURNER	INT		NOT NULL," +
				"CLIENT	BOOLEAN		INT NULL," +
				"JDK_CLIENT	INT		NOT NULL," +
				"TAIL_CALLER	INT		NOT NULL);" ;

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
		System.out.println("Adding new Rows");

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
			for(int i = 0; i < data.getNumClasses(); i++)
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
					String sql = "INSERT INTO PATTERNS (PACKAGE_NAME, CLASS_NAME, METHOD_NAME, METHOD_DESC, NUM_INSTRUCTIONS, NO_PARAMS," + 
							"VOID, RECURSIVE, SAME_NAME, LEAF, OBJECT_CREATOR, THIS_INSTANCE_FIELD_READER, THIS_INSTANCE_FIELD_WRITER," +
							" OTHER_INSTANCE_FIELD_READER, OTHER_INSTANCE_FIELD_WRITER,"+
							"STATIC_FIELD_READER, STATIC_FIELD_WRITER, TYPE_MANIPULATOR, STRAIGHT_LINE,"+
							"LOOPER, SWITCHER,EXCEPTIONS, LOCAL_READER, LOCAL_WRITER,"+
							"ARRAY_CREATOR, ARRAY_READER, ARRAY_WRITER, POLYMORPHIC, SINGLE_RETURNER,"+
							"MULTIPLE_RETURNER, CLIENT, JDK_CLIENT, TAIL_CALLER) VALUES ("+
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

			System.out.println("New rows added");
		}
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}


	/**
	 * Allows the database to be Queried and the results displayed to the user
	 *
	 */
	public Model queryDatabase()
	{
		Connection con = null;
		Statement stmt = null;

		//model to hold the results of the query
		Model dbModel = new Model();

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(connection);

			stmt = con.createStatement();

			//TODO enable different queries
			ResultSet rs = stmt.executeQuery( "SELECT * FROM PATTERNS;" );
			while ( rs.next() ) {


				String[] methodResults = new String[6];

				methodResults[PACKAGE_NAME] = rs.getString("package_name");
				methodResults[CLASS_NAME] = rs.getString("class_name");
				methodResults[METHOD_NAME] = rs.getString("method_name");
				methodResults[METHOD_DESC] = rs.getString("method_desc");
				methodResults[NUM_INSTRUCTIONS] = "" + rs.getInt("num_instructions");

				String binaryResults = "";
				binaryResults += (rs.getInt("no_params")+ " ");
				binaryResults += (rs.getInt("void")+" ");
				binaryResults += (rs.getInt("recursive")+" ");
				binaryResults += (rs.getInt("same_name")+" ");
				binaryResults += (rs.getInt("leaf")+" ");
				binaryResults += (rs.getInt("object_creator")+" ");
				binaryResults += (rs.getInt("this_instance_field_reader")+" ");
				binaryResults += (rs.getInt("this_instance_field_writer")+" ");
				binaryResults += (rs.getInt("other_instance_field_reader")+" ");
				binaryResults += (rs.getInt("other_instance_field_writer")+" ");
				binaryResults += (rs.getInt("static_field_reader")+" ");
				binaryResults += (rs.getInt("static_field_writer")+" ");
				binaryResults += (rs.getInt("type_manipulator")+" ");
				binaryResults += (rs.getInt("straight_line")+" ");
				binaryResults += (rs.getInt("looper")+" ");	
				binaryResults += (rs.getInt("switcher")+" ");
				binaryResults += (rs.getInt("exceptions")+" ");
				binaryResults += (rs.getInt("local_reader")+" ");
				binaryResults += (rs.getInt("local_writer")+" ");
				binaryResults += (rs.getInt("array_creator")+" ");
				binaryResults += (rs.getInt("array_reader")+" ");
				binaryResults += (rs.getInt("array_writer")+" ");
				binaryResults += (rs.getInt("polymorphic")+" ");
				binaryResults += (rs.getInt("single_returner")+" ");
				binaryResults += (rs.getInt("multiple_returner")+" ");
				binaryResults += (rs.getInt("client")+" ");
				binaryResults += (rs.getInt("jdk_client")+" ");
				binaryResults += (rs.getInt("tail_caller")+" ");

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
}

