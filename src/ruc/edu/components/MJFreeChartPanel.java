package ruc.edu.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import ruc.edu.tools.MXYLineAndShapeRender;

public class MJFreeChartPanel {
	
	// 三个曲线数据序列
	public TimeSeries dataRateSeries;
	public TimeSeries empiricalSeries;
	public TimeSeries adaStormSeries;
	public TimeSeries userDefinedSeries;
	public ChartPanel chartPanel;
    
	public MJFreeChartPanel( String plotTitle, String yAxisName, String yAxisName2 , String type) {
		
		dataRateSeries = new TimeSeries("Data Rate");
		empiricalSeries = new TimeSeries("Empirical");
		adaStormSeries = new TimeSeries("AdaStorm");
		userDefinedSeries = new TimeSeries("Deadline");
		
		final TimeSeriesCollection dateRateDataset = new TimeSeriesCollection(this.dataRateSeries);
		final TimeSeriesCollection empiricalDataset = new TimeSeriesCollection(this.empiricalSeries);
		final TimeSeriesCollection adaStormDataset = new TimeSeriesCollection(this.adaStormSeries);
		final TimeSeriesCollection userDefinedDataset = new TimeSeriesCollection(this.userDefinedSeries);
		JFreeChart chart = ChartFactory.createTimeSeriesChart(plotTitle, null, yAxisName,
				adaStormDataset, true, true, false);
		chart.setBackgroundPaint(Color.white);
	    // 曲线对象
	    final XYPlot plot = chart.getXYPlot();
	    plot.setBackgroundPaint(Color.white);
	    plot.setDomainGridlinePaint(Color.gray);
	    plot.setRangeGridlinePaint(Color.gray);
//	      plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));
	    plot.setDataset(1, dateRateDataset);
	    plot.setDataset(2, empiricalDataset);
	    //plot.setDataset(3, dateRateDataset);
	    // 第一个y轴对象
	    final ValueAxis axis = plot.getDomainAxis();
	    axis.setAutoRange(true);		// 不滑动
	    // 设置第二个y轴以及对应的数据库
          
	    // 设置3条线的颜色
	    plot.setRenderer(0, new MXYLineAndShapeRender(false));
	    plot.setRenderer(1, new MXYLineAndShapeRender(true));
	    plot.setRenderer(2, new MXYLineAndShapeRender(false));
	    // 设置dataset对应的y轴是哪一个
	    if( !type.equals("Throughput")) {
	    	final NumberAxis rangeAxis2 = new NumberAxis(yAxisName2);
		    rangeAxis2.setAutoRangeIncludesZero(false);
		    rangeAxis2.setRange(0, 30);
		    plot.setRangeAxis(1, rangeAxis2);
	    	plot.mapDatasetToRangeAxis(1, 1);
	    }
	    else {
	    	// throughput不需要第二个y轴
	    	plot.getRangeAxis().setRange(0, 40);
	    	plot.mapDatasetToRangeAxis(1, 0);
	    }
	    
	    if( type.equals("CPU")) {
	    	plot.getRangeAxis().setRange(0, 100);
	    }
	    else if( type.equals("Memory")) {
	    	plot.getRangeAxis().setRange(0, 60);
	    }
	    else if( type.equals("Latency")) {
	    	plot.getRangeAxis().setRange(0, 2000);
	    	// 用户定义的最高延时线
	    	plot.setDataset(3, userDefinedDataset);
	    	DefaultXYItemRenderer render4 = new DefaultXYItemRenderer();
		    render4.setShapesVisible(false);
		    render4.setBaseStroke(new BasicStroke(5));
		    plot.setRenderer(3, render4);
		    plot.getRenderer(3).setSeriesPaint(0, Color.black);
	    }
	    
	    plot.getRenderer(0).setSeriesPaint(0, new Color(237, 125, 49));
	    plot.getRenderer(1).setSeriesPaint(0, new Color(192, 0, 0));
	    plot.getRenderer(2).setSeriesPaint(0, new Color(91, 155, 213));
	    chartPanel = new ChartPanel(chart);
	    Dimension ds = chartPanel.getPreferredSize();
	    ds.setSize(ds.getWidth(), ds.getHeight() - 50);
	    chartPanel.setPreferredSize(ds);
		
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
 
}
