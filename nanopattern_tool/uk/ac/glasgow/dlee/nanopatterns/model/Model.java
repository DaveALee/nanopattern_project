package uk.ac.glasgow.dlee.nanopatterns.model;

import java.util.ArrayList;

import src.uk.ac.glasgow.jsinger.nanopatterns.PatternSpotter;

/**
 * This class holds the datastructure which is used to produce the 
 * analysis results. Methods are provided so that the datastructure can be 
 * maniplulted in order to produce the required view. 
 * 
 * @author Dave's Laptop
 *
 */
public class Model {

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	/** constants to represent the levels of analysis*/
	private final int PACKAGE_LEVEL = 0, CLASS_LEVEL = 1, METHOD_LEVEL = 2;

	/** Holds the names of all the nanopatterns*/
	private ArrayList<String> listOfPatterns;

	/** Data strucure to hold the results of the analysis */
	private ArrayList<ArrayList<String[]>> theModel;

	/** Tool to detect nanopatterns*/
	private PatternSpotter spotter;	

	/**Holds the current level of analysis (Package/Class/Method)	 */
	private int analysisLevel;



	/** Constructor to create a new data structure */
	public Model(){

		theModel = new ArrayList<ArrayList<String[]>>();

		//initially created at class level
		analysisLevel = CLASS_LEVEL;

		spotter = new PatternSpotter();

		createPatternList();
	}


	
	/** Analyses the list of files and adds the results of the analysis
	 * to the model.
	 * 
	 * @param files the files to be analysed
	 */
	public void createModel(String[] files)
	{
		if(files.length>0)
		{
			theModel = spotter.detect(files);
		}
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

	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// 				Transform data structure for different analysis levels					//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Transforms the data structure to make the packages the top level element.
	 * scans each class to find the package it belongs to and adds it to an element in a 
	 * temporary model representing the package, or if the package element doesnt exist, 
	 * adds a new element representing the packages.
	 */
	public void makePackagesTopLevel()
	{
		ArrayList<ArrayList<String[]>> tempModel = new ArrayList<ArrayList<String[]>>();

		//examine the results for each class
		for(int i = 0; i <theModel.size(); i ++)
		{
			//the class to be assigned a package
			ArrayList<String[]> nextClass = theModel.get(i); 

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
			if(tempModel.size() == 0)
			{
				tempModel.add(nextClass);
			}
			else
			{
				//we have already identified package(s)
				//check if the package this class belongs to has already been seen
				boolean found = false;

				for(int j = 0; j< tempModel.size(); j++)
				{
					ArrayList<String[]> aPackage = tempModel.get(j);

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
						tempModel.get(j).addAll(nextClass);
						found = true;
						break;
					}
				}
				//package has not been previously seen
				if(!found)
				{
					//make a new entry for the package
					tempModel.add(nextClass);	
				}
			}
		}	
		theModel = tempModel;

		analysisLevel = PACKAGE_LEVEL;
	}



	/**
	 * Restructures the data structure so that all the methods are contained in one
	 *  "class level" element. Used for displaying heat maps with method level analysis.
	 */
	public void makeMethodsTopLevel()
	{
		//List to hold all the methods. Behaves like all the methods
		//belong to a single class
		ArrayList<String[]> methods = new ArrayList<String[]>();

		//remove class information
		for(int i = 0; i < theModel.size(); i++)
		{
			//get the class
			ArrayList<String[]> currentClass = theModel.get(i);

			//add all the methods in the class to the higher level list
			//thereby removing class information
			methods.addAll(currentClass);	
		}

		// methodLevelResults will hold an arrayList with only one entry containing all the methods
		//from all the classes
		ArrayList<ArrayList<String[]>> tempModel = new ArrayList<ArrayList<String[]>>();

		//add the ArrayList containing results from the methods to the higher level ArrayList
		//so the results are in the form needed by the display
		tempModel.add(methods);

		theModel = tempModel;

		analysisLevel = METHOD_LEVEL;
	}

	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//									Getters												//
	//////////////////////////////////////////////////////////////////////////////////////////


	
	/**
	 * Returns the size of the first element in the model. Used when the model is in
	 * the method level analysis in order to get the number of methods.
	 * 
	 * @return integer representing the number of methods
	 */
	public int getSizeOfFirstElement()
	{
		return theModel.get(0).size();
	}



	/**
	 * Returns the first element in the datastructure. Used when model is in method
	 * level anlaysis in order to get the methods.
	 * 
	 * @return ArrayList<String[]> containing one element with all the methods results to be displayed
	 */
	public ArrayList<String[]> getFirstElement()
	{
		return theModel.get(0);
	}



	/**
	 * Returns whether the model is in package/class/method level analysis.
	 * 
	 * @return integer representing the level of analysis
	 */
	public int getAnalysisLevel()
	{
		return analysisLevel;
	}



	/**
	 * Returns the size of the model in terms of the number of classes present
	 * 
	 * @return integer representing the number of classes in the model
	 */
	public int size()
	{
		return theModel.size();
	}


	/**
	 * Returns the class at the specified index
	 * @param index integer representing the position in the datastructure the class is at
	 * 
	 * @return ArrayList<String[]> holding all the methods of the class
	 */
	public ArrayList<String[]> getClass(int index)
	{
		return theModel.get(index);
	}



	/**
	 *  Returns the method situated at the specified class and method index
	 *  
	 * @param classIndex the position of the class in the datastructure
	 * @param methodIndex the position of the method in the class
	 * @return String[] represeting the results of a method
	 */
	public String[] getMethod(int classIndex, int methodIndex)
	{
		return theModel.get(classIndex).get(methodIndex);
	}



	/** returns the number of methods belonging to the class at
	 * the specified index.
	 * 
	 * @param index integer representing the class' position in the data structure
	 * @return integer representing the number of methods in the class
	 */
	public int getClassSize(int index)
	{
		return theModel.get(index).size();
	}

	
	
	/**
	 * Returns the list of nanopattern names
	 * 
	 * @return ArrayList<String> of the nanopatterns
	 */
	public ArrayList<String> getPatternList()
	{
		return listOfPatterns;
	}


	
	//////////////////////////////////////////////////////////////////////////////////////////
	//								Setters													//
	//////////////////////////////////////////////////////////////////////////////////////////

	
	
	/** Adds a method to the correct class in the datastructure by checking the 
	 * package name and the class name associated with the method. If the package
	 * or class doesnt exist, a new entry is created for the method
	 * 
	 * @param method the method to add to the model
	 */
	public void addMethod(String[] method)
	{
		//check for classes
		String packageName = method[PACKAGE_NAME];
		String className = method[CLASS_NAME];

		boolean exists = false;

		//check for class in the model
		for(int i = 0;  i < theModel.size(); i++)
		{
			//the current class
			ArrayList<String[]> thisClass = theModel.get(i);

			String[] methodToCheck = thisClass.get(0);

			//the package and class exists in the model
			if (methodToCheck[PACKAGE_NAME].equals(packageName) && methodToCheck[CLASS_NAME].equals(className))
			{
				//add the method to the model
				theModel.get(i).add(method);
				exists = true;
			}
		}
		//the package and class the method belongs to does not exist in the model
		if (!exists)
		{
			//make a new class for the method and add it to the model
			ArrayList<String[]> newClass = new ArrayList<String[]>();
			newClass.add(method);
			theModel.add(newClass);
		}
	}



	/**allows one model to be added to the end of another, for example 
	 * appending the results from a database to the results from analysed
	 * files.
	 * 
	 * @param otherModel the model to append to the main model
	 */
	public void appendModel(Model otherModel)
	{
		if (otherModel.size() != 0)
		{
			for(int i = 0; i < otherModel.size(); i++)
			{
				//add each class to the model
				theModel.add(otherModel.getClass(i));
			}
		}
	}

}
