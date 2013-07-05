package src.uk.ac.glasgow.jsinger.nanopatterns;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.tc33.jheatchart.HeatChart;

public class HeatMap extends JFrame{

	/** Holds the percentage of each type of nanopattern for each package/class/method */
	double[][] data;
	
	/** Holds the values to be displayed on the x axis */
	Object[] xAxis;
	
	/** Holds the values to be displayed on the y axis */
	Object[] yAxis;

	/**
	 * Constructor to set up a HeatMap object by instantiating the instance variables
	 *  
	 * @param classInfo the 2D array containing each package/class/method (y axis) and the percentage
	 * of each type of nanopattern associated with it
	 * @param patternNames the x axis values
	 * @param classNames the y axis values
	 */
	public HeatMap(double[][] classInfo, Object[] patternNames, Object[] classNames)
	{
		data =  classInfo;
		xAxis = patternNames;
		yAxis = classNames;
	}

	
	/**
	 * Creates and configures a heat map based on the data provided by 
	 * the instance variables 
	 * @return a scrollpane containing the heat map
	 */
	public JScrollPane displayHeatMap()
	{
		//0 is the low value, 1 is high.
		//all data expected to be in the range 0-1
		HeatChart map = new HeatChart(data, 0, 1);

		//axis labels
		map.setXAxisLabel("pattern");
		map.setYAxisLabel("class");

		//high and low value colours
		map.setHighValueColour(Color.GREEN);
		map.setLowValueColour(Color.WHITE);

		//title
		map.setTitle("Patterns Heat Map");

		//set the size of the cells
		map.setCellSize(new Dimension(20,20));

		//axis font
		map.setAxisValuesFont(new Font ("Courier", Font.BOLD, 14));

		//set axis values
		map.setYValues(yAxis);
		map.setXValues(xAxis);

		//produce a BoundedImage which is the heat map
		Image chart = map.getChartImage();
		
		JPanel panel = new JPanel();
		
		//chart is displayed as an ImageIcon of a JLabel
		JLabel chartImage = new JLabel(new ImageIcon(chart));
		panel.add(chartImage);		

		//adds a scroll bar so that if the heat map is larger than the GUI, scrolling is possible
		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//set the default position of the bar to be to the right
		scrollPane.getHorizontalScrollBar().setValue(100); 
		
		return scrollPane;
	}
}



