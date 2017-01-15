package ruc.edu.components;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jfree.data.time.Millisecond;

import ruc.edu.core.AdaptiveStorm;
import ruc.edu.core.AllTPCHProducer;

public class SettingPanel extends JPanel implements ActionListener{

	private AdaptiveStorm adaptiveStorm;
	private static final long serialVersionUID = -3371941485026694783L;
	private JComboBox<String> regressionAlgCb = null;
	private JComboBox<String> classifiAlgCb = null;
	private JTextField collectInterval = null;
	private JTextField maxDRate = null;
	private JTextField checkPoints = null;
	private JTextField maxLatency = null;
	private JComboBox<String> dataRatecomboBox = null;
	private JTextField logPath = null;
	private AllTPCHProducer producer = null;
	
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
		JLabel lblNewLabel = new JLabel("Collecting Interval (Mins):");
		add(lblNewLabel);
		
		collectInterval = new JTextField("3");
		add(collectInterval);
		collectInterval.setColumns(10);
		
		// 吞吐差值阈值（超过则触发模型重新选择参数）
		JLabel label_1 = new JLabel("Threshold of Max Different Rates:");
		add(label_1);
		
		maxDRate = new JTextField("30000");
		add(maxDRate);
		maxDRate.setColumns(10);
		
		// 检查点间隔时间
		JLabel lblDistanceOfTwo = new JLabel("Distance of Two Checkpoints (Mins):");
		add(lblDistanceOfTwo);
		
		checkPoints = new JTextField("2");
		add(checkPoints);
		checkPoints.setColumns(10);
		
		// 用户定义最高延时
		JLabel lblLatencyDeadline = new JLabel("Latency Deadline:");
		add(lblLatencyDeadline);
		
		maxLatency = new JTextField("1000");
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
 		
 		final JButton button4 = new JButton("Write Logs");
 		button4.setActionCommand("Write_Logs");
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
		       // 得到文件名字 并启动adaStorm
		       adaptiveStorm.startStorm(chooser.getSelectedFile().getName());
		    }
		}
		else if( e.getActionCommand().equals("Start_Producer")) {
			// 启动数据发送线程
			producer = new AllTPCHProducer( adaptiveStorm);
			producer.startProducing();
			// 开启自动配置定时器任务
			adaptiveStorm.startAdaStorm();

		}
		else if( e.getActionCommand().equals("Apply_Change")) {
			// 应用修改的配置
			int newLevel = Integer.valueOf((String) dataRatecomboBox.getSelectedItem());
			adaptiveStorm.logs[1].append("change spout to level " + newLevel + "\n");
			adaptiveStorm.logs[3].append("change spout to level " + newLevel + "\n");
			producer.changeRateLevel(newLevel);
		}
		else if( e.getActionCommand().equals("Write_Logs")) {
			adaptiveStorm.plots[2].adaStormSeries.add(new Millisecond(), new Random().nextInt(50));

		}
	}

}
