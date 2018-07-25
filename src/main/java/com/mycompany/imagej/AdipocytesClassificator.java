
package com.mycompany.imagej;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ij.plugin.*;
import ij.plugin.frame.*;

public class AdipocytesClassificator implements PlugIn{
	
	
	Map <Double,String> resultsClassifier = new HashMap<>();
	private String simpleThresholdMethod = "Huang";
	private int simpleMinSize = 1000;
	private int simpleMaxSize = 7000;
	private int preMinSize = 40;
	private int preMaxSize = 20000;
	private String preThresholdMethod = "Percentile";
	private int preNumberOfDilates = 10;
	private int oldForegroundColor;
	private int oldBackgroundColor;
	
	public boolean removeScale (ImageProcessor ip){
		try{
			ip.scale(0, 0);
			return true;
		}
		catch(Exception e){
			return false;
		}
	}
	
	public void clearBackground(ImagePlus imp,int preMinSize,int preMaxSize,String preThresholdMethod,int numberOfDilates){
		
		IJ.run(imp,"Find Edges","");
		IJ.run(imp,"8-bit","");
		IJ.run(imp,"Smooth","");
		IJ.run(imp,"Invert","");
		IJ.setAutoThreshold(imp,preThresholdMethod + " dark");
		IJ.run(imp,"Analyze Particles...", "size="+simpleMinSize+"-"+simpleMaxSize+" circularity=0.00-1.00 show=Masks exclude in_situ");
		IJ.run(imp,"Create Selection","");
		IJ.run(imp,"Enlarge...", "enlarge=" + preNumberOfDilates);
		IJ.run(imp,"Revert","");
		IJ.run(imp,"Clear Outside","");
		imp.show();
	
	}
	
	
	public void storeColors (ImagePlus imp){
		oldBackgroundColor = IJ.COLOR;
	}
	
	public void setWhiteOnBlack (ImagePlus imp){
		IJ.setBackgroundColor(0, 0, 0);
		IJ.setForegroundColor(255, 255, 255);
	}
	
	public void setBlackOnWhite (ImagePlus imp){
		IJ.setBackgroundColor(255,255, 255);
		IJ.setForegroundColor(0, 0, 0);
	}
	
	public void simpleSegmentation(String file){
		
		ImagePlus imp = IJ.openImage(file);
		ImageProcessor ip = imp.getProcessor();
	
		
		
		IJ.run("ROI Manager...","Show All with labels");

//		 setBatchMode(true);
//		 roiManager("reset");
//		 storeColors();
		
         setWhiteOnBlack(imp);  	
		 IJ.run(imp,"Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
		 clearBackground(imp,preMinSize, preMaxSize, preThresholdMethod, preNumberOfDilates);
		 IJ.run(imp,"Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
		 IJ.run(imp,"Find Edges","");
		 IJ.run(imp,"8-bit","");
		 IJ.run(imp,"Smooth","");
		 IJ.run(imp,"Invert","");
		 
		 ip.setAutoThreshold(simpleThresholdMethod+ " dark");
		 
		 IJ.run("Convert to Mask");
		 
		 IJ.run(imp,"Watershed","");
		 imp.show();
		 IJ.run("Clear Results");
		 IJ.run("Analyze Particles...", "size="+simpleMinSize+"-"+simpleMaxSize+" circularity=0.00-1.00 show=Nothing add exclude");
		 IJ.run("Revert");
		 IJ.run("Set Scale...", "distance=0 known=0 pixel=1 unit=pixel");
		     
		 //resetColors();
		 //setBatchMode("exit and display");
		 
		 
//		IJ.selectWindow("Results");
//		IJ.run("Close");
		 
		IJ.run("ROI Manager...","Show All with labels");

	}
	 
	
	
	
	
	public void displayResults (Map <Double, String> justaMap)
	{
		
		for (Map.Entry<Double, String> entry : justaMap.entrySet())
			
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
	}
	
	public double mean (int[] histogram)
	{

		double sum = 0;
		double sum2 = 0; 

		for (int i=0 ; i<histogram.length ; i++)
		{
			sum += histogram[i] * i ;
			sum2 += histogram[i];
		}
		
		return sum/sum2;	
	}
	
	public double getHistogramData (String file) 
	{
		
		ImagePlus imp = IJ.openImage(file);
		IJ.run(imp, "Histogram", "");
		ImageProcessor ip = imp.getProcessor();
		int[] imagehist = ip.getHistogram();
		IJ.run("Close");
		return mean(imagehist);
	
	}
	
	
	public void classifyAdipocytes (String rootfolder)
	{
		double histogramMean = 0; 
		File root = new File (rootfolder);
		File[] list = root.listFiles();
		for (int i = 1; i < list.length; i+=3){
			
				System.out.println(histogramMean=getHistogramData(list[i].getPath()));
				
				if (histogramMean <= 110) 
				{
					resultsClassifier.put(histogramMean, "Dark");
					simpleSegmentation(list[i].getPath());
					
				}
				if (histogramMean > 110 && histogramMean <=160)
				{
					resultsClassifier.put(histogramMean, "Medium");
					simpleSegmentation(list[i].getPath());
				}
				if (histogramMean > 160)
				{
					resultsClassifier.put(histogramMean, "Light");
					simpleSegmentation(list[i].getPath());
				}
				
		}
		
		displayResults(resultsClassifier);
	
	}

	@Override
	public void run(String arg0) 
	{
		IJ.run("Install...", "install=[/Users/mac/Desktop/IC/IC-Claudia & Andrea/adiposoft/MRI_Adipocyte_Tools.ijm]");
		classifyAdipocytes("/Users/mac/Desktop/IC/IC-Claudia & Andrea/Imagens Adipócitos/test_folder/");
		System.out.println("Everything is OK !");
			
	}
	
	public static void main(String[] args)
	{
		new ij.ImageJ();
		new AdipocytesClassificator().run("");
	}

}
