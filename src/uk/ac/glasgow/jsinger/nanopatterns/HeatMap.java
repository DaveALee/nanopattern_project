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

	double[][] data;
	Object[] xAxis;
	Object[] yAxis;

	public HeatMap(double[][] classInfo, Object[] patternNames, Object[] classNames)
	{

		data =  classInfo;
		xAxis = patternNames;
		yAxis = classNames;
	}

	public JScrollPane displayHeatMap()
	{

		//0 is the low value, 1 is high.
		//all data expected to be in the range 0-1
		HeatChart map = new HeatChart(data, 0, 1);

		map.setXAxisLabel("pattern");
		map.setYAxisLabel("class");

		//high and low value colours
		map.setHighValueColour(Color.GREEN);
		map.setLowValueColour(Color.WHITE);

		map.setTitle("Patterns Heat Map");

		//set the size of the cells
		map.setCellSize(new Dimension(20,20));

		map.setAxisValuesFont(new Font ("Courier", Font.BOLD, 14));

		map.setYValues(yAxis);
		map.setXValues(xAxis);
		// Step 3: Output the chart to a file.
		//map.saveToFile(new File("java-heat-chart.png"));


		//ouptut to a JFrame
		Image chart = map.getChartImage();


		JPanel panel = new JPanel();

		//chart is displayed as an ImageIcon of a JLabel
		JLabel chartImage = new JLabel(new ImageIcon(chart));
		panel.add(chartImage);		

		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		//set the default position of the bar to be to the right
		scrollPane.getHorizontalScrollBar().setValue(100); 
		
		return scrollPane;




	}



}



