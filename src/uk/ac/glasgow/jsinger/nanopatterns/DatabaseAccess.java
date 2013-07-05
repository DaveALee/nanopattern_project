package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseAccess {

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	private final String CONNECTION = "jdbc:sqlite:nanopatterns.db";
	private final String DRIVER = "org.sqlite.JDBC";

	/** Default Constructor */
	public DatabaseAccess(){}


	/**
	 * Forms a connection to the databse. Drops any existing table
	 * and replaces it with an empty table containing columns for 
	 * storing the results of each method.
	 */
	public void createTable()
	{
		Connection con = null;
		Statement stmt = null;
		try {
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(CONNECTION);

			con.setAutoCommit(false);

			System.out.println("Re-creating table");

			//create a statement to allow interatction with the database
			stmt = con.createStatement();

			//SQL to drop then recreate the table
			String sql = "DROP TABLE PATTERNS; " +
					"CREATE TABLE PATTERNS " +
					"(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
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
	 * Adds new rows to the database table. Each row represents the results from the
	 * nanopattern tool of one method.
	 * 
	 * @param data datastructure holding the results to be added to the database
	 */
	public void addNewRows(ArrayList<ArrayList<String[]>> data)
	{
		System.out.println("Adding new Rows");

		//a copy of the results of the analysis
		ArrayList<ArrayList<String[]>>dataToAdd  = data;

		Connection con = null;
		Statement stmt = null;

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(CONNECTION);

			stmt = con.createStatement();
			con.setAutoCommit(false);

			//for each class in the results
			for(int i = 0; i < dataToAdd.size(); i++)
			{
				//make an arraylist to hold the current class
				ArrayList<String[]> aClass = dataToAdd.get(i);

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
	public void queryDatabase()
	{
		System.out.println("Querying Database");

		Connection con = null;
		Statement stmt = null;

		try
		{
			//load database driver dynamically
			//and  get connection
			Class.forName(DRIVER);
			con = DriverManager.getConnection(CONNECTION);
			
			stmt = con.createStatement();

			//selects all the rows in the patterns table and prints out 
			//the meta information and some of the patterns
			ResultSet rs = stmt.executeQuery( "SELECT * FROM PATTERNS;" );
			while ( rs.next() ) {
				int id = rs.getInt("id");
				String  apackage = rs.getString("package_name");
				String  aclass = rs.getString("class_name");
				String  amethod = rs.getString("method_name");
				String  methodDesc = rs.getString("method_desc");
				int numInstrs  = rs.getInt("num_instructions");
				int tailCaller = rs.getInt("tail_caller");
				int noParams = rs.getInt("no_params");
				int isvoid = rs.getInt("void");
				int isrecursive = rs.getInt("recursive");
				int issamename = rs.getInt("same_name");
				int isleaf = rs.getInt("leaf");


				System.out.println( "ID = " + id );
				System.out.println( "PACKAGE_NAME = " + apackage );
				System.out.println( "CLASS_NAME = " + aclass );
				System.out.println( "METHOD_NAME = " + amethod );
				System.out.println( "METHOD_DESC = " + methodDesc );
				System.out.println( "NUMBER_INSTRUCTIONS = " + numInstrs );
				System.out.println( "TAIL_CALLER = " + tailCaller );
				System.out.println( "NO_PARAMS = " + noParams );
				System.out.println( "VOID = " + isvoid );
				System.out.println( "RECURSIVE = " + isrecursive );
				System.out.println( "SAME_NAME = " + issamename );
				System.out.println( "LEAF = " + isleaf );

				System.out.println();
			}

			//close resources
			rs.close();
			stmt.close();
			con.close();

			System.out.println("End of database");
		}
		catch ( Exception e ) 
		{
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}
}

