package src.uk.ac.glasgow.jsinger.nanopatterns;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import java.sql.*;

public class PatternGUI extends JFrame implements ActionListener 
{

	////////////////////  Class Constants ////////////////////

	/** Display mode constants */
	private final int HEATMAP_MODE = 0, TEXT_DISPLAY_MODE = 1;

	/** Analysis level constants */
	private final int PACKAGE_LEVEL_ANALYSIS = 0, CLASS_LEVEL_ANALYSIS = 1, METHOD_LEVEL_ANALYSIS = 2;

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	/////////////////////// JComponents ////////////////////

	/**Enclosing JFame	 */
	private JFrame topLevel;

	/** Panel for buttons */
	private JPanel userInput, filePanel, buttonsPanel;

	/** Label to provide an initial indication of the files selected */
	private JLabel init;

	/** JButton for user input */
	private JButton openFile;

	/** Menu Components */
	private JMenuBar menu;
	private JMenu fileMenu,helpMenu,db;
	private JMenuItem reset, saveResults, help, exit, addToDb, clearDb, printDb;

	/**JRadioButtons to allow switching between display modes */
	private JRadioButton heatMapMode,textMode;

	/** JRadioButtons to allow the analysis to be run at alternative levels */
	private JRadioButton packageAnalysis, classAnalysis, methodAnalysis;

	/** Area to display help text when the application is run */
	private JTextArea initText;

	/** Allows class file to be selected */
	private JFileChooser fileChooser;

	/**Panel to hold the heatmap results */
	private JScrollPane heatMapPanel;

	/** Allows the files to be displayed in a single column with multiple rows*/
	private GridLayout grid;

	/**Tabbed pane to display the results separately */
	private JTabbedPane tabs;


	///////////////////// Instance Variables //////////////////////////

	/** List of all the patterns we know about (order is important) */
	private ArrayList<String> listOfPatterns;

	/**The patternDetector */
	private PatternSpotterForGUI spotter;

	/** The files to be analysed **/
	private ArrayList<String> fileList;

	/** holds the current values of analysis level and display mode */
	private int analysisLevel, displayMode;

	/** The database object to use for storing the results of pattern analysis*/
	private DatabaseAccess database;

	/** Datastructure containing results for passing to databse */
	private ArrayList<ArrayList<String[]>> data;


	/**
	 * Constructor to initialise the GUI and make the tool ready to accept user input
	 */
	public PatternGUI(){		

		// detects patterns in specified classes
		spotter = new PatternSpotterForGUI();

		//set containing the files to be analysed
		fileList = new ArrayList<String>();	

		data = new ArrayList<ArrayList<String[]>>();

		database = new DatabaseAccess();

		//Set the default level of analysis to be class level
		analysisLevel = CLASS_LEVEL_ANALYSIS;

		//set the default display to be a heatmap
		displayMode =  HEATMAP_MODE;

		//helper method to layout components
		initDisplay();

		//helper method to make a list of all nanopattern types
		createPatternList();
	}



	//////////////////////////////////////////////////////////////
	//			Initialise GUI Components						//
	//////////////////////////////////////////////////////////////

	/**
	 * Sets up and lays out the JComponents
	 */
	private void initDisplay(){

		//create and configure the top level of the GUI
		topLevel = new JFrame();
		topLevel.setSize(850,600);
		topLevel.setTitle("Nanopattern Detector Tool");
		topLevel.setDefaultCloseOperation(EXIT_ON_CLOSE);

		//panel to hold the components related to user input
		userInput = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel();
		//holds the results if analysis mode is HeatMap
		heatMapPanel = new JScrollPane();
		//holds the results if analysis mode is Text Display
		tabs = new JTabbedPane();

		//helper method to set up the radiobuttons to select display mode
		initDisplayMode();

		//helper method to set up the radiobuttons to selece analysis level
		initAnalysisLevel();	

		//helper method to setup Jmenu and add to the frame
		initMenu();

		//helper method to set up a button and file chooser so files can be added
		initFileSelectionTools();

		//helper method to add an initial text area with help information to the GUI
		drawInitialText();

		//helper method to set up the area where selected files will be displayed
		initFileDisplay();

		topLevel.add(userInput, BorderLayout.NORTH);
		topLevel.setVisible(true);	
	}



	/**
	 * Sets up a group of radio buttons which allow the user to select
	 * the display mode (heat map or text display). Action listeners are
	 * added to the radio buttons so they respond appropriately to events.
	 */
	private void initDisplayMode() {

		//button group to hold heatMap and textDisplay radio buttons
		ButtonGroup modeSelection = new ButtonGroup();

		//Heat Map radio Button
		heatMapMode = new JRadioButton("Heat Map");
		heatMapMode.addActionListener (new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				displayMode = HEATMAP_MODE;	
				//can select the analysis level when heat map is selected
				packageAnalysis.setEnabled(true);
				classAnalysis.setEnabled(true);
				methodAnalysis.setEnabled(true);
				//alter display to show heat map of selected files
				redrawDisplay();
			}});

		//Text Display radio button
		textMode = new JRadioButton("Text Display");
		textMode.addActionListener (new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				displayMode = TEXT_DISPLAY_MODE;
				//analysis level not applicable to text display
				classAnalysis.setEnabled(false);
				packageAnalysis.setEnabled(false);
				methodAnalysis.setEnabled(false);
				//alter display to show text Display of selected files
				redrawDisplay();
			}});

		//initially set hetMap to be the selected item
		heatMapMode.setSelected(true);

		//add the radioButtons to the button panel
		modeSelection.add(heatMapMode);
		modeSelection.add(textMode);

		//make and and configure a JPanel to add the radio buttons to
		JPanel mode = new JPanel();
		mode.add(heatMapMode);
		mode.add(textMode);
		mode.setBorder(BorderFactory.createTitledBorder("Display Mode:"));
		//add the panel to the main GUI
		buttonsPanel.add(mode);
	}



	/**
	 * Sets up a group of radio buttons to allow the user to select the 
	 * analysis level - ie whether the results are displayed grouped by 
	 * package, class or method. Action listeners allow the radio buttons
	 * to respond appropriately to user input.
	 */
	private void initAnalysisLevel()
	{
		//button group to hold the 3 analysis level radio buttons
		ButtonGroup analysisLevelSelect = new ButtonGroup();

		//Package level Analysis radio button
		packageAnalysis = new JRadioButton("Package");
		packageAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){		

				analysisLevel = PACKAGE_LEVEL_ANALYSIS;
				//alter display to reflect the selected analysis level
				redrawDisplay();
			}});

		//Class level analysis radio button
		classAnalysis = new JRadioButton("Class");	
		classAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = CLASS_LEVEL_ANALYSIS;
				//alter display to reflect the selected analysis level
				redrawDisplay();
			}});

		//method level analysis radio button
		methodAnalysis = new JRadioButton("Method");
		methodAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = METHOD_LEVEL_ANALYSIS;
				//alter display to reflect the selected analysis level
				redrawDisplay();
			}});

		//initially set the tool to class level analysis
		classAnalysis.setSelected(true);			

		//add the radioButtons to the button panel
		analysisLevelSelect.add(packageAnalysis);
		analysisLevelSelect.add(classAnalysis);
		analysisLevelSelect.add(methodAnalysis);

		//make and configure a Panel, and add the radioButtons
		JPanel analysis = new JPanel();
		analysis.add(packageAnalysis);
		analysis.add(classAnalysis);
		analysis.add(methodAnalysis);	
		analysis.setBorder(BorderFactory.createTitledBorder("Analysis Level:"));

		//add the panel to the main GUI
		buttonsPanel.add(analysis);
	}



	/**
	 * Creates and adds the menubar for the top of the JFrame.
	 */
	private void initMenu()
	{
		//create a new MenuBar
		menu = new JMenuBar();
		topLevel.setJMenuBar(menu);	

		//JMenu to hold 'file' related items
		fileMenu = new JMenu("File");

		//allows the display to be reset to initial appearance
		reset = new JMenuItem ("Reset Display");
		reset.addActionListener(this);	
		fileMenu.add(reset);

		//allows the results to be exported as xml
		saveResults = new JMenuItem ("Export as xml");
		saveResults.addActionListener(this);
		fileMenu.add(saveResults);

		//allows the application to be exited
		exit = new JMenuItem ("Exit");
		exit.addActionListener(this);
		fileMenu.add(exit);
		menu.add(fileMenu);		


		//menu to allow interaction with the database
		db = new JMenu ("Database");

		//option to add current results to the database
		addToDb = new JMenuItem("Add Results");
		addToDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//add the data
				database.addNewRows(data);
			}});
		db.add(addToDb);

		//option to print out all entries of the databse
		printDb = new JMenuItem("Print Database");
		printDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){	
				//prints all rows of database
				database.queryDatabase();
			}});
		db.add(printDb);

		//option to reset the database
		clearDb = new JMenuItem("Reset Database");
		clearDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){	
				//drops table and makes it again
				database.createTable();
			}});
		db.add(clearDb);
		menu.add(db);

		//JMenu to hold 'help' related items
		helpMenu = new JMenu("Help");

		//shows rudimentary help information
		help = new JMenuItem ("NanoPatterns");
		help.addActionListener(this);
		helpMenu.add(help);
		menu.add(helpMenu);	
	}



	/**
	 * Creates a Button which will bring up a JFileChooser, allowing the
	 * user to select files they wish to analyse
	 */
	private void initFileSelectionTools()
	{
		//initialise and add the button to open files
		openFile = new JButton ("Add File");
		openFile.addActionListener(this);
		buttonsPanel.add(openFile);
		userInput.add(buttonsPanel, BorderLayout.NORTH);

		//open a file chooser at the user's home directory
		fileChooser = new JFileChooser(System.getProperty ("user.home")); 
		//allow multiple files to be selected at once
		fileChooser.setMultiSelectionEnabled(true);
		//set a filter to display only .jar and .class files
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JAR and CLASS files", "class", "jar");
		fileChooser.setFileFilter(filter);
	}



	/**
	 * Creates a Text area to be displayed upon starting or resetting the application. 
	 * TextArea holds information about using the tool. This panel is removed from the 
	 * display when the user selects files to analyse.
	 */
	private void drawInitialText()
	{
		//initialise and configure the textArea
		initText = new JTextArea();
		initText.setFont(new Font("Courier", Font.BOLD, 14));
		initText.setMargin(new Insets(10,10,10,10));
		initText.setLineWrap(true);
		initText.setWrapStyleWord(true);

		//set the help text on the display
		initText.setText("Nano Pattern Detector Tool \n\n" +
				"To begin, select the .jar or .class files you wish to analyse for nano patterns by clicking the 'Add File' button.\n\n" +
				"Files can be excluded from the analysis by deselecting the check box beside the file's name.\n\n" +
				"Use the radio buttons to display the results of the analysis either as a heat map showing the percentage" +
				" of nanopatterns present, or a textual display of the results. \n\n" +
				"The analysis can be run at package, class or method level, allowing easy comparison of the nanopatterns"+
				" present in the packages or classes selected.");

		topLevel.add(initText, BorderLayout.CENTER);
	}



	/**
	 * Initialises a JPanel to hold the files the user has selected for analysis.
	 * GridLayout is used so each file can be displayed in an element to itself,
	 * allowing aesthetically pleasing display. Number of rows in the grid can be 
	 * altered depending onn the number of files the user has selected.
	 */
	private void initFileDisplay()
	{
		//instance variable grid can be changed according to the number of files that
		//have been seleted. Initially only has space for one file
		grid = new GridLayout(1,1);

		//create and customise a panel to hold the selected files
		filePanel = new JPanel(grid);
		filePanel.setBorder(BorderFactory.createTitledBorder("Files to Include:"));

		//the initial contents of the file panel
		init = new JLabel("      (No Files Currently Selected)");

		filePanel.add(init);
		userInput.add(filePanel);
	}



	/**
	 * Creates an ArrayList of all the known nanopatterns. This is
	 * compared to the results of the analysis, and allows the display to be 
	 * formatted with pattern name information.
	 */
	private void createPatternList(){

		listOfPatterns = new ArrayList<String>();
		// each element is the name of a pattern
		// order of elements is important
		listOfPatterns.add("noparams");
		listOfPatterns.add("void");
		listOfPatterns.add("recursive");
		listOfPatterns.add("samename");
		listOfPatterns.add("leaf");
		listOfPatterns.add("objCreator");
		listOfPatterns.add("thisInstanceFieldReader");
		listOfPatterns.add("thisInstanceFieldWriter");
		listOfPatterns.add("otherInstanceFieldReader");
		listOfPatterns.add("otherInstanceFieldWriter");
		listOfPatterns.add("staticFieldReader");
		listOfPatterns.add("staticFieldWriter");
		listOfPatterns.add("typeManipulator");
		listOfPatterns.add("straightLine");
		listOfPatterns.add("looper");
		listOfPatterns.add("switcher");
		listOfPatterns.add("exceptions");
		listOfPatterns.add("localReader");
		listOfPatterns.add("localWriter");
		listOfPatterns.add("arrCreator");
		listOfPatterns.add("arrReader");
		listOfPatterns.add("arrWriter");
		listOfPatterns.add("polymorphic");
		listOfPatterns.add("singleReturner");
		listOfPatterns.add("multipleReturner");
		listOfPatterns.add("client");
		listOfPatterns.add("jdkClient");
		listOfPatterns.add("tailCaller");
	}



	/////////////////////////////////////////////////////////////////
	//					Action Performed                           //
	/////////////////////////////////////////////////////////////////

	/**
	 * Listens for events generated by the 'open file' button, the 'reset display'
	 * menu item, the 'save results' menu item, the 'exit' menu item and the 
	 * 'help' menu item. Initiates appropriate actions  when event is received.
	 */
	public void actionPerformed (ActionEvent event)
	{
		//User has clicked the button to add files
		//Get the selected files and call methods to analyse and display results.
		if(event.getSource() == openFile)
		{
			//user chooses a file
			if (fileChooser.showOpenDialog(userInput) == JFileChooser.APPROVE_OPTION) {

				//array of selected files (user is able to select more than one at once)
				File[] selectedFiles = fileChooser.getSelectedFiles();

				//for every element in the array of selected files
				for(int i = 0; i < selectedFiles.length; i++)
				{
					//check whether the file is a .jar or .class
					if(checkFileValid(""+  selectedFiles[i]))
					{
						//file is of valid type
						//add this file to the fileList
						fileList.add(""+  selectedFiles[i]);
					}
					//file is not a .jar or .class file
					else
					{
						JOptionPane.showMessageDialog(topLevel, "Files must be .jar or .class",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				//helper method to show the selected files on the GUI
				displayFilesToAnalyse();

				//helper method to analyse the selected files and update the display
				redrawDisplay();

			}
		}
		//reset menu item has been selected
		//reset the GUI to initial settings by removing any results,
		//and adding components to return the GUI to initial settings
		else if (event.getSource() == reset)
		{
			tabs.setVisible(false);
			heatMapPanel.setVisible(false);
			filePanel.removeAll();
			filePanel.add(init);
			topLevel.add(initText, BorderLayout.CENTER);
			fileList.clear();
			grid.setRows(1);

			//dynamically remove components
			filePanel.revalidate();
			validate();
		}

		//export as xml menu item has been selected
		else if (event.getSource() == saveResults)
		{
			//call helper method to output the results in xml form
			printXML();
		}

		//exit menu item is selected, exit the application
		else if (event.getSource() == exit)
		{
			System.exit(0);
		}
		//show basic help info
		//@TODO expand this
		else if (event.getSource() == help)
		{
			JOptionPane.showMessageDialog(topLevel, "This tool detects fundamental nanopatterns in java bytecode\n" +
					"class files. See the paper at \n"+
					"http://www.dcs.gla.ac.uk/~jsinger/pdfs/nanopatterns.pdf \nfor more details about nano-patterns.",
					"Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}



	/**
	 * Analyses all the files in the fileList, then if the display mode is 
	 * HEATMAP, calls a helper method to format these results according to 
	 * the selected analysis level. If the display mode is TEXT_DISPLAY, calls
	 * a helper method to print the results. Also configures the GUI to allow the
	 * new components to be added by removing existing components.
	 */
	private void redrawDisplay()
	{
		//convert the fileSet to an array which is compatible with the detector tool
		String[] args = fileList.toArray(new String[0]);

		//check to see if there is a file to be analysed before analysing
		if (!fileList.isEmpty())
		{
			data.clear();

			//use the PatternSpotter to analyse the file(s) and detect patterns
			ArrayList<ArrayList<String[]>> results = spotter.detect(args);	


			//copy of results for the database
			data = results;

			//TODO
			//database.createTable();
			//database.addNewRows(results);
			//database.queryDatabase();


			if(displayMode == HEATMAP_MODE)
			{		
				//modify the data structure holding the results of the analysis
				//as required by the analysis level selected
				results = formatAnalysisLevel(results);

				//configure display to allow new results to be displayed
				tabs.setVisible(false);
				topLevel.remove(initText);
				topLevel.remove(heatMapPanel);

				//generate a heat map using the results of the analysis
				makeHeatMap(results);
				repaint();
			}

			else if(displayMode == TEXT_DISPLAY_MODE)
			{
				//configure display to allow new results to be displayed
				topLevel.remove(initText);
				topLevel.remove(heatMapPanel);

				//generate the text display using the results of the analysis
				printResults(results);
				repaint();		
			}
		}
	}



	/**
	 * Checks whether the files selected are an acceptable type for
	 * analysis (ie .jar or .class).
	 * 
	 * @param fileName the name of the file to be checked
	 * @return true if the file is a valid type
	 */
	private boolean checkFileValid(String fileName)
	{
		if (fileName.endsWith(".jar") || fileName.endsWith(".class"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}



	//////////////////////////////////////////////////////////////////
	//						View Selected Files			            //
	//////////////////////////////////////////////////////////////////

	/**
	 * Displays the selected classes to the user by creating a JCheckBox
	 * for each element in the file List. Adds an action listener to each
	 * of these check boxes so that when the checkbox is deselected, the 
	 * associated file is removed from the list of files.
	 */
	private void displayFilesToAnalyse()
	{
		//reset the panel to avoid duplicate display
		filePanel.removeAll();

		//iterator to enable the set of file paths to be output
		Iterator<String> it = fileList.iterator();

		//the number rows relating to the number of files to be analysed
		int row=0;		

		//use the iterator to traverse the List
		while (it.hasNext())
		{
			final String curr = it.next();

			//create a checkbox for every selected file			
			final JCheckBox file = new JCheckBox(curr);

			//anonymous inner class to handle action events
			//associated with the selection of which files should be 
			//analysed
			file.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//add to the set if selected
					if (file.isSelected())
					{	
						fileList.add(curr);
						redrawDisplay();
					}
					//remove from the set if deselected
					else{fileList.remove(curr);
					redrawDisplay();}							
				}});			

			//initally set the file to be selected
			file.setSelected(true);

			//increase the size of the grid
			//allows the file paths to be displayed in multiple rows
			grid.setRows(row+1);

			//add the checkbox and file desc to the display
			filePanel.add(file, row,0);		
			row++;
		}		
		//allow the display to be dynamically updated
		filePanel.revalidate();
		validate();
	}



	//////////////////////////////////////////////////////////
	//				Heat Map Display                        //
	//////////////////////////////////////////////////////////

	/**
	 * 
	 * 
	 * @param listToAnalyse
	 */
	private void makeHeatMap(ArrayList<ArrayList<String[]>> listToAnalyse)
	{
		//make an array for the x axis containing the pattern names
		//is the same regardless of the analysis level
		Object[]patterns = listOfPatterns.toArray();

		//make an array to hold the labels for the y axis. How the array is 
		//generated varies depending on the analysis level.
		Object[] yAxisItems;

		//make the 2D array required by the heat map
		double[][] heatMapInput;

		if(analysisLevel == PACKAGE_LEVEL_ANALYSIS || analysisLevel == CLASS_LEVEL_ANALYSIS)
		{
			//prepare the double[][] required as input to the heat map
			heatMapInput = new double[listToAnalyse.size()][listOfPatterns.size()];

			//make the array to hold the y axis labels
			yAxisItems = getYAxisNames(listToAnalyse);

			// for each class, interpret the results of the analysis
			for(int i = 0; i < listToAnalyse.size(); i++)
			{ 
				//find the number of methods in the class to allow calculation of average
				int numMethodsInAnalysis = listToAnalyse.get(i).size();

				// holds the number of each type of pattern in the target package/class
				int[] numPatternsInAnalysis = new int[listOfPatterns.size()];

				//for each method in the class/package, decide whether it contains each nanopattern or not
				for (int j = 0; j < numMethodsInAnalysis; j++)
				{			
					//the full results of the analysis of one method
					String[] methodInfo = listToAnalyse.get(i).get(j);

					String [] binaryResults = methodInfo[BINARY_RESULTS_STRING].split(" +");

					//for each binary digit of the method results
					//increase frequency of pattern in class if present
					for(int k = 0; k < binaryResults.length; k++)
					{
						//will be a 1 or 0 representing a pattern's presence in the method
						String pattern = binaryResults[k];

						if (pattern.equals("1"))
						{
							//increase the number of patterns found
							numPatternsInAnalysis[k]++;
						}
					}

					//one class' results. Each nanopattern has a number between 0 and 1 repepresenting
					//the percentage of methods in the class exhibiting that pattern
					heatMapInput[i] = calcPercentPatterns(numPatternsInAnalysis, numMethodsInAnalysis);
				}
			}

		}
		//Operate on the methods
		//average does not need to be found as the pattern is either present or not present,
		//so simply use the 1 or 0 from the results to provide input to the heat map
		else
		{
			//the number of items in the y axis is the number of methods
			//x axis is made up of the names of each pattern
			heatMapInput = new double[listToAnalyse.get(0).size()][listOfPatterns.size()];

			//the methods are all held in the first element of the analysis data structure 
			ArrayList<String[]> allMethods = listToAnalyse.get(0);

			//array to hold the y axis values
			yAxisItems = new Object[allMethods.size()];

			//split for each method
			for(int i = 0; i < allMethods.size(); i++)
			{
				//the results from the analysis of a single method
				String[] methodInfo = allMethods.get(i);

				//add the current methods name to the array holding y axis values
				yAxisItems[i] = methodInfo[METHOD_NAME];

				//get the string representing the presence of patterns for this method
				String [] binaryResults = methodInfo[BINARY_RESULTS_STRING].split(" +");

				//creates an array where the 1 or 0 for each pattern has a separate element
				double[] methodResults = new double[binaryResults.length];

				//translate the String result to a Double so it can be added to the heat map input
				for(int j = 0; j < binaryResults.length; j++)
				{
					methodResults[j] = Double.parseDouble(binaryResults[j]);
				}
				//add the array containing the patterns for this method to the heat map input
				heatMapInput[i] = methodResults;
			}
		}

		//create the heat map using heatMapInput (z values), patterns (x axis) and 
		//class/package/method names (y axis)
		HeatMap map = new HeatMap(heatMapInput, patterns, yAxisItems);

		//display the heatmap by adding it to a panel which is then added to the GUI
		heatMapPanel = map.displayHeatMap();
		topLevel.add(heatMapPanel, BorderLayout.CENTER);			

		//dynamically update components
		topLevel.revalidate();
		validate();
	}



	/**
	 * Finds the elements for the y axis of the heat map. Depending on the 
	 * analysis level, the y axis will be either package names or class names.
	 * 
	 * @param analysisList the datastructure the y axis values will be extracted from
	 * @return an array containing the elements to be used on the y axis of the heat map
	 */
	private Object[] getYAxisNames(ArrayList<ArrayList<String[]>> analysisList)
	{
		//array to hold the y axis values
		Object[] yAxis = new Object[analysisList.size()];

		for(int i = 0; i < yAxis.length; i++ )
		{
			//the first element in any class will contain the y axis value
			ArrayList<String[]> a = analysisList.get(i);

			if(a.size() == 0)
			{
				yAxis[i] ="empty";
				continue;
			}

			String[] aYAxisElement = a.get(0);	

			if(analysisLevel == PACKAGE_LEVEL_ANALYSIS)
			{
				//y axis values are package names
				yAxis[i] = aYAxisElement[PACKAGE_NAME];
			}
			else if (analysisLevel == CLASS_LEVEL_ANALYSIS)
			{
				//y axis values are class names
				yAxis[i] = aYAxisElement[CLASS_NAME];
			}
		}
		return yAxis;
	}


	/**
	 * Calculates the percentage of each nanopattern present by dividing the 
	 * total number of times each pattern occurs in the package/class by the
	 * number of methods in the package/class. Provides the z values for the heat
	 * map which translate to colour intensity.
	 * 
	 * @param numPatterns the number of each type of nanopattern (numerator) 
	 * @param numMethodsInClass the number of methods in the class (denominator)
	 * @return an array containing numbers between 0 and 1 represesnting the 
	 * percentage of patterns present.
	 */
	private double[] calcPercentPatterns(int[] numPatterns, int numMethodsInClass)
	{
		// array to hold the percentages of each pattern present
		double[] percentPatterns = new double [numPatterns.length];

		for (int i =0;  i< numPatterns.length; i++)
		{
			double patternNumber = (double) numPatterns[i];

			//calculate the percentage of patterns
			percentPatterns[i] = patternNumber / (double) numMethodsInClass;
		}
		return percentPatterns;
	}



	/**
	 * Transforms the results of the analysis to the format required
	 * by the analysis level that is selected. An ArrayList<ArrayList<String[]>> is
	 * passed in, the elements are changed according to the requested analysis level
	 * and an ArrayList<ArrayList<String[]>> is returned to be displayed. 
	 * 
	 * For PACKAGE_LEVEL_ANALYSIS, iterate over the list of classes in the results 
	 * data strucute and check the package name of each class. If the package has not been seen,
	 * add it to a new ArrayList<ArrayList<String[]>>. If the package has been seen, add the 
	 * methods associated with the class to the ArrayList associated with that package.
	 * 
	 * For METHOD_LEVEL_ANALYSIS add all the methods from all the classes to a new data structure
	 * where these methods are held in the first element and so acting as if they belong to a 
	 * single class.
	 * 
	 * For CLASS_LEVEL_ANALYSIS, simply return the data structure without modification as the
	 * results are already in the required format.
	 * 
	 * @param results The initial results of the analysis
	 * @return results formatted correctly for the analysis level selected
	 */
	private ArrayList<ArrayList<String[]>> formatAnalysisLevel(ArrayList<ArrayList<String[]>> results)
	{
		//Decide which level of analysis is required and respond appropriately
		if(analysisLevel == PACKAGE_LEVEL_ANALYSIS) 
		{
			//data structure to hold the list of methods belonging to all the classes in a package
			ArrayList<ArrayList<String[]>> packages = new ArrayList<ArrayList<String[]>>();

			//examine the results for each class
			for(int i = 0; i < results.size(); i ++)
			{
				//the class to be assigned a package
				ArrayList<String[]> nextClass = results.get(i); 

				//the first method in a class will give its packagename
				String packageName = "";

				if(nextClass.size() == 0)
				{
					packageName ="empty";

				}
				else{
					String[] method = nextClass.get(0);
					//packageName is the package the current class belongs to
					packageName = method[PACKAGE_NAME];	
				}

				//if no classes have been assigned packages yet,
				//the first class is always in a new package
				if(packages.size() == 0)
				{
					packages.add(nextClass);
				}
				else
				{
					//we have already identified package(s)
					//check if the package this class belongs to has already been seen
					boolean found = false;

					for(int j = 0; j< packages.size(); j++)
					{
						ArrayList<String[]> aPackage = packages.get(j);

						String thisPackageName;

						if(aPackage.size() == 0)
						{
							thisPackageName = "empty";
						}
						else
						{
							//get the name of the next package in the list of seen packages
							String[] aMethod = aPackage.get(0);
							thisPackageName = aMethod[0];
						}
						//package already exists in the list
						if (thisPackageName.equals(packageName))
						{
							//add the classes results to the element containing results
							//from the same package
							packages.get(j).addAll(nextClass);
							found = true;
							break;
						}
					}
					//package has not been previously seen
					if(!found)
					{
						//make a new entry for the package
						packages.add(nextClass);	
					}
				}
			}	
			//return ArrayList<ArrayList<String[]>> where each element in the top
			//level ArrayList is a package.
			return packages;
		}
		//method level analysis.
		else if (analysisLevel == METHOD_LEVEL_ANALYSIS)
		{
			//List to hold all the methods. Behaves like all the methods
			//belong to a single class
			ArrayList<String[]> methods = new ArrayList<String[]>();

			//remove class information
			for(int i = 0; i < results.size(); i++)
			{
				//get the class
				ArrayList<String[]> currentClass = results.get(i);

				//add all the methods in the class to the higher level list
				//thereby removing class information
				methods.addAll(currentClass);	
			}

			// methodLevelResults will hold an arrayList with only one entry containing all the methods
			//from all the classes
			ArrayList<ArrayList<String[]>> methodLevelResults = new ArrayList<ArrayList<String[]>>();

			//add the ArrayList containing results from the methods to the higher level ArrayList
			//so the results are in the form needed by the display
			methodLevelResults.add(methods);

			return methodLevelResults;
		}
		// mode will be CLASS_LEVEL_ANALYSIS. Results are already in the correct format,
		//so just return them as they are
		else  
		{
			return results;
		}
	}



	////////////////////////////////////////////////////////////////
	//						Tabbed Display						  //
	////////////////////////////////////////////////////////////////

	/**
	 *  Produces the output of the analysis in the form of a tabbed interface
	 *  with a tab for every class analysed.
	 * 
	 * @param results arrayList containing a the classes analysed
	 */
	private void printResults(ArrayList<ArrayList<String[]>> results) 
	{
		tabs.removeAll();
		tabs.setVisible(true);		

		//for each class analysed
		for(int i = 0; i < results.size();i++)
		{
			//get the list of classes
			ArrayList<String[]> fileResults = results.get(i);

			// make a new textArea to be used as a tab
			JTextArea textPanel = new JTextArea();

			//fixed width font to ease alignment
			textPanel.setFont(new Font("Courier", Font.BOLD, 14));

			//layout and configuretext area
			Border resultsBorder = BorderFactory.createLineBorder(Color.BLACK);
			textPanel.setBorder(BorderFactory.createCompoundBorder(
					resultsBorder, BorderFactory.createEmptyBorder(10,10,10,10)));
			JScrollPane scrollPane = new JScrollPane(textPanel); 
			textPanel.setEditable(false);

			//get the list of methods for current class
			for(int j=0; j < fileResults.size(); j++)
			{

				//the results for one method
				String [] method = fileResults.get(j);

				//add class name
				if (j == 0)
				{		
					//tooltip (method[0]) shows the package
					tabs.addTab(method[CLASS_NAME], null, scrollPane, method[PACKAGE_NAME] );	
				}
				//get and display results as a string
				textPanel.append(getMethodInfo(method));	
			}
			//set to top of results
			textPanel.setCaretPosition(0);
		}
		//add the tabs
		topLevel.add(tabs);
		//allow dynamic adding
		topLevel.revalidate();
		validate();
	}



	/**
	 * controls how the results of the analysis are displayed
	 * @param methodResults an array containing the results of the analysis on a single method
	 */
	public String getMethodInfo(String[] methodResults){

		StringBuilder sb = new StringBuilder();
		String results = "";

		//String[] methodResults will be in the following format:
		//[0] = package
		//[1] = class
		//[2] = method name
		//[3] = method desc
		//[4] = num Instructions
		//[5] = binary String of patterns present

		//displays the method name
		sb.append(String.format("Method Name: %-15s\n", methodResults[METHOD_NAME]));

		sb.append(String.format("Method Desc: %-15s\n", methodResults[METHOD_DESC]));

		sb.append(String.format("Num Instructions: %-15s\n", methodResults[NUM_INSTRUCTIONS]));


		//split the binary string into an array of 1s and 0s representing the presence of nanopatterns
		String[] binary = methodResults[BINARY_RESULTS_STRING].split(" +");

		//go through the binary array of nanopatterns
		for (int i = 0; i < binary.length; i++)
		{
			//nanopattern type is included in display. The index a new type begins at 
			//is shown below
			if (i == 0)	{sb.append("\nCalling Patterns: ");}
			if (i == 5)	{sb.append("\nObject-Orientation Patterns: ");}
			if (i == 13)	{sb.append("\nControl Flow Patterns: ");}
			if (i == 17)	{sb.append("\nData Flow Patterns: ");}
			if (i == 22)	{sb.append("\nOther Patterns: ");}

			//check whether the pattern at the index is a 1 (ie present)
			String pattern = binary[i];
			if (pattern.equals("1"))
			{
				//if so print the associated pattern from the list of patterns
				sb.append(listOfPatterns.get(i) + " ");
			}

		}
		sb.append("\n---------------------------------------------------------------\n");

		results = sb.toString();

		return results;
	}



	///////////////////////////////////////////////////////////////
	//						XML Output							 //
	///////////////////////////////////////////////////////////////

	/**
	 * Takes the results of the analysis, adds xml tags and outputs the results to a file
	 * @param list the results of the analysis
	 */
	private void printXML ()
	{
		//convert the fileSet to an array which is compatible with the detector tool
		String[] args = fileList.toArray(new String[0]);

		//check to see if there is a file to be analysed
		if (fileList.isEmpty())
		{
			JOptionPane.showMessageDialog(topLevel, "At least one file must be selected\n" +
					"in order to export as XML.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}	
		else
		{
			//run the detection
			ArrayList<ArrayList<String[]>> list = spotter.detect(args);

			//string builder to hold the xml String as it is created
			StringBuilder sb = new StringBuilder();


			sb.append("<analysis>");
			for (int i = 0; i < list.size(); i++)
			{
				ArrayList<String[]>thisClass = list.get(i);

				//format the classname
				String[] firstMethod = thisClass.get(0);
				String className = escapeChars(firstMethod[1]);


				sb.append("\n\t<class name =\""+className+"\">");
				//format xml for all methods in the class
				for(int j = 0; j< thisClass.size(); j++)
				{
					String[] thisMethod = thisClass.get(j);
					//print the method name description and number of instructions
					sb.append("\n\t\t<method name = \""+ escapeChars(thisMethod[METHOD_NAME]) + "\" desc = \""
							+thisMethod[METHOD_DESC] + "\" numinstr = \"" + thisMethod[NUM_INSTRUCTIONS] + "\">");

					//print out the binary string of nanopatterns
					sb.append("\n\t\t\t"+thisMethod[BINARY_RESULTS_STRING]);

					sb.append("\n\t\t</method>");	
				}

				sb.append("\n\t</class>");
			}
			sb.append("\n</analysis>");

			String output = sb.toString();

			//write the xml to a file
			FileWriter fw = null;
			try{
				try 
				{
					fw = new FileWriter("analysis.xml");
					fw.write(output);	

					JOptionPane.showMessageDialog(topLevel, "analysis.xml created successfully",
							"Success", JOptionPane.INFORMATION_MESSAGE);
				} 
				finally
				{
					if(fw != null)
						fw.close();
				}
			}
			catch (IOException e) {

				e.printStackTrace();
			}
		}
	}



	/**
	 * For the xml output. Remove illegal characters & replace with xml valid equivalents
	 * 
	 * @param input the file name to be formatted
	 * @return a String with illegal characters removed
	 */
	private String escapeChars(String input)
	{
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");

		return input;
	}


}

