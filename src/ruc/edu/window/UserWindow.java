package ruc.edu.window;

import java.awt.EventQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;

import java.awt.event.InputMethodListener;
import java.awt.image.BufferedImage;
import java.awt.event.InputMethodEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Canvas;

import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.text.DefaultCaret;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ruc.edu.components.MJFreeChartPanel;
import ruc.edu.components.SettingPanel;
import ruc.edu.core.AdaptiveStorm;
import ruc.edu.core.Mlmodel;
import ruc.edu.tools.Tools;

public class UserWindow  extends ApplicationFrame implements ActionListener {

	private static final long serialVersionUID = -1479725239644074487L;
	private static final String title = "Adaptive Storm";
	public MJFreeChartPanel cpuChart;
	public MJFreeChartPanel memoryChart;
	public MJFreeChartPanel throughputChart;
	public MJFreeChartPanel latencyChart;
	private AdaptiveStorm adaptiveStorm;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		final UserWindow demo = new UserWindow(title);
		demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
	}

	/**
	 * Create the application.
	 */
	public UserWindow( final String title) {
		super(title);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// 初始化AdaptiveStorm
		adaptiveStorm = new AdaptiveStorm( this);

		// 所有pane的母pane tabbedPane
		JTabbedPane tp = new JTabbedPane();
		// 子pane
		SettingPanel panel_setting_1 = new SettingPanel(adaptiveStorm);		// 设置panel
		SettingPanel panel_setting_2 = new SettingPanel(adaptiveStorm);
		panel_setting_1.setClonePanel(panel_setting_2);
		panel_setting_2.setClonePanel(panel_setting_1);
		
		JPanel panel_CPUMem = new JPanel(new BorderLayout());			// CPU Memory图panel
		JPanel panel_latencyThrou = new JPanel(new BorderLayout());	// latency 
		//tp.addTab("Setting", panel_setting);
		tp.addTab("CPU/Memory", panel_CPUMem);
		// tp.addTab("cluster Memory", panel_clusterMem);
		// tp.addTab("throughput", panel_throughput);
		tp.addTab("Throughput/Latency", panel_latencyThrou);
		panel_CPUMem.add(panel_setting_1, BorderLayout.NORTH);
		panel_latencyThrou.add(panel_setting_2, BorderLayout.NORTH);
		
		// 下面添加4个plots
		cpuChart = new MJFreeChartPanel("CPU", "Utilization", "Tuples/s (x10K)", "CPU");
		memoryChart = new MJFreeChartPanel("Memory", "GB", "Tuples/s (x10K)", "Memory");
		throughputChart = new MJFreeChartPanel("Throughput", "Tuples/s (x10K)", null, "Throughput");
		latencyChart = new MJFreeChartPanel("Latency", "ms", "Tuples/s (x10K)", "Latency");
		// 将两个图放进一个panel里 之后一起放入总panel里
		JPanel cpuMemCharts = new JPanel();
		cpuMemCharts.add(cpuChart.getChartPanel());
		cpuMemCharts.add(memoryChart.getChartPanel());
		panel_CPUMem.add(cpuMemCharts);
		
		JPanel thrLantCharts = new JPanel();
		thrLantCharts.add(throughputChart.getChartPanel());
		thrLantCharts.add(latencyChart.getChartPanel());
		panel_latencyThrou.add(thrLantCharts);
		
		// 4个log区域
		JTextArea ta_1_storm = new JTextArea(7,10);
		DefaultCaret dc_1_storm = (DefaultCaret)ta_1_storm.getCaret();
		dc_1_storm.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JTextArea ta_1_kafka = new JTextArea(7,10);
		DefaultCaret dc_1_kafka = (DefaultCaret)ta_1_kafka.getCaret();
		dc_1_kafka.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JTextArea ta_2_storm = new JTextArea(7, 10);
		DefaultCaret dc_2_storm = (DefaultCaret)ta_2_storm.getCaret();
		dc_2_storm.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JTextArea ta_2_kafka = new JTextArea(7, 10);
		DefaultCaret dc_2_kafka = (DefaultCaret)ta_2_kafka.getCaret();
		dc_2_kafka.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// log区域的style
		ta_1_storm.setLineWrap(true);
		ta_1_storm.setWrapStyleWord(true);
        
		ta_1_kafka.setLineWrap(true);
		ta_1_kafka.setWrapStyleWord(true);
        
		ta_2_storm.setLineWrap(true);
		ta_2_storm.setWrapStyleWord(true);
        
		ta_2_kafka.setLineWrap(true);
		ta_2_kafka.setWrapStyleWord(true);
		
		// 在log外封装scroll控件
		JScrollPane scroll1 = new JScrollPane(ta_1_storm);
		JScrollPane scroll2 = new JScrollPane(ta_1_kafka);
		JScrollPane scroll3 = new JScrollPane(ta_2_storm);
		JScrollPane scroll4 = new JScrollPane(ta_2_kafka);
		
		// 添加标题
		scroll1.setBorder(
	            BorderFactory.createCompoundBorder(
	                BorderFactory.createCompoundBorder(
	                                BorderFactory.createTitledBorder("Storm Consumer"),
	                                BorderFactory.createEmptyBorder(3,5,3,5)),
	                                scroll1.getBorder()));
		scroll2.setBorder(
	            BorderFactory.createCompoundBorder(
	                BorderFactory.createCompoundBorder(
	                                BorderFactory.createTitledBorder("Kafka Producer"),
	                                BorderFactory.createEmptyBorder(3,5,3,5)),
	                                scroll2.getBorder()));
		scroll3.setBorder(
	            BorderFactory.createCompoundBorder(
	                BorderFactory.createCompoundBorder(
	                                BorderFactory.createTitledBorder("Storm Consumer"),
	                                BorderFactory.createEmptyBorder(3,5,3,5)),
	                                scroll3.getBorder()));
		scroll4.setBorder(
	            BorderFactory.createCompoundBorder(
	                BorderFactory.createCompoundBorder(
	                                BorderFactory.createTitledBorder("Kafka Producer"),
	                                BorderFactory.createEmptyBorder(3,5,3,5)),
	                                scroll4.getBorder()));
		
		// 两两放入临时panel里
		JPanel tempPanel1 = new JPanel(new GridLayout(1,2));
		tempPanel1.add(scroll1);
		tempPanel1.add(scroll2);
		
		JPanel tempPanel2 = new JPanel(new GridLayout(1,2));
		tempPanel2.add(scroll3);
		tempPanel2.add(scroll4);
		
		panel_CPUMem.add(tempPanel1, BorderLayout.SOUTH);
		panel_latencyThrou.add(tempPanel2, BorderLayout.SOUTH);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 设置内容pane为tp
		setContentPane(tp);
		
		// ********************界面部分结束*********************
		// 向adaptiveStorm里传入log控件
		adaptiveStorm.setLogPanels(new JTextArea[]{ta_1_storm, ta_1_kafka, ta_2_storm, ta_2_kafka}, 
				new MJFreeChartPanel[]{ cpuChart, memoryChart, throughputChart, latencyChart});
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	} 
}
