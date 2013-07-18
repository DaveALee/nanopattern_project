package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;



import org.tc33.jheatchart.HeatChart;

public class HeatMap extends JFrame implements ViewInterface{

	/** Holds the percentage of each type of nanopattern for each package/class/method */
	private double[][] data;

	/** Holds the values to be displayed on the x axis */
	private Object[] xAxis;

	/** Holds the values to be displayed on the y axis */
	private Object[] yAxis;

	private Model theModel;

	private int analysisLevel;
	
	private HeatChart map;

	private final int PACKAGE_LEVEL = 0;
	private final int CLASS_LEVEL = 1;
	private final int METHOD_LEVEL = 2;

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 


	/**
	 * Constructor to set up a HeatMap object by instantiating the instance variables
	 *  
	 * @param classInfo the 2D array containing each package/class/method (y axis) and the percentage
	 * of each type of nanopattern associated with it
	 * @param patternNames the x axis values
	 * @param classNames the y axis values
	 */
	public HeatMap(Model modelToDisplay)
	{
		theModel = modelToDisplay;

		analysisLevel = theModel.getAnalysisLevel();

		//make an array for the x axis containing the pattern names
		//is the same regardless of the analysis level
		xAxis = theModel.getPatternList().toArray();
		
		//make the array to hold the y axis labels
		yAxis = getYAxisNames();
			

		generateView();
	}



	//////////////////////////////////////////////////////////
	//				Heat Map Display                        //
	//////////////////////////////////////////////////////////

	/**
	 * 
	 * 
	 * @param listToAnalyse
	 */
	public void generateView()	
	{
		if(analysisLevel == PACKAGE_LEVEL || analysisLevel == CLASS_LEVEL)
		{
			//prepare the double[][] required as input to the heat map
			data = new double[theModel.getNumClasses()][theModel.getPatternList().size()];

			// for each class, interpret the results of the analysis
			for(int i = 0; i < theModel.getNumClasses(); i++)
			{ 
				//find the number of methods in the class to allow calculation of average
				int numMethodsInAnalysis = theModel.getClassSize(i);

				// holds the number of each type of pattern in the target package/class
				int[] numPatternsInAnalysis = new int[theModel.getPatternList().size()];

				//for each method in the class/package, decide whether it contains each nanopattern or not
				for (int j = 0; j < numMethodsInAnalysis; j++)
				{			
					//the full results of the analysis of one method
					String[] methodInfo = theModel.getMethod(i,j);

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
					data[i] = calcPercentPatterns(numPatternsInAnalysis, numMethodsInAnalysis);
				}
			}

		}
		//Operate on the methods
		//average does not need to be found as the pattern is either present or not present,
		//so simply use the 1 or 0 from the results to provide input to the heat map
		else
		{
			//prepare the double[][] required as input to the heat map
			data = new double[theModel.getSizeOfFirstElement()][theModel.getPatternList().size()];

			//the methods are all held in the first element of the analysis data structure 
			ArrayList<String[]> allMethods = theModel.getFirstElement();

			//array to hold the y axis values
			yAxis = new Object[allMethods.size()];

			//split for each method
			for(int i = 0; i < allMethods.size(); i++)
			{
				//the results from the analysis of a single method
				String[] methodInfo = allMethods.get(i);

				//add the current methods name to the array holding y axis values
				yAxis[i] = methodInfo[METHOD_NAME];

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
				data[i] = methodResults;
			}
		}
	}



	/**
	 * Finds the elements for the y axis of the heat map. Depending on the 
	 * analysis level, the y axis will be either package names or class names.
	 * 
	 * @param analysisList the datastructure the y axis values will be extracted from
	 * @return an array containing the elements to be used on the y axis of the heat map
	 */
	private Object[] getYAxisNames()
	{
		
		//array to hold the y axis values
		Object[] yAxis = new Object[theModel.getNumClasses()];

		for(int i = 0; i < yAxis.length; i++ )
		{
			//the first element in any class will contain the y axis value
			ArrayList<String[]> a = theModel.getClass(i);

			if(a.size() == 0)
			{
				yAxis[i] ="empty";
				continue;
			}

			String[] aYAxisElement = a.get(0);	

			if(analysisLevel == PACKAGE_LEVEL)
			{
				//y axis values are package names
				yAxis[i] = aYAxisElement[PACKAGE_NAME];
			}
			else if (analysisLevel == CLASS_LEVEL)
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
	 * Creates and configures a heat map based on the data provided by 
	 * the instance variables 
	 * @return a scrollpane containing the heat map
	 */
	public JPanel displayView()
	{
		//0 is the low value, 1 is high.
		//all data expected to be in the range 0-1
		map = new HeatChart(data, 0, 1);

		//axis labels
		map.setXAxisLabel("pattern");
		map.setYAxisLabel("class");

		//high and low value colours
		map.setHighValueColour(Color.GREEN);
		map.setLowValueColour(Color.WHITE);

		//title
		map.setTitle("Patterns Heat Map");

		//set the size of the cells
		map.setCellSize(new Dimension(20,20));

		//axis font
		map.setAxisValuesFont(new Font ("Courier", Font.BOLD, 14));

		//set axis values
		map.setYValues(yAxis);
		map.setXValues(xAxis);

		//produce a BoundedImage which is the heat map
		Image chart = map.getChartImage();

		JPanel panel = new JPanel();	

		//chart is displayed as an ImageIcon of a JLabel
		JLabel chartImage = new JLabel(new ImageIcon(chart));
		panel.add(chartImage);		



		return panel;
	}
	
	
	
	public boolean save(File theFileToSave)
	{
		try {
			map.saveToFile(theFileToSave);
			return true;
		}
		catch (IOException e)
		{

			return false;
		}
	}
}



