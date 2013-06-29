package src.uk.ac.glasgow.jsinger.nanopatterns;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class PatternGUI extends JFrame implements ActionListener 
{

	private final int HEATMAP_MODE = 0;
	private final int TEXT_DISPLAY_MODE = 1;

	private final int PACKAGE_LEVEL_ANALYSIS = 0;
	private final int CLASS_LEVEL_ANALYSIS = 1;
	private final int METHOD_LEVEL_ANALYSIS = 2;	

	private int analysisLevel = 1;

	private int displayMode =0;

	/**Enclosing JFame	 */
	private JFrame topLevel;

	/** Panel for buttons */
	private JPanel userInput;

	/** List of all the patterns we know about (order is important) */
	private ArrayList<String> listOfPatterns;

	/**The patternDetector */
	private PatternSpotterForGUI spotter;

	/** JButton for user input */
	private JButton openFile;

	/** Menu Components */
	private JMenuBar menu;
	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenuItem reset;
	private JMenuItem saveResults;
	private JMenuItem help;
	private JMenuItem exit;

	/**JRadioButtons to allow switching between display modes */
	private JRadioButton heatMapMode;
	private JRadioButton textMode;

	/** JRadioButtons to allow the analysis to be run at alternative levels
	 * ie package, class and method
	 */
	private JRadioButton packageAnalysis;
	private JRadioButton classAnalysis;
	private JRadioButton methodAnalysis;


	/** Allows class file to be selected */
	private JFileChooser fileChooser;

	/** The files to be analysed **/
	private ArrayList<String> fileList;

	/** Panel to hold the files to be analysed */
	private JPanel filePanel;

	/**Panel to hold the heatmap results */
	private JPanel heatMapPanel;

	/** Allows the files to be displayed in a single column with multiple rows*/
	private GridLayout grid;

	/**Tabbed pane to display the results separately */
	private JTabbedPane tabs;




	/**
	 * Constructor to initialise the GUI
	 */
	public PatternGUI(){		

		// detects patterns in specified classes
		spotter = new PatternSpotterForGUI();

		//set containing the files to be analysed
		fileList = new ArrayList<String>();	

		initDisplay();

		createPatternList();
	}



	/**
	 * Sets up and lays out the JComponents
	 */
	private void initDisplay(){

		topLevel = new JFrame();
		topLevel.setSize(750,600);
		topLevel.setTitle("Nanopattern Detector Tool");
		topLevel.setDefaultCloseOperation(EXIT_ON_CLOSE);

		userInput = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();


		/////////////////// Display Mode //////////////////////
		ButtonGroup modeSelection = new ButtonGroup();
		heatMapMode = new JRadioButton("Heat Map");

		heatMapMode.addActionListener (new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				displayMode = HEATMAP_MODE;	

				packageAnalysis.setVisible(true);
				classAnalysis.setVisible(true);
				methodAnalysis.setVisible(true);

				redrawDisplay();
			}});

		heatMapMode.setSelected(true);

		textMode = new JRadioButton("Text Display");

		textMode.addActionListener (new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				displayMode = TEXT_DISPLAY_MODE;	

				packageAnalysis.setVisible(false);
				classAnalysis.setVisible(false);
				methodAnalysis.setVisible(false);


				redrawDisplay();
			}});


		modeSelection.add(heatMapMode);
		modeSelection.add(textMode);

		buttonsPanel.add(heatMapMode);
		buttonsPanel.add(textMode);

		heatMapPanel = new JPanel();


		////////////////// Analysis Level ////////////////



		ButtonGroup analysisLevelSelect = new ButtonGroup();
		packageAnalysis = new JRadioButton("Package");
		packageAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = PACKAGE_LEVEL_ANALYSIS;
				redrawDisplay();
			}});

		classAnalysis = new JRadioButton("Class");	
		classAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = CLASS_LEVEL_ANALYSIS;
				redrawDisplay();
			}});

		methodAnalysis = new JRadioButton("Method");
		methodAnalysis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){			
				analysisLevel = METHOD_LEVEL_ANALYSIS;
				redrawDisplay();
			}});

		analysisLevelSelect.add(packageAnalysis);
		analysisLevelSelect.add(classAnalysis);
		analysisLevelSelect.add(methodAnalysis);

		classAnalysis.setSelected(true);			

		buttonsPanel.add(packageAnalysis);
		buttonsPanel.add(classAnalysis);
		buttonsPanel.add(methodAnalysis);	


		//////////////// Tabbed Pane for Text Results //////////

		//set up tabs to hold the results
		tabs = new JTabbedPane();


		//////////////// Buttons /////////////////

		openFile = new JButton ("Add File");
		openFile.addActionListener(this);

		buttonsPanel.add(openFile);

		userInput.add(buttonsPanel, BorderLayout.NORTH);


		///////////// Area to display class paths ////////////////////
		grid = new GridLayout(1,1);
		filePanel = new JPanel(grid);
		userInput.add(filePanel);


		////////////////// add components to frame ///////////////////

		topLevel.add(userInput, BorderLayout.NORTH);
		//setup menu and add to the frame
		setupMenu();

		topLevel.setVisible(true);	
	}



	/**
	 * Creates the menubar for the top of the JFrame
	 */
	private void setupMenu()
	{
		menu = new JMenuBar();
		topLevel.setJMenuBar(menu);	

		fileMenu = new JMenu("File");

		//reset the display to original appearance
		reset = new JMenuItem ("Reset Display");
		reset.addActionListener(this);	
		fileMenu.add(reset);

		saveResults = new JMenuItem ("Export as xml");
		saveResults.addActionListener(this);
		fileMenu.add(saveResults);

		exit = new JMenuItem ("Exit");
		exit.addActionListener(this);
		fileMenu.add(exit);
		menu.add(fileMenu);		

		helpMenu = new JMenu("Help");
		help = new JMenuItem ("NanoPatterns");
		help.addActionListener(this);
		helpMenu.add(help);
		menu.add(helpMenu);	
	}



	/**
	 * Creates an ArrayList of all the known nanopatterns
	 * TODO maybe not the right place for this? Think of a better way.
	 */
	private void createPatternList(){

		listOfPatterns = new ArrayList<String>();

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



	/**
	 * Listens for button presses and responds appropriately
	 */
	public void actionPerformed (ActionEvent event)
	{
		//open button
		if(event.getSource() == openFile)
		{

			//open a file chooser at the current working directory
			fileChooser = new JFileChooser(System.getProperty ("user.dir")); 

			//user chooses a file
			if (fileChooser.showOpenDialog(userInput) == JFileChooser.APPROVE_OPTION) {

				if(checkFileValid(""+  fileChooser.getSelectedFile()))
				{

					//add this file to the fileSet
					fileList.add(""+  fileChooser.getSelectedFile());					

				}
				else
				{
					JOptionPane.showMessageDialog(topLevel, "Files must be .jar or .class",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				//show the files to be analysed
				displayFilesToAnalyse();

				//analyse the selected files and update the display
				redrawDisplay();

			}
		}
		//reset menu item
		else if (event.getSource() == reset)
		{

			tabs.setVisible(false);
			heatMapPanel.setVisible(false);

			filePanel.removeAll();
			fileList.clear();

			filePanel.revalidate();
			validate();
		}

		else if (event.getSource() == saveResults)
		{
			printXML();
		}

		//exit menu item
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



	private void redrawDisplay()
	{
		//convert the fileSet to an array which is compatible with the detector tool
		String[] args = fileList.toArray(new String[0]);

		//check to see if there is a file to be analysed before analysing
		if (!fileList.isEmpty()){

			//use the PatternSpotter to analyse the file(s) and detect patterns
			ArrayList<ArrayList<String[]>> results = spotter.detect(args);			


			if(displayMode == HEATMAP_MODE)
			{		


				results = formatAnalysisLevel(results);

				tabs.setVisible(false);

				topLevel.remove(heatMapPanel);

				makeHeatMap(results);
				repaint();
			}

			else if(displayMode == TEXT_DISPLAY_MODE)
			{

				topLevel.remove(heatMapPanel);

				printResults(results);

				repaint();		
			}
		}
	}


	/**
	 * Checks whether the files selected are the correct type
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
	 * Displays the selected classes to the user and allows them to be deselected
	 */
	private void displayFilesToAnalyse()
	{
		//reset the panel to avoid duplicate display
		filePanel.removeAll();

		//iterator to enable the set of file paths to be output
		Iterator<String> it = fileList.iterator();

		//the number rows relating to the number of files to be analysed
		int row=0;		

		//use the iterator to traverse the set
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

	private void makeHeatMap(ArrayList<ArrayList<String[]>> listToAnalyse)
	{

		if(analysisLevel == PACKAGE_LEVEL_ANALYSIS || analysisLevel == CLASS_LEVEL_ANALYSIS)
		{

			double[][] heatMapInput = new double[listToAnalyse.size()][listOfPatterns.size()];

			//make an array for the y axis containing the pattern names
			Object[]patterns = listOfPatterns.toArray();

			Object [] classes = getClassNames(listToAnalyse);

			//for each of the classes analysed...
			for(int i = 0; i < listToAnalyse.size(); i++)
			{ 
				//find the number of methods in the class
				int numMethodsInClass = listToAnalyse.get(i).size();

				int[] numPatternsInClass = new int[listOfPatterns.size()];

				//for each method in the class, decide whether it contains each nanopattern or not
				for (int j = 0; j < numMethodsInClass; j++)
				{			
					//the full results of the analysis of one method
					String[] methodInfo = listToAnalyse.get(i).get(j);

					String [] binaryResults = methodInfo[5].split(" +");

					//for each binary digit of the method results
					//increase frequency of pattern in class if present
					for(int k = 0; k < binaryResults.length; k++)
					{
						//will be a 1 or 0 representing a pattern's presence in the method
						String pattern = binaryResults[k];

						if (pattern.equals("1"))
						{
							//increase the number of patterns found
							numPatternsInClass[k]++;
						}
					}

					//one class' results 
					heatMapInput[i] = calcPercentPatterns(numPatternsInClass, numMethodsInClass);
				}
			}


			HeatMap map = new HeatMap(heatMapInput, patterns, classes);

			heatMapPanel = map.displayHeatMap();		




			topLevel.add(heatMapPanel, BorderLayout.CENTER);

			topLevel.revalidate();
			validate();
		}
		//pass the methods
		else
		{

			double[][] heatMapInput = new double[listToAnalyse.get(0).size()][listOfPatterns.size()];

			//make an array for the y axis containing the pattern names
			Object[]patterns = listOfPatterns.toArray();



			ArrayList<String[]> allMethods = listToAnalyse.get(0);

			Object[] methods = new Object[allMethods.size()];

			//split for each method
			for(int i = 0; i < allMethods.size(); i++)
			{
				//a method's results
				String[] methodInfo = allMethods.get(i);

				methods[i] = methodInfo[2];

				String [] binaryResults = methodInfo[5].split(" +");

				double[] methodResults = new double[binaryResults.length];

				for(int j = 0; j < binaryResults.length; j++)
				{
					methodResults[j] = Double.parseDouble(binaryResults[j]);
				}


				heatMapInput[i] = methodResults;
			}

			HeatMap map = new HeatMap(heatMapInput, patterns, methods);

			heatMapPanel = map.displayHeatMap();


			topLevel.add(heatMapPanel, BorderLayout.CENTER);			

			topLevel.revalidate();
			validate();
		}
	}




	private Object[] getClassNames(ArrayList<ArrayList<String[]>> listOfClasses)
	{
		Object[] classNames = new Object[listOfClasses.size()];
		for(int i = 0; i < classNames.length; i++ )
		{
			String[] aMethod = listOfClasses.get(i).get(0);	

			if(analysisLevel == PACKAGE_LEVEL_ANALYSIS)
			{
				classNames[i] = aMethod[0];
			}
			else
			{
				classNames[i] = aMethod[1];
			}
		}
		return classNames;
	}



	private double[] calcPercentPatterns(int[] numPatterns, int numMethodsInClass)
	{
		double[] percentPatterns = new double [numPatterns.length];

		for (int i =0;  i< numPatterns.length; i++)
		{
			double patternNumber = (double) numPatterns[i];

			percentPatterns[i] = patternNumber / (double) numMethodsInClass;

		}

		return percentPatterns;
	}


	private ArrayList<ArrayList<String[]>> formatAnalysisLevel(ArrayList<ArrayList<String[]>> results)
	{
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
				String[] method = nextClass.get(0);
				//packageName is the package the current class belongs to
				String packageName = method[0];	

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
						//get the name of the next package in the list of seen packages
						String[] aMethod = packages.get(j).get(0);
						String thisPackageName = aMethod[0];

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
					if(!found)
					{
						//make a new entry for the package
						packages.add(nextClass);	
					}
				}
			}	
			return packages;
		}
		else if(analysisLevel == CLASS_LEVEL_ANALYSIS)
		{
			return results;
		}
		//method level analysis
		else
		{
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

			ArrayList<ArrayList<String[]>> methodLevelResults = new ArrayList<ArrayList<String[]>>();

			methodLevelResults.add(methods);

			results = methodLevelResults;

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

			//layout text area
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
					tabs.addTab(method[1], null, scrollPane, method[0] );	

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
		sb.append(String.format("Method Name: %-15s\n", methodResults[2]));

		sb.append(String.format("Method Desc: %-15s\n", methodResults[3]));

		sb.append(String.format("Num Instructions: %-15s\n", methodResults[4]));


		//split the binary string into an array of 1s and 0s representing the presence of nanopatterns
		String[] binary = methodResults[5].split(" +");

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
					sb.append("\n\t\t<method name = \""+ escapeChars(thisMethod[2]) + "\" desc = \""
							+thisMethod[3] + "\" numinstr = \"" + thisMethod[4] + "\">");

					//print out the binary string of nanopatterns
					//TODO alter so arrayList is used and can divide into types of pattern??
					sb.append("\n\t\t\t"+thisMethod[5]);

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

