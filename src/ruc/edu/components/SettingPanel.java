package ruc.edu.components;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import ruc.edu.core.AdaptiveStorm;

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
	
	public SettingPanel( AdaptiveStorm adaptiveStorm) {
		
		this.adaptiveStorm = adaptiveStorm;
		// ����setting panel
		setLayout(new GridLayout(5,4,15,15));
		
		// �ع��㷨ѡ��
		JLabel lblRegressionAlgorithm = new JLabel("Regression Algorithm:");
		add(lblRegressionAlgorithm);

		regressionAlgCb = new JComboBox<String>(new String[]{"J48"});
		add(regressionAlgCb);
		
		// �����㷨ѡ��
		JLabel label = new JLabel("Classification Algorithm:");
		add(label);
		
		classifiAlgCb = new JComboBox<String>(new String[]{"Multilayer Perceptron"});
		add(classifiAlgCb);
		
		// �����ռ����ʱ��
		JLabel lblNewLabel = new JLabel("Collecting Interval (Mins):");
		add(lblNewLabel);
		
		collectInterval = new JTextField("3");
		add(collectInterval);
		collectInterval.setColumns(10);
		
		// ���²�ֵ��ֵ�������򴥷�ģ������ѡ�������
		JLabel label_1 = new JLabel("Threshold of Max Different Rates:");
		add(label_1);
		
		maxDRate = new JTextField("20000");
		add(maxDRate);
		maxDRate.setColumns(10);
		
		// ������ʱ��
		JLabel lblDistanceOfTwo = new JLabel("Distance of Two Checkpoints (Mins):");
		add(lblDistanceOfTwo);
		
		checkPoints = new JTextField("2");
		add(checkPoints);
		checkPoints.setColumns(10);
		
		// �û����������ʱ
		JLabel lblLatencyDeadline = new JLabel("Latency Deadline:");
		add(lblLatencyDeadline);
		
		maxLatency = new JTextField("2000");
		add(maxLatency);
		maxLatency.setColumns(10);
		
		// �û�������������
		JLabel lblDataRate = new JLabel("Data Producing Rate Level:");
		add(lblDataRate);

		dataRatecomboBox = new JComboBox<String>(new String[]{"1", "2", "3", "4"
				, "5", "6","7", "8", "9","10"});
		add(dataRatecomboBox);
		
		// log·��
		JLabel lblLogPaht = new JLabel("Log Path:");
		add(lblLogPaht);
		
		logPath = new JTextField("/Users/Logs/log.txt");
		add(logPath);
		logPath.setColumns(10);
		
		// ���4����ť
		final JButton button1 = new JButton("Choose Jar File");
		button1.setActionCommand("Choose_Jar_File");
		button1.addActionListener(this);
        
        // ���4����ť
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
			// �ļ�ѡȡ��ť������
		    JFileChooser chooser = new JFileChooser();
		    FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Jar Files", "jar");
		    chooser.setFileFilter(filter);
		    int returnVal = chooser.showOpenDialog(this);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: " +
		            chooser.getSelectedFile().getAbsolutePath());
		       // �õ��ļ����� ������adaStorm
		       adaptiveStorm.startStorm(chooser.getSelectedFile().getName());
		    }
		}
		else if( e.getActionCommand().equals("Start_Producer")) {
			// ��wamdm12������producer
			Process process;
			try {
				process = Runtime
						.getRuntime()
						.exec(new String[] {
								"bash",
								"-c",
								"ssh wamdm12 \"source /etc/profile ; cd ~/wengzujian/ ;"
										+ "java -jar tpchproducer.jar && exit\" "});
				process.waitFor();
			} catch (IOException er) {
				// TODO Auto-generated catch block
				er.printStackTrace();
			} catch (InterruptedException er) {
				// TODO Auto-generated catch block
				er.printStackTrace();
			}
		}
		else if( e.getActionCommand().equals("Apply_Change")) {
			// Ӧ���޸ĵ�����
			
		}
	}

}
