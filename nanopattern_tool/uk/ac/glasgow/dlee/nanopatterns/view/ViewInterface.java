package uk.ac.glasgow.dlee.nanopatterns.view;

import java.io.File;

import javax.swing.JComponent;

/**
 * Interface to be implemented by all views in the project
 * 
 * @author Dave's Laptop
 *
 */
public interface ViewInterface {
	
	/** The order of elements in the array of results for each method */
	final int PACKAGE_NAME = 0, CLASS_NAME = 1, METHOD_NAME = 2, METHOD_DESC = 3,NUM_INSTRUCTIONS = 4, BINARY_RESULTS_STRING = 5; 

	
	/** allows the view to be saved to the specified location */
	public boolean save(File location);
		
	/** returns a JComponent holding the view for use with the GUI */
	public JComponent displayView();
}
