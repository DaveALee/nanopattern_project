package src.uk.ac.glasgow.jsinger.nanopatterns;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class PatternGUI extends JFrame implements ActionListener 
{

	/**Enclosing JFame	 */
	private JFrame topLevel;

	/** Panel for buttons */
	private JPanel userInput;

	/** List of all the patterns we know about (order is important) */
	private ArrayList<String> listOfPatterns;

	/**The patternDetector */
	private PatternSpotterForGUI spotter;

	/** JButtons for user input */
	private JButton openFile;
	private JButton makeRocketsGoNow;

	/** Menu Components */
	private JMenuBar menu;
	private JMenu fileMenu;
	private JMenu helpMenu;
	private JMenuItem reset;
	private JMenuItem saveResults;
	private JMenuItem help;
	private JMenuItem exit;

	/** Allows class file to be selected */
	private JFileChooser fileChooser;

	/** The files to be analysed **/
	private TreeSet<String> fileSet;

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
		fileSet = new TreeSet<String>();	

		initDisplay();

		createPatternList();
	}



	/**
	 * Sets up and lays out the JComponents
	 */
	private void initDisplay(){

		topLevel = new JFrame();
		topLevel.setSize(1150,400);
		topLevel.setTitle("Nanopattern Detector Tool");
		topLevel.setDefaultCloseOperation(EXIT_ON_CLOSE);

		userInput = new JPanel(new BorderLayout());
		JPanel buttonsPanel = new JPanel();

		heatMapPanel = new JPanel();

		//set up tabs to hold the results
		//tabs = new JTabbedPane();


		//////////////// Buttons /////////////////

		openFile = new JButton ("Add File");
		openFile.addActionListener(this);

		//makeRocketsGoNow = new JButton("Analyse");
		//makeRocketsGoNow.addActionListener(this);

		buttonsPanel.add(openFile);
		//buttonsPanel.add(makeRocketsGoNow);

		userInput.add(buttonsPanel, BorderLayout.NORTH);


		///////////// Area to display class paths ////////////////////
		grid = new GridLayout(1,1);
		filePanel = new JPanel(grid);
		userInput.add(filePanel);


		////////////////// add components to frame ///////////////////

		topLevel.add(userInput, BorderLayout.NORTH);
		//topLevel.add(scrollPane, BorderLayout.CENTER);

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
					fileSet.add(""+  fileChooser.getSelectedFile());					

				}
				else
				{
					JOptionPane.showMessageDialog(topLevel, "Files must be .jar or .class",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				//show the files to be analysed
				displayFilesToAnalyse();


				/////////////////// auto analyse ///////////////


				redrawDisplay();

			}
		}

		/*
			//analyse button
			else if (event.getSource() == makeRocketsGoNow)
			{
				//check to see if there is a file to be analysed
				if (fileSet.isEmpty()){
					JOptionPane.showMessageDialog(topLevel, "Please select a file to be analysed.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				//analyse the file(s)
				else{
					//convert the fileSet to an array which is compatible with the detector tool
					String[] args = fileSet.toArray(new String[0]);

					//use the PatternSpotter to analyse the file(s) and detect patterns
					ArrayList<ArrayList<String[]>> results = spotter.detect(args);

					//printResults(results);

					makeHeatMap(results);

				}
			}

		 */
		//reset menu item
		else if (event.getSource() == reset)
		{

			tabs.setVisible(false);
			filePanel.removeAll();
			fileSet.clear();

			filePanel.revalidate();
			validate();
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
		String[] args = fileSet.toArray(new String[0]);

		//check to see if there is a file to be analysed
		if (fileSet.isEmpty()){
			JOptionPane.showMessageDialog(topLevel, "Please select a file to be analysed.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		else{
		
		//use the PatternSpotter to analyse the file(s) and detect patterns
		ArrayList<ArrayList<String[]>> results = spotter.detect(args);

		//printResults(results);

		topLevel.remove(heatMapPanel);

		makeHeatMap(results);
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
			JTextArea panel = new JTextArea();

			//fixed width font to ease alignment
			panel.setFont(new Font("Courier", Font.BOLD, 14));

			//layout text area
			Border resultsBorder = BorderFactory.createLineBorder(Color.BLACK);
			panel.setBorder(BorderFactory.createCompoundBorder(
					resultsBorder, BorderFactory.createEmptyBorder(10,10,10,10)));
			JScrollPane scrollPane = new JScrollPane(panel); 
			panel.setEditable(false);

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
				//panel.append(getMethodInfo(method));	
			}
			//set to top of results
			panel.setCaretPosition(0);
		}
		//add the tabs
		topLevel.add(tabs);
		//allow dynamic adding
		topLevel.revalidate();
		validate();
	}


	private void makeHeatMap(ArrayList<ArrayList<String[]>> listOfClasses)
	{

		double[][] heatMapInput = new double[listOfClasses.size()][listOfPatterns.size()];


		//make an array for the y axis containing the pattern names
		Object[] patterns = new Object[listOfPatterns.size()];
		patterns = listOfPatterns.toArray();

		Object [] classes = getClassNames(listOfClasses);


		//for each of the classes analysed...
		for(int i = 0; i < listOfClasses.size(); i++)
		{ 

			//find the number of methods in the class
			int numMethodsInClass = listOfClasses.get(i).size();

			int[] numPatternsInClass = new int[listOfPatterns.size()];

			//for each method in the class, decide whether it contains each nanopattern or not
			for (int j = 0; j < numMethodsInClass; j++)
			{			
				//the full results of the analysis of one method
				String[] methodInfo = listOfClasses.get(i).get(j);

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




	private Object[] getClassNames(ArrayList<ArrayList<String[]>> listOfClasses)
	{
		Object[] classNames = new Object[listOfClasses.size()];
		for(int i = 0; i < classNames.length; i++ )
		{
			String[] aMethod = listOfClasses.get(i).get(0);

			classNames[i] = aMethod[1];
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


	/**
	 * controls how the results of the analysis are displayed
	 * @param methodResults an array containing the results of the analysis on a single method
	 */

	/*
	public String getMethodInfo(String[] methodResults){

		StringBuilder sb = new StringBuilder();
		String results = "";
		//methodResults will be in the following format:
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

	 */






	/**
	 * Displays the selected classes to the user and allows them to be deselected
	 */
	private void displayFilesToAnalyse()
	{
		//reset the panel to avoid duplicate display
		filePanel.removeAll();

		//iterator to enable the set of file paths to be output
		Iterator<String> it = fileSet.iterator();

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
			file.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					//add to the set if selected
					if (file.isSelected()){	
						fileSet.add(curr);
						redrawDisplay();

					}
					//remove from the set if deselected
					else{fileSet.remove(curr);
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


	///////////////////////// XML ///////////////////////////////

	/**
	 * Takes the results of the analysis, adds xml tags and outputs the results to a file
	 * @param list the results of the analysis
	 */
	private void printxml (ArrayList<ArrayList<String[]>> list)
	{
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
				sb.append("\n\t\t\t"+thisMethod[4]);

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

