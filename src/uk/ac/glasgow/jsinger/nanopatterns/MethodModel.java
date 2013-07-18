package src.uk.ac.glasgow.jsinger.nanopatterns;

/**
 * Datastructure to hold a single analysed method
 * 
 *NOT YET INCORPORATED TO MODEL
 * 
 * @author Dave's Laptop
 *
 */
public class MethodModel {
	
	/** The order of elements in the array of results for each method */
	private final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	/** Instance variables to hold information about the method */
	private String packageName, className, methodName, methodDesc, binaryResults;
	private int numInstructions;

	
	
/**
 * Initialises the instance variables to the information held in the analysed method
 * results supplied as a parameter
 * 	
 * @param aMethod a single method is created from a String[] holding the analysed results of the method
 */
	public MethodModel(String[] aMethod)
	{
		packageName = aMethod[PACKAGE_NAME];
		className = aMethod[CLASS_NAME];
		methodName = aMethod[METHOD_NAME];
		methodDesc = aMethod[METHOD_DESC];
		numInstructions = Integer.parseInt(aMethod[NUM_INSTRUCTIONS]);
		binaryResults = aMethod[BINARY_RESULTS_STRING];
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//									Getters												//
	//////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * Returns the package name that the method belongs to
	 * 
	 * @return String representing the package name of the method
	 */
	public String getPackageName()
	{
		return packageName;
	}
	
	
	/**
	 * Returns the class name that the method belongs to
	 * 
	 * @return String representing the class name of the method
	 */
	public String getClassName()
	{
		return className;
	}
	
	
	
	/**
	 * Returns the name of this method
	 * 
	 * @return String representing the name of the method
	 */
	public String getMethodName()
	{
		return methodName;
	}
	
	
	
	/**
	 * Returns the description of the method
	 * 
	 * @return String representing the method description
	 */
	public String getMethodDesc()
	{
		return methodDesc;
	}
	
	
	
	/**
	 * Returns the number of instructions belonging to the method
	 * 
	 * @return integer representing the number of instructions
	 */
	public int getNumInstructions()
	{
		return numInstructions;
	}
	
	
	/**
	 * Returns the binary string representing the presence of 
	 * nanopatterns in the method
	 * 
	 * @return binary String of nanopatterns in the method
	 */
	public String getNanopatterns()
	{
		return binaryResults;
	}
}
