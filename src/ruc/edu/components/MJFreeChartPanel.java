package ruc.edu.components;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class MJFreeChartPanel {
	
	// 三个曲线数据序列
    private TimeSeries dataRateSeries;
    private TimeSeries empiricalSeries;
    private TimeSeries adaStormSeries;
    private TimeSeries userDefinedSeries;
    private ChartPanel chartPanel;
    
	public MJFreeChartPanel( String plotTitle, String yAxisName, String yAxisName2) {
		
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
	    plot.setBackgroundPaint(Color.lightGray);
	    plot.setDomainGridlinePaint(Color.white);
	    plot.setRangeGridlinePaint(Color.white);
//	      plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 4, 4, 4, 4));
	    plot.setDataset(1, dateRateDataset);
	    plot.setDataset(2, empiricalDataset);
	    //plot.setDataset(3, dateRateDataset);
	    // 第一个y轴对象
	    final ValueAxis axis = plot.getDomainAxis();
	    axis.setAutoRange(true);
	    axis.setFixedAutoRange(60000.0);  // 60 seconds
	    // 设置第二个y轴以及对应的数据库
	    final NumberAxis rangeAxis2 = new NumberAxis(yAxisName2);
	    rangeAxis2.setAutoRangeIncludesZero(false);
	    plot.setRenderer(1, new DefaultXYItemRenderer());
	    plot.setRangeAxis(1, rangeAxis2);
	    // 设置dataset对应的y轴是哪一个
	    plot.mapDatasetToRangeAxis(1, 1);

	    chartPanel = new ChartPanel(chart);
		
	}
	
	public ChartPanel getChartPanel() {
		return chartPanel;
	}
 
}
