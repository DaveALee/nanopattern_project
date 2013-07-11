package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;




public class TextDisplay implements ViewInterface{

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 


	/**Tabbed pane to display the results separately */
	private JTabbedPane tabs;

	private Model theModel;


	public TextDisplay(Model modelToDisplay)
	{
		theModel = modelToDisplay;

		tabs = new JTabbedPane();
	}

	/**
	 *  Produces the output of the analysis in the form of a tabbed interface
	 *  with a tab for every class analysed.
	 * 
	 * @param results arrayList containing a the classes analysed
	 */
	public JTabbedPane displayView() 
	{				

		//for each class analysed
		for(int i = 0; i < theModel.getNumClasses();i++)
		{
			//get the list of classes
			ArrayList<String[]> fileResults = theModel.getClass(i);

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


		//scroll.add(tabs);
		return tabs;
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
				sb.append(theModel.getPatternList().get(i) + " ");
			}

		}
		sb.append("\n---------------------------------------------------------------\n");

		results = sb.toString();

		return results;
	}

	/** 
	 * saves the results of the analysis in the specified file location
	 */
	public boolean save(File saveLocation)
	{
		String analysisResults = "";

		//for each class analysed
		for(int i = 0; i < theModel.getNumClasses();i++)
		{
			String classResults = "";

			//get the list of classes
			ArrayList<String[]> fileResults = theModel.getClass(i);


			//get the list of methods for current class
			for(int j=0; j < fileResults.size(); j++)
			{

				//the results for one method
				String [] method = fileResults.get(j);

				//add class name
				if (j == 0)
				{		

					classResults = "\n===============================================================\n"+
							method[PACKAGE_NAME] + "." + method[CLASS_NAME] + 
							"\n===============================================================\n";

				}
				//get and display results as a string
				classResults += (getMethodInfo(method));	
			}
			analysisResults += classResults;			
		}

		//write to file
		FileWriter fw = null;
		try{
			try{


				fw = new FileWriter(saveLocation);

				fw.write(analysisResults);
				return true;

			}
			finally{
				fw.close();
			}
		}
		catch (Exception e)
		{
			return false;
		}

	}

}