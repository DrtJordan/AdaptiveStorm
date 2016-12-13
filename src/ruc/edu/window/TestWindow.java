package ruc.edu.window;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;

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

public class TestWindow {

	private static Mlmodel mlModel = null;
	private JFrame frame;
	private Tools tools = null;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTextField textField_6;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestWindow window = new TestWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TestWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mlModel = new Mlmodel();
		tools = new Tools();
		frame = new JFrame();
		frame.setBounds(100, 100, 706, 479);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTabbedPane tp = new JTabbedPane();
		frame.setContentPane(tp);

		JPanel panel_setting = new JPanel();
		JPanel panel_clusterCPU = new JPanel();
		// JPanel panel_clusterMem = new JPanel();
		// JPanel panel_throughput = new JPanel();
		JPanel panel_latency = new JPanel();
		tp.addTab("Setting", panel_setting);
		tp.addTab("CPU/Memory", panel_clusterCPU);
		// tp.addTab("cluster Memory", panel_clusterMem);
		// tp.addTab("throughput", panel_throughput);
		tp.addTab("Throughput/Latency", panel_latency);

		BufferedImage CPUImage;
		BufferedImage memoryImage;
		BufferedImage throughputImage;
		BufferedImage latencyImage;
		try {
			CPUImage = ImageIO.read(new File("cpu.png"));
			memoryImage = ImageIO.read(new File("memory.png"));
			throughputImage = ImageIO.read(new File("throughput.png"));
			latencyImage = ImageIO.read(new File("latency.png"));
			panel_clusterCPU.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			// CPUImage = resizeImage(CPUImage, 680, 459);
			JLabel picLabel = new JLabel(new ImageIcon(CPUImage));
			//panel_clusterCPU.add(panel_setting);
			panel_clusterCPU.add(picLabel);
			JLabel memLabel = new JLabel(new ImageIcon(memoryImage));
			panel_clusterCPU.add(memLabel);

			JLabel thrLabel = new JLabel(new ImageIcon(throughputImage));
			panel_latency.add(thrLabel);
			JLabel latLabel = new JLabel(new ImageIcon(latencyImage));
			panel_latency.add(latLabel);

		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// setting panel
		JButton btnNewButton = new JButton("run storm topology");
		btnNewButton.setBounds(30, 244, 141, 23);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// run storm using shell
				Process process = null;
				List<String> processList = new ArrayList<String>();
				try {
					process = Runtime
							.getRuntime()
							.exec(new String[] {
									"bash",
									"-c",
									"ssh wamdm7 \"source /etc/profile ; cd ~/wengzujian/ ;"
											+ "storm jar StormTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar "
											+ "storm.starter.TPCHQuery3 tpchquery 12 12 12 12 30 10 && exit\" " });
					BufferedReader input = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
					String line = "";
					while ((line = input.readLine()) != null) {
						processList.add(line);
					}
					input.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				for (String templine : processList) {
					System.out.println(templine);
				}
			}
		});

		JButton btnGetcpuusage = new JButton("getCPUUsage");
		btnGetcpuusage.setBounds(180, 244, 99, 23);
		btnGetcpuusage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println("memory: " + tools.getMemoryUsage());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		JButton btnNewButton_2 = new JButton("Get CPU/Memory");
		btnNewButton_2.setBounds(40, 281, 117, 23);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("cpu:" + tools.getCpuUsage());
				System.out.println("memory:" + tools.getMemoryUsage());
			}
		});

		textField = new JTextField("spoutRate");
		textField.setBounds(180, 282, 66, 21);
		textField.setColumns(10);

		textField_1 = new JTextField("onBoltRate");
		textField_1.setBounds(267, 282, 66, 21);
		textField_1.setColumns(10);

		textField_2 = new JTextField("joinBoltRate");
		textField_2.setBounds(358, 282, 66, 21);
		textField_2.setColumns(10);
		panel_setting.setLayout(null);
		panel_setting.add(btnGetcpuusage);
		panel_setting.add(btnNewButton);
		panel_setting.add(btnNewButton_2);
		panel_setting.add(textField);

		JButton btnGetpredicted = new JButton("getPredicted");
		btnGetpredicted.setBounds(289, 244, 105, 23);
		btnGetpredicted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int[] result = mlModel.getOptimalParameters(new int[] {
						Integer.valueOf(textField.getText()),
						Integer.valueOf(textField_1.getText()),
						Integer.valueOf(textField_2.getText()) });
				System.out.println("optimalResult:" + result[0] + " "
						+ result[1] + " " + result[2] + " " + result[3]);
			}
		});

		panel_setting.add(btnGetpredicted);

		JButton btnNewButton_1 = new JButton("run producer");
		btnNewButton_1.setBounds(401, 244, 105, 23);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// start producing data
				AllTPCHProducer producer = new AllTPCHProducer();
				producer.startProducing();
			}
		});
		panel_setting.add(btnNewButton_1);
		panel_setting.add(textField_1);
		panel_setting.add(textField_2);
	
	}

	public static double getProcessCpuLoad() throws Exception {

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName
				.getInstance("java.lang:type=OperatingSystem");
		AttributeList list = mbs.getAttributes(name,
				new String[] { "ProcessCpuLoad" });

		if (list.isEmpty())
			return Double.NaN;

		Attribute att = (Attribute) list.get(0);
		Double value = (Double) att.getValue();

		// usually takes a couple of seconds before we get real values
		if (value == -1.0)
			return Double.NaN;
		// returns a percentage value with 1 decimal point precision
		return ((int) (value * 1000) / 10.0);
	}

	private BufferedImage resizeImage(BufferedImage originalImage, int width,
			int height) throws IOException {
		BufferedImage resizedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}
}
