package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.util.ArrayList;

/**
 * This class holds an abstracted model for holding the results of classes
 * analysed for nanopatterns. 
 * 
 * Not yet incorporated to the model
 * 
 * @author Dave's Laptop
 *
 */
public class ClassModel{

	private String packageName, className;
	private int numMethods;
	
	/** datastructure to hold the methods associated with a class */
	private ArrayList<MethodModel> methods;
	
	
	
	/**
	 * Supplied with an ArrayList<String[]> representing a class
	 * and its methods, creates a MethodModel for each method, and adds
	 * to a list of methods belonging to the class
	 * 
	 * @param aClass 
	 */
	public ClassModel(ArrayList<String[]> aClass)
	{
		methods = new ArrayList<MethodModel>();
		
		for(int i = 0; i< aClass.size(); i++)
		{
			MethodModel aMethod = new MethodModel(aClass.get(i));
			methods.add(aMethod);
		}
		
		numMethods = methods.size();
		
		//the package name and class name can be garnered from the
		//first method
		if(numMethods != 0)
		{
			packageName = methods.get(0).getPackageName();
			className = methods.get(0).getClassName();
		}
		//deal with empty classes
		else
		{
			packageName = "Empty Package";
			className = "Empty Class";
		}
	}
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//								Getters													//
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the name of the package
	 * 
	 * @return String representing the package name
	 */
	public String getPackageName()
	{
		return packageName;
	}
	
	
	
	/**
	 * Returns the name of the class
	 * 
	 * @return String representing the class name
	 */
	public String getClassName()
	{
		return className;
	}
	
	
	
	/**
	 * Returns the number of methods in this class
	 * 
	 * @return integer representing the number of methods
	 */
	public int size()
	{
		return numMethods;
	}


	
	/**
	 * Returns the method at the specified index
	 * 
	 * @param methodIndex the index of the method
	 * @return MethodModel object representing an analysed method
	 */
	public MethodModel getMethod(int methodIndex) {
		return methods.get(methodIndex);
		
	}	
}
