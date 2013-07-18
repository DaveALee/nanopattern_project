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
	private JMenuItem reset, saveResults, printHeatMap, printTextDisplay, help, exit, newDb, openDb, addToDb, clearDb;

	/**JRadioButtons to allow switching between display modes */
	private JRadioButton heatMapMode,textMode;

	/** JRadioButtons to allow the analysis to be run at alternative levels */
	private JRadioButton packageAnalysis, classAnalysis, methodAnalysis;

	/** Area to display help text when the application is run */
	private JTextArea initText;

	/** Allows class file to be selected */
	private JFileChooser fileChooser, fileSaver;

	/** Allows the files to be displayed in a single column with multiple rows*/
	private GridLayout grid;

	private HeatMap map;
	private TextDisplay display;

	private JPanel mainPanel;


	///////////////////// Instance Variables //////////////////////////


	/** The files to be analysed **/
	private ArrayList<String> fileList;

	/** holds the current values of analysis level and display mode */
	private int analysisLevel, displayMode;

	/** The database object to use for storing the results of pattern analysis*/
	DatabaseAccess database;

	/**Holds the filepath for the currently selected database */
	private File currentDatabase;

	/** The model to hold the results of nanopattern analysis */
	Model theModel;

	/**
	 * Constructor to initialise the GUI and make the tool ready to accept user input
	 */
	public PatternGUI(){		

		//model to analyse and hold results
		theModel = new Model();

		//set containing the files to be analysed
		fileList = new ArrayList<String>();	

		//Set the default level of analysis to be class level
		analysisLevel = CLASS_LEVEL_ANALYSIS;

		//set the default display to be a heatmap
		displayMode =  HEATMAP_MODE;

		//no database is initially selected
		currentDatabase = null;

		//helper method to layout components
		initDisplay();		
	}



	//////////////////////////////////////////////////////////////
	//			Initialise GUI Components						//
	//////////////////////////////////////////////////////////////

	/**
	 * Sets up and lays out the JComponents
	 */
	private void initDisplay(){

		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);

		//create and configure the top level of the GUI
		topLevel = new JFrame();
		topLevel.setSize(850,600);
		topLevel.setTitle("Nanopattern Detector Tool");
		topLevel.setDefaultCloseOperation(EXIT_ON_CLOSE);

		//panel to hold the components related to user input
		userInput = new JPanel(new BorderLayout());
		buttonsPanel = new JPanel();

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
				analyseFiles();
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
				analyseFiles();
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
				analyseFiles();
			}});

		//Class level analysis radio button
		classAnalysis = new JRadioButton("Class");	
		classAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = CLASS_LEVEL_ANALYSIS;
				//alter display to reflect the selected analysis level
				analyseFiles();
			}});

		//method level analysis radio button
		methodAnalysis = new JRadioButton("Method");
		methodAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = METHOD_LEVEL_ANALYSIS;
				//alter display to reflect the selected analysis level
				analyseFiles();
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

		//allows the heat map to be saved
		printHeatMap = new JMenuItem("Save Heat Map");
		printHeatMap.addActionListener(this);
		fileMenu.add(printHeatMap);

		//allows the text display to be saved
		printTextDisplay = new JMenuItem("Save Text Output");
		printTextDisplay.addActionListener(this);
		fileMenu.add(printTextDisplay);

		//allows the application to be exited
		exit = new JMenuItem ("Exit");
		exit.addActionListener(this);
		fileMenu.add(exit);
		menu.add(fileMenu);	

		//menu to allow interaction with the database
		db = new JMenu ("Database");

		//allows a new database to be created
		newDb = new JMenuItem("New Database");
		newDb.addActionListener(new ActionListener(){	

			public void actionPerformed(ActionEvent e) {
				JFileChooser newDb = new JFileChooser("user.dir");
				newDb.setSelectedFile(new File("nanopatterns.db"));

				if (newDb.showSaveDialog(topLevel) == JFileChooser.APPROVE_OPTION) {
					File newDatabase = newDb.getSelectedFile();
					database = new DatabaseAccess (newDatabase);
					//set the current database to the newly created db
					currentDatabase = newDatabase;
					displayFilesToAnalyse();
				}}});
		db.add(newDb);

		//option open a database
		openDb = new JMenuItem("Open Database");
		openDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser openDb = new JFileChooser("user.dir");
				openDb.setFileFilter(new FileNameExtensionFilter("database files (.db)", "db"));
				//user chooses a file	
				if (openDb.showOpenDialog(topLevel) == JFileChooser.APPROVE_OPTION) {
					currentDatabase = openDb.getSelectedFile();

					// create a DatabaseAccess object to allow the database to be accessed
					database = new DatabaseAccess(currentDatabase);	

					//initialise the GUI to allow classes to be selected from the database
					database.initDbSelection(PatternGUI.this, theModel, database);	

					//show the selected files + database
					displayFilesToAnalyse();
				}}});
		db.add(openDb);

		//option to add current results to the database
		addToDb = new JMenuItem("Add Results");
		addToDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (hasDbConnection())
				{
					//add the data
					database.addNewRows(theModel);
				}
			}});
		db.add(addToDb);

		//option to reset the database
		clearDb = new JMenuItem("Reset Database");
		clearDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){	

				if(hasDbConnection())
				{
					//drops table and makes it again
					database.resetTable();
				}
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
		fileChooser.setFileFilter(new FileNameExtensionFilter("JAR and CLASS files", "class", "jar"));
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
		initText.setSize(820,420);
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

		initText.setEditable(false);
		mainPanel.add(initText);		

		JScrollPane scroll = new JScrollPane(mainPanel, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,   
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		topLevel.add(scroll, BorderLayout.CENTER);
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
				analyseFiles();

			}
		}
		//reset menu item has been selected
		//reset the GUI to initial settings by removing any results,
		//and adding components to return the GUI to initial settings
		else if (event.getSource() == reset)
		{
			//tabs.setVisible(false);
			mainPanel.removeAll();
			filePanel.removeAll();

			filePanel.add(init);
			mainPanel.add(initText);

			fileList.clear();
			grid.setRows(1);

			//dynamically remove components
			filePanel.revalidate();
			validate();

			//reset database access
			currentDatabase = null;
			database = null;

			//reset model
			theModel = new Model();
		}

		//export as xml menu item has been selected
		else if (event.getSource() == saveResults)
		{

			//convert the fileSet to an array which is compatible with the detector tool
			String[] args = fileList.toArray(new String[0]);

			//check to see if there is a file to be analysed before analysing
			if (!fileList.isEmpty())
			{	
				fileSaver = new JFileChooser(System.getProperty("user.home"));
				//fileSaver.setApproveButtonText("Save");
				fileSaver.setSelectedFile(new File("nanopattern_analysis.xml"));

				theModel = new Model();

				//generate the dataStructure
				theModel.createModel(args);

				XMLOutput xml = new XMLOutput(theModel);

				//user chooses a file
				if (fileSaver.showSaveDialog(userInput) == JFileChooser.APPROVE_OPTION) 
				{
					if(fileSaver.getSelectedFile()!=null)
					{  
						File theFileToSave = fileSaver.getSelectedFile();						

						if(xml.save(theFileToSave));{
							JOptionPane.showMessageDialog(topLevel, theFileToSave +" created successfully",
									"Success", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
			else
			{
				JOptionPane.showMessageDialog(topLevel, "No Files Selected",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (event.getSource() == printHeatMap)
		{
			if (map != null)
			{
				fileSaver = new JFileChooser(System.getProperty("user.home"));
				//fileSaver.setApproveButtonText("Save");
				fileSaver.setSelectedFile(new File("heat_chart.png"));

				//user chooses a file
				if (fileSaver.showSaveDialog(userInput) == JFileChooser.APPROVE_OPTION) 
				{
					if(fileSaver.getSelectedFile()!=null)
					{  
						File theFileToSave = fileSaver.getSelectedFile();

						System.out.println("" + theFileToSave);

						//pass the selected file saving location to the heat map
						map.save(theFileToSave);

					}
				}
			}

		}
		else if (event.getSource() == printTextDisplay)
		{
			if(display != null)
			{
				fileSaver = new JFileChooser(System.getProperty("user.home"));
				//fileSaver.setApproveButtonText("Save");
				fileSaver.setSelectedFile(new File("nanopattern_analysis.txt"));

				//user chooses a file
				if (fileSaver.showSaveDialog(userInput) == JFileChooser.APPROVE_OPTION) 
				{
					if(fileSaver.getSelectedFile()!=null)
					{  
						File theFileToSave = fileSaver.getSelectedFile();

						System.out.println("" + theFileToSave);

						//pass the selected file saving location to the heat map
						display.save(theFileToSave);
					}
				}
			}
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
	 * Analyses all the files in the fileList, and calls a helper method to 
	 * display the model according to the display mode.
	 */
	private void analyseFiles()
	{
		//convert the fileSet to an array which is compatible with the detector tool
		String[] args = fileList.toArray(new String[0]);		

		//check to see if there is a file to be analysed before analysing
		if (!fileList.isEmpty() || currentDatabase != null)
		{	
			theModel = new Model();

			//generate the dataStructure
			theModel.createModel(args);

			//displays the view, database has not been modified
			displayView(false);
		}
	}

	

	/**
	 * Displays the model according to the selected display mode
	 * 
	 * @param dbModified true if the database has been modified
	 */
	public void displayView(boolean dbModified)
	{		
		//if the database has not been modified, add it to the model.
		//if the database has been modified, it has already been added to the model,
		//so do nothing.
		if (!dbModified)
		{
			if(database != null)
			{
				if(database.getDatabasesModel() != null)
				{
					theModel.appendModel(database.getDatabasesModel());
				}
			}
		}
		//clear the panel in preparation of new results being displayed
		mainPanel.removeAll();

		if(displayMode == HEATMAP_MODE)
		{		
			//modify the data structure holding the results of the analysis
			//as required by the analysis level selected
			formatAnalysisLevel();

			if(theModel.size() != 0)
			{
				//generate a heat map using the results of the analysis
				map = new HeatMap(theModel);				

				mainPanel.add(map.displayView());
			}
		}
		else if(displayMode == TEXT_DISPLAY_MODE)
		{
			//generate the text display using the results of the analysis
			display = new TextDisplay(theModel);
			mainPanel.add(display.displayView());	
		}
		mainPanel.revalidate();
		mainPanel.repaint();
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

	
	/**
	 * Check whether there is an active database
	 * @return true if there is an active database
	 */
	private boolean hasDbConnection()
	{
		if(database ==null)
		{
			JOptionPane.showMessageDialog(topLevel, "No Database Active",
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;

		}
		else
		{
			return true;
		}

	}

	
	//////////////////////////////////////////////////////////////////
	//						View Selected Files			            //
	//////////////////////////////////////////////////////////////////

	//TODO tidy this up a bit
	/**
	 * Displays the selected classes and the database to the user by creating a
	 * JCheckBox for each element in the file List. Adds an action listener to each
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
						analyseFiles();
					}
					//remove from the set if deselected
					else{fileList.remove(curr);
					analyseFiles();}							
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
		//display the currently selected database
		if (currentDatabase != null)
		{
			//create a checkbox for every selected file			
			final JCheckBox file = new JCheckBox(currentDatabase.getAbsolutePath());

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

						currentDatabase = new File(file.getText());
						database = new DatabaseAccess(currentDatabase);
						analyseFiles();

						database.initDbSelection(PatternGUI.this, theModel, database);	

					}
					//remove from the list if deselected
					else{
						database = null;
						currentDatabase = null;
						analyseFiles();}							
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

	

	/**
	 * Transforms the results of the analysis to the format required
	 * by the analysis level that is selected by calling appropriate
	 * methods in the Model Class
	 */
	private void formatAnalysisLevel()
	{
		if (analysisLevel == PACKAGE_LEVEL_ANALYSIS)
			theModel.makePackagesTopLevel();
		else if (analysisLevel == METHOD_LEVEL_ANALYSIS)
			theModel.makeMethodsTopLevel();
	}
}