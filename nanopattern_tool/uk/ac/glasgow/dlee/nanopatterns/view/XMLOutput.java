package uk.ac.glasgow.dlee.nanopatterns.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;

import uk.ac.glasgow.dlee.nanopatterns.model.Model;

/**
 * Produces an XML representation of the model, following the
 * standard component-based structure of Java.
 * 
 * @author Dave's Laptop
 *
 */
public class XMLOutput implements ViewInterface{

	private Model theModel;
	

	public XMLOutput(Model modelToOutput)
	{
		theModel = modelToOutput;
		
	}

	
	/**
	 * Takes the results of the analysis, adds xml tags and outputs the results to a file
	 * @param saveLocation destination to save the file
	 */
	public boolean save (File saveLocation)
	{

			//string builder to hold the xml String as it is created
			StringBuilder sb = new StringBuilder();

			sb.append("<analysis>");
			for (int i = 0; i < theModel.size(); i++)
			{
				ArrayList<String[]>thisClass = theModel.getClass(i);

				//format the classname
				String[] firstMethod = thisClass.get(0);
				String className = escapeChars(firstMethod[1]);


				sb.append("\n\t<class name =\""+className+"\">");
				//format xml for all methods in the class
				for(int j = 0; j< thisClass.size(); j++)
				{
					String[] thisMethod = thisClass.get(j);
					//print the method name description and number of instructions
					sb.append("\n\t\t<method name = \""+ escapeChars(thisMethod[METHOD_NAME]) + "\" desc = \""
							+thisMethod[METHOD_DESC] + "\" numinstr = \"" + thisMethod[NUM_INSTRUCTIONS] + "\">");

					//print out the binary string of nanopatterns
					sb.append("\n\t\t\t"+thisMethod[BINARY_RESULTS_STRING]);

					sb.append("\n\t\t</method>");	
				}

				sb.append("\n\t</class>");
			}
			sb.append("\n</analysis>");

			String output = sb.toString();

			//write the xml to a file
			FileWriter fw = null;
			try{
				try 
				{
					fw = new FileWriter(saveLocation);
					fw.write(output);	

				return	 true;
				} 
				finally
				{
					if(fw != null)
						fw.close();
				}
			}
			catch (IOException e) {

				e.printStackTrace();
				
				return false;
			}
		}
	



	/**
	 * For the xml output. Remove illegal characters & replace with xml valid equivalents
	 * 
	 * @param input the file name to be formatted
	 * @return a String with illegal characters removed
	 */
	private String escapeChars(String input)
	{
		input = input.replaceAll("<", "&lt;");
		input = input.replaceAll(">", "&gt;");

		return input;
	}

	/**
	 * Stub method to comply with the interface. 
	 * May be expanded at a later date.
	 */
	public JComponent displayView()
	{
		return null;
	}
}
