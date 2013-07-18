package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.util.ArrayList;

public class Model {

	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 


	private ArrayList<String> listOfPatterns;

	private ArrayList<ArrayList<String[]>> theModel;

	/** Tool to detect nanopatterns*/
	private PatternSpotter spotter;;



	private final int PACKAGE_LEVEL = 0;
	private final int CLASS_LEVEL = 1;
	private final int METHOD_LEVEL = 2;

	private int analysisLevel;

	/** Constructor to create a new data structure */
	public Model(){

		theModel = new ArrayList<ArrayList<String[]>>();

		analysisLevel = CLASS_LEVEL;

		spotter = new PatternSpotter();

		createPatternList();

	}



	/** Makes sets the model to hold the results of the analysis */
	public Model createModel(String[] files)
	{
		theModel = spotter.detect(files);


		return this;
	}



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


	public int getSizeOfFirstElement()
	{
		return theModel.get(0).size();
	}


	public ArrayList<String[]> getFirstElement()
	{
		return theModel.get(0);
	}

	
	public int getAnalysisLevel()
	{
		return analysisLevel;
	}


	/**
	 * Returns the size of the model in terms of the number of classes present
	 * 
	 * @return integer representing the number of classes in the model
	 */
	public int getNumClasses()
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


	/** adds a method to the correct class in the datastructure*/
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
			
			if (methodToCheck[PACKAGE_NAME].equals(packageName) && methodToCheck[CLASS_NAME].equals(className))
			{
				theModel.get(i).add(method);
				exists = true;
			}
		}
		if (!exists)
		{
			ArrayList<String[]> a = new ArrayList<String[]>();

			a.add(method);

			theModel.add(a);
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


	public ArrayList<String> getPatternList()
	{
		return listOfPatterns;
	}

	/** helper method to return the model. Not visible externally */
	private ArrayList<ArrayList<String[]>> getModel()
	{
		return theModel;
	}
	
	
	/**allows one model to be added to the end of another **/
	public void appendModel(Model otherModel)
	{
		for(int i = 0; i < otherModel.getModel().size(); i++)
		{
			theModel.add(otherModel.getModel().get(i));
		}
	}




}
