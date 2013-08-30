package uk.ac.glasgow.dlee.nanopatterns.controller;

/**
 * This application allows .class or .jar files to be analysed for 
 * the nanopatterns they contain. 
 * An external nanopattern detector tool is utilised in order to carry out 
 * the analysis. The results are then fed into the data model of this application.
 * 
 * The model can be displayed in the form of a heat-map showing the percentage 
 * of nanopatterns present in the packages/classes/methods that were analysed.
 * Alternatively, a text display can be produced giving a detailed description of the
 * nanopatterns that were detected. These two views can be saved as a .png and a .txt
 * file respectively.
 * 
 * A database feature allows the analysis results to be saved and retrieved
 * later. The results in the database can be filtered so the user can choose the packages
 * and classes they wish to view, and these results are integrated into the data model.
 * 
 * @author Dave's Laptop
 *
 */
public class nanopatternDetector {

	/**
	 * Main method to create a new PatternGUI
	 * 
	 * @param args the unused command line arguments
	 */
	public static void main(String[] args)
	{		
		PatternGUI gui = new PatternGUI();			
	}
}
