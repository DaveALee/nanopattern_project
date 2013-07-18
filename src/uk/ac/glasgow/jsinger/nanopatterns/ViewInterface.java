package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.io.File;

import javax.swing.JComponent;

/**
 * Interface to be implemented by all views in the project
 * 
 * @author Dave's Laptop
 *
 */
public interface ViewInterface {
	
	/** allows the view to be saved to the specified location */
	public boolean save(File location);
		
	/** returns a JComponent holding the view for use with the GUI */
	public JComponent displayView();
}
