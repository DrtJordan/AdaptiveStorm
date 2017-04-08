package ruc.edu.components;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.data.time.Millisecond;
import org.jfree.ui.RefineryUtilities;

import ruc.edu.core.AdaptiveStorm;
import ruc.edu.core.AllTPCHProducer;
import ruc.edu.tools.ComponentMetric;
import ruc.edu.window.EmpiricalSettingWindow;
import ruc.edu.window.LoadingWindow;
import ruc.edu.window.UserWindow;

public class SettingPanel extends JPanel implements ActionListener{

	public AdaptiveStorm adaptiveStorm;
	public SettingPanel anotherPanel = null;			// 它的panle克隆 内容均需保持统一
	private static final long serialVersionUID = -3371941485026694783L;
	public JComboBox<String> regressionAlgCb = null;
	public JComboBox<String> classifiAlgCb = null;
	public JTextField refreshInterval = null;
	public JTextField maxDRate = null;
	public JTextField checkPoints = null;
	public JTextField maxLatency = null;
	public JComboBox<String> dataRatecomboBox = null;
	public JTextField logPath = null;
	public AllTPCHProducer producer = null;
	
	public void setClonePanel( SettingPanel anotherPanel) {
		this.anotherPanel = anotherPanel;
	}
	
	public SettingPanel( AdaptiveStorm adaptiveStorm) {
		
		this.adaptiveStorm = adaptiveStorm;
		// 设置setting panel
		setLayout(new GridLayout(5,4,15,15));
		
		// 回归算法选择
		JLabel lblRegressionAlgorithm = new JLabel("Regression Algorithm:");
		add(lblRegressionAlgorithm);

		regressionAlgCb = new JComboBox<String>(new String[]{"J48"});
		add(regressionAlgCb);
		
		// 分类算法选择
		JLabel label = new JLabel("Classification Algorithm:");
		add(label);
		
		classifiAlgCb = new JComboBox<String>(new String[]{"Multilayer Perceptron"});
		add(classifiAlgCb);
		
		// 样本收集间隔时间
		JLabel lblNewLabel = new JLabel("Refreshing Interval (Secs):");
		add(lblNewLabel);
		
		refreshInterval = new JTextField("6");
		add(refreshInterval);
		refreshInterval.setColumns(10);
		
		// 吞吐差值阈值（超过则触发模型重新选择参数）
		JLabel label_1 = new JLabel("Threshold of Max Different Rates (Tuples/s):");
		add(label_1);
		
		maxDRate = new JTextField("40000");
		add(maxDRate);
		maxDRate.setColumns(10);
		
		// 检查点间隔时间
		JLabel lblDistanceOfTwo = new JLabel("Checkpoints Interval (Secs):");
		add(lblDistanceOfTwo);
		
		checkPoints = new JTextField("30");
		add(checkPoints);
		checkPoints.setColumns(10);
		
		// 用户定义最高延时
		JLabel lblLatencyDeadline = new JLabel("Latency Deadline (ms):");
		add(lblLatencyDeadline);
		
		maxLatency = new JTextField("1500");
		add(maxLatency);
		maxLatency.setColumns(10);
		
		// 用户定义数据流速
		JLabel lblDataRate = new JLabel("Data Producing Rate Level:");
		add(lblDataRate);

		dataRatecomboBox = new JComboBox<String>(new String[]{"1", "2", "3", "4"
				, "5", "6","7", "8", "9","10"});
		dataRatecomboBox.setSelectedIndex(9);
		add(dataRatecomboBox);
		
		// log路径
		JLabel lblLogPaht = new JLabel("Log Path:");
		add(lblLogPaht);
		
		logPath = new JTextField("/Users/Logs/log.txt");
		add(logPath);
		logPath.setColumns(10);
		
		// 添加4个按钮
		final JButton button1 = new JButton("Choose Jar File");
		button1.setActionCommand("Choose_Jar_File");
		button1.addActionListener(this);
        
        // 添加4个按钮
 		final JButton button2 = new JButton("Start Producer");
 		button2.setActionCommand("Start_Producer");
 		button2.addActionListener(this);
 		
 		final JButton button3 = new JButton("Apply Change");
 		button3.setActionCommand("Apply_Change");
 		button3.addActionListener(this);
 		
 		final JButton button4 = new JButton("Change Empirical Config");
 		button4.setActionCommand("Change_Empirical");
 		button4.addActionListener(this);

 		add(button1);
 		add(button2);
 		add(button3);
 		add(button4);
		
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if( e.getActionCommand().equals("Choose_Jar_File")) {
			// 文件选取按钮被触发
		    JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Jar Files", "jar");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showOpenDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: " +
		            chooser.getSelectedFile().getAbsolutePath());
		       // 显示load对话框
		       final LoadingWindow loading = new LoadingWindow();
		       loading.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		       loading.setUndecorated(true);
		       loading.setLocationRelativeTo(null);
		       loading.pack();
		       RefineryUtilities.centerFrameOnScreen(loading);
		       loading.setVisible(true);
		       final String fileName = chooser.getSelectedFile().getName();
		       
		       new Thread(new Runnable() {
		           public void run() {
		               // 启动后台线程  并显示 loading对话框
					   adaptiveStorm.startStorm(fileName);
		               // when loading is finished, make frame disappear
		               SwingUtilities.invokeLater(new Runnable() {
		                   public void run() {
		                	   loading.setVisible(false);
		                   }
		               });

		           }
		       }).start();
		    }
		  
		}
		else if( e.getActionCommand().equals("Start_Producer")) {
			// 启动数据发送线程
			if( producer == null) {
				
				final LoadingWindow loading = new LoadingWindow();
				loading.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				loading.setUndecorated(true);
				loading.setLocationRelativeTo(null);
				loading.pack();
				RefineryUtilities.centerFrameOnScreen(loading);
				loading.setVisible(true);
				new Thread(new Runnable() {
		           public void run() {
		               // 启动后台线程  并显示 loading对话框
		        	   // 开始random配置
		        	   
		        	   producer = new AllTPCHProducer( adaptiveStorm);
		        	   producer.startProducing();
						// 开启自动配置定时器任务
		        	   adaptiveStorm.startAdaStorm();
		        	   anotherPanel.producer = producer;
		        	   try {
		        		   Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		               // when loading is finished, make frame disappear
		               SwingUtilities.invokeLater(new Runnable() {
		                   public void run() {
		                	   loading.setVisible(false);
		                   }
		               });

		           }
				}).start();
				
			}

		}
		else if( e.getActionCommand().equals("Apply_Change")) {
			// refreshing Interval
			if( !refreshInterval.getText().equals(anotherPanel.refreshInterval.getText())) {
				// refreshInterval被修改了
				String newInteval = refreshInterval.getText();
				// show dialge
				JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, 
						"Refresh interval change to: " + newInteval);
				
				int newInterval = Integer.valueOf(newInteval) * 1000;
				anotherPanel.refreshInterval.setText(newInteval);
				adaptiveStorm.drawTimer.cancel();
				adaptiveStorm.drawTimer = new Timer();
				adaptiveStorm.drawTimer.schedule(new DrawTimerTask( new ComponentMetric[]{
						adaptiveStorm.spoutMetric,
						adaptiveStorm.onBoltMetric, adaptiveStorm.joinBoltMetric, adaptiveStorm.kafkaMetric},
						adaptiveStorm.plots, adaptiveStorm), newInterval, newInterval);
				producer.changeRefreshInterval(newInterval);
			}
			// checkpoints interval
			if( !checkPoints.getText().equals(anotherPanel.checkPoints.getText())) {
				// checkpoints interval被修改了
				String newInteval = checkPoints.getText();
				int newInterval = Integer.valueOf(newInteval) * 1000;
				JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, 
						"Checkpoints interval change to: " + newInteval);
				
				anotherPanel.checkPoints.setText(newInteval);
				adaptiveStorm.adaStormTimer.cancel();
				adaptiveStorm.adaStormTimer = new Timer();
				adaptiveStorm.adaStormTimer.schedule(new ChooseOPTimerTask( new ComponentMetric[]{ 
						adaptiveStorm.spoutMetric, adaptiveStorm.onBoltMetric,
						adaptiveStorm.joinBoltMetric, adaptiveStorm.kafkaMetric}, adaptiveStorm), newInterval,
						newInterval);
			}
			// data Rate Combo
			if( dataRatecomboBox.getSelectedIndex() != anotherPanel.dataRatecomboBox.getSelectedIndex()) {
				// data rate level 被修改了
				int newLevel = Integer.valueOf((String) dataRatecomboBox.getSelectedItem());
				JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, 
						"Data rate level change to: " + newLevel);
				anotherPanel.dataRatecomboBox.setSelectedIndex(dataRatecomboBox.getSelectedIndex());
				adaptiveStorm.logs[1].append("change spout to level " + newLevel + "\n");
				adaptiveStorm.logs[3].append("change spout to level " + newLevel + "\n");
				producer.changeRateLevel(newLevel);
			}
			// threshold of Max D-Rates
			if(!maxDRate.getText().equals(anotherPanel.maxDRate.getText())) {
				// threshold of Max D-Rates 被修改了
				String newInteval = maxDRate.getText();
				JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, 
						"Max threshold change to: " + newInteval);
				int newInterval = Integer.valueOf(newInteval);
				anotherPanel.maxDRate.setText(newInteval);
				adaptiveStorm.maxDRate = newInterval;
			}
			// user defined latency
			if(!maxLatency.getText().equals(anotherPanel.maxLatency.getText())) {
				// threshold of Max D-Rates 被修改了
				String newInteval = maxLatency.getText();
				JOptionPane.showMessageDialog(adaptiveStorm.parentFrame, 
						"Max latency change to: " + newInteval);
				int newInterval = Integer.valueOf(newInteval);
				anotherPanel.maxLatency.setText(newInteval);
				adaptiveStorm.maxLatency = newInterval;
			}
			
		}
		else if( e.getActionCommand().equals("Change_Empirical")) {
			EmpiricalSettingWindow demo = new EmpiricalSettingWindow(adaptiveStorm);
			demo.pack();
			demo.setSize(new Dimension(339,331));
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(true);
		}
	}

}
