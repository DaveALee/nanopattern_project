package org.dave;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;


import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import uk.ac.glasgow.dlee.nanopatterns.model.Model;
import uk.ac.glasgow.dlee.nanopatterns.view.HeatMap;



/**
 * Servlet to allow .jar or .class files to be uploaded, by a brower then
 * analysed by the nanopattern tool. The results of the analysis are then
 * passed back to the brower.
 * 
 * Adapted from the tutorial series https://www.youtube.com/playlist?list=PLE0F6C1917A427E96
 * 
 * @author Dave's Laptop
 *
 */
@MultipartConfig
public class UploadServlet extends HttpServlet{			

	// Create path components to save the file
	final String path = System.getProperty("java.io.tmpdir");
	

/**
 * Processes the  servelt's HTTP request, analyses the files, and saves the results in a location
 * where it is accessible by a browser.
 */
	protected void processRequest(HttpServletRequest request,HttpServletResponse response, int analysisLevel)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");

		OutputStream out = null;
		InputStream filecontent = null;
		final PrintWriter writer = response.getWriter();

		try {
			//array to hold the file names
			ArrayList<String> files = new ArrayList<String>();

			for(Part part : request.getParts())
			{
				if(getFileName(part) != null){
					//String fileName = (path + File.separator + getFileName(part));
					String fileName = (System.getProperty("java.io.tmpdir")+getFileName(part));
					System.out.println("fileName = " +fileName);

					out = new FileOutputStream(new File(fileName));
					filecontent = part.getInputStream();

					int read = 0;
					final byte[] bytes = new byte[1024];
					//create the file content
					while ((read = filecontent.read(bytes)) != -1) {
						out.write(bytes, 0, read);
					}

					files.add(fileName);
				}
			}
			String[] args = files.toArray(new String[0]);
			analyse(args, analysisLevel);	

			//display the heat map
			writer.println("<img src=\""+path+"heatmap.png\"/><br/>" +
					"<a href = \"uploader.html\">back</a>");
			System.out.println("heat map location = "+path +"heatmap.png" );

		} catch (FileNotFoundException fne) {
			writer.println("You either did not specify a file to upload or are "
					+ "trying to upload a file to a protected or nonexistent "
					+ "location.");
			writer.println("<br/> ERROR: " + fne.getMessage());


		} finally {
			if (out != null) {
				out.close();
			}
			if (filecontent != null) {
				filecontent.close();
			}
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Helper method to retrieve the filename
	 * @return String representing the filename
	 */
	private String getFileName(Part part) {

		String partHeader = part.getHeader("content-disposition");

		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(
						content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}   



	/**
	 * gets the level parameter from the HTTP request and calls the helper method processRequest to
	 * conduct the analysis.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		
		String analysisLevel = request.getParameter("level");
		
		int level = -1;
		if(analysisLevel.equals("package")) level = 0;
		else if(analysisLevel.equals("class")) level = 1;
		else if(analysisLevel.equals("method")) level = 2;
			
		
		processRequest(request, response, level);


	}

	/**
	 * Creates a model and populates it with analysis results
	 */
	private void analyse(String[] args, int analysisLevel){

		Model theModel = new Model();
		theModel.createModel(args);

		if(analysisLevel == 0) theModel.makePackagesTopLevel();
		else if (analysisLevel == 2) theModel.makeMethodsTopLevel();
		
		HeatMap map = new HeatMap(theModel);
		map.displayView();
		System.out.println("heatmap saved at "+path+"heatmap.png");
		map.save(new File(path+"heatmap.png"));
		
	}
}