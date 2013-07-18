package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class DatabasePane extends JFrame{

	/** Lists to hold the selected package names and class names */
	private ArrayList<String> thePackages;
	private ArrayList<String> theClasses;

	/** JComponents of the frame */
	private JFrame frame;
	private JButton go;
	
	/** The Pattern GUI which constitutes the main display */	
	private PatternGUI mainGUI;
	
	/** The model containing analysed classes */
	private Model mainModel;
	
	/** Allows access to the database */
	private DatabaseAccess database;

	

	/**
	 * Constructor to create a new DatabasePane
	 * 
	 * @param packages the list of packages contained in the database
	 * @param classes the list of classes contained in the database
	 * @param callingGUI the GUI where the results of the selection will be displayed
	 * @param model the existing model to append the database results to
	 * @param db the DatabaseAccess object which allows the database to be queried
	 */
	public DatabasePane(ArrayList<String> packages, ArrayList<String> classes,
			PatternGUI callingGUI, Model model, DatabaseAccess db)
	{
		thePackages = packages;
		theClasses = classes;

		mainGUI = callingGUI;
		mainModel = model;
		database = db;		

		frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.setSize(300,400);

		layoutPackagePanel(thePackages);
		layoutClassPanel(theClasses);

		go = new JButton("Go");
		go.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//add the results of the query to the main model
				mainModel.appendModel(database.queryDatabase());
				//displays the view on the main GUI with a notification that 
				//the database model has been modified
				mainGUI.displayView(true);
				frame.dispose();
			}});			

		frame.add(go, BorderLayout.SOUTH);
		frame.setVisible(true);
	}


	/**
	 * Creates a new JPanel to hold the package names. For each package name
	 * adds a JCheckBox to allow the package name to be selected/deselected. 
	 * An action listener attached to the check box adds/removes the package 
	 * name from the list of selected packages.
	 * 
	 * @param classes the list of class names retrieved from the database
	 * to display on the JPanel
	 */
	private void layoutPackagePanel(ArrayList<String> packages )
	{
		JPanel packagePanel = new JPanel();
		packagePanel.setBorder(BorderFactory.createTitledBorder("Packages"));

		for(int i = 0; i < packages.size(); i++)
		{
			//create a checkbox for every selected file			
			final JCheckBox aPackage = new JCheckBox(packages.get(i));

			//anonymous inner class to handle action events
			//associated with the selection of which files should be 
			//analysed
			aPackage.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//add to the set if selected
					if (aPackage.isSelected())
					{	
						thePackages.add(aPackage.getText());
					}
					//remove from the set if deselected
					else{thePackages.remove(aPackage.getText());
					}							
				}});			

			//initally set the file to be selected
			aPackage.setSelected(true);

			//add the checkbox and file desc to the display
			packagePanel.add(aPackage);					
		}

		frame.add(packagePanel, BorderLayout.NORTH);
	}



	/**
	 * Creates a new JPanel to hold the class names. For each class name
	 * adds a JCheckBox to allow the class name to be selected/deselected. 
	 * An action listener attached to the check box adds/removes the class 
	 * name from the list of selected classes.
	 * 
	 * @param classes the list of class names retrieved from the database
	 * to display on the JPanel
	 */
	private void layoutClassPanel(ArrayList<String> classes )
	{
		JPanel classPanel = new JPanel();
		classPanel.setBorder(BorderFactory.createTitledBorder("Classes"));

		for(int i = 0; i < classes.size(); i++)
		{
			//create a checkbox for every selected file			
			final JCheckBox aClass = new JCheckBox(classes.get(i));

			//anonymous inner class to handle action events
			//associated with the selection of which files should be 
			//analysed
			aClass.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					//add to the set if selected
					if (aClass.isSelected())
					{	
						theClasses.add(aClass.getText());
					}
					//remove from the set if deselected
					else{theClasses.remove(aClass.getText());
					}							
				}});			

			//initally set the file to be selected
			aClass.setSelected(true);

			//add the checkbox and file desc to the display
			classPanel.add(aClass);					
		}

		frame.add(classPanel, BorderLayout.CENTER);
	}



	/**
	 * Returns an ArrayList<String> of the packages the user has
	 * selected using the DatabasePane
	 * 
	 * @return ArrayList<String> of package names
	 */
	public ArrayList<String> getPackages()
	{
		return thePackages;
	}



	/**
	 * Returns and ArrayList<String> of the classes the user has 
	 * selected using the DatabasePane
	 * 
	 * @return ArrayList<String> of clas names
	 */
	public ArrayList<String> getClasses()
	{
		return theClasses;
	}
}
