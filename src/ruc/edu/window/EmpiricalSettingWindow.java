package ruc.edu.window;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import ruc.edu.components.MJFreeChartPanel;
import ruc.edu.core.AdaptiveStorm;

public class EmpiricalSettingWindow  extends JFrame implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9106949745534738792L;
	private static final String title = "Empirical Configurations";
	public JTextField workerNumJT = null;
	public JTextField spoutNumJT = null;
	public JTextField onBoltNumJT = null;
	public JTextField joinBoltNumJT = null;
	public JTextField windowLengthNumJT = null;
	public AdaptiveStorm adaptiveStorm = null;
	
	public EmpiricalSettingWindow(AdaptiveStorm adaptiveStorm) {
		super(title);
		// TODO Auto-generated constructor stub
		this.adaptiveStorm = adaptiveStorm;
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(null);
		
		// worker number
		JLabel lblNewLabel = new JLabel("Worker Number:");
		lblNewLabel.setBounds(50, 35, 126, 15);
		contentPanel.add(lblNewLabel);
		
		workerNumJT = new JTextField(adaptiveStorm.workerNum);
		workerNumJT.setText(adaptiveStorm.workerNum + "");
		workerNumJT.setBounds(207, 32, 66, 21);
		contentPanel.add(workerNumJT);
		workerNumJT.setColumns(10);
		
		// spout number
		JLabel lblNewLabel2 = new JLabel("Spout Number:");
		lblNewLabel2.setBounds(50, 73, 126, 15);
		contentPanel.add(lblNewLabel2);
		
		spoutNumJT = new JTextField(adaptiveStorm.spoutNum);
		spoutNumJT.setText(adaptiveStorm.spoutNum + "");
		spoutNumJT.setBounds(207, 70, 66, 21);
		contentPanel.add(spoutNumJT);
		spoutNumJT.setColumns(10);
		
		// onbolt number
		JLabel lblNewLabel3 = new JLabel("OnBolt Number:");
		lblNewLabel3.setBounds(50, 110, 126, 15);
		contentPanel.add(lblNewLabel3);
		
		onBoltNumJT = new JTextField(adaptiveStorm.onBoltNum);
		onBoltNumJT.setText(adaptiveStorm.onBoltNum + "");
		onBoltNumJT.setBounds(207, 107, 66, 21);
		contentPanel.add(onBoltNumJT);
		onBoltNumJT.setColumns(10);
		
		// joinbolt number
		JLabel lblNewLabel4 = new JLabel("JoinBolt Number:");
		lblNewLabel4.setBounds(50, 144, 126, 15);
		contentPanel.add(lblNewLabel4);
		
		joinBoltNumJT = new JTextField(adaptiveStorm.joinBoltNum);
		joinBoltNumJT.setText(adaptiveStorm.joinBoltNum + "");
		joinBoltNumJT.setBounds(207, 141, 66, 21);
		contentPanel.add(joinBoltNumJT);
		joinBoltNumJT.setColumns(10);
		
		// windowLength
		JLabel lblNewLabel5 = new JLabel("Window Length:");
		lblNewLabel5.setBounds(50, 179, 147, 15);
		contentPanel.add(lblNewLabel5);
		
		windowLengthNumJT = new JTextField(adaptiveStorm.windowLength);
		windowLengthNumJT.setText(adaptiveStorm.windowLength + "");
		windowLengthNumJT.setBounds(207, 176, 66, 21);
		contentPanel.add(windowLengthNumJT);
		windowLengthNumJT.setColumns(10);
		
		JButton confirm = new JButton("Confirm");
		confirm.setBounds(50, 221, 99, 21);
		confirm.setActionCommand("confirm");
		confirm.addActionListener(this);
		contentPanel.add(confirm);
		
		JButton cancel = new JButton("Cancel");
		cancel.setBounds(172, 221, 101, 21);
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		contentPanel.add(cancel);
		
		setContentPane(contentPanel);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if( e.getActionCommand().equals("confirm")) {
			// 确认了
			int newWorkerNum = Integer.valueOf(workerNumJT.getText());
			int newSpoutNum = Integer.valueOf(spoutNumJT.getText());
			int newOnBoltNum = Integer.valueOf(onBoltNumJT.getText());
			int newJoinBoltNum = Integer.valueOf(joinBoltNumJT.getText());
			if( adaptiveStorm.workerNum != newWorkerNum || adaptiveStorm.spoutNum != newSpoutNum ||
					adaptiveStorm.onBoltNum != newOnBoltNum || adaptiveStorm.joinBoltNum != newJoinBoltNum) {
				// 改变了参数了  需要到empirical集群中kill掉旧的 重启新的集群
				adaptiveStorm.workerNum = newWorkerNum;
				adaptiveStorm.spoutNum = newSpoutNum;
				adaptiveStorm.onBoltNum = newOnBoltNum;
				adaptiveStorm.joinBoltNum = newJoinBoltNum;
				dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				// 显示load对话框
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
		        	   adaptiveStorm.changeEmpiricalParameters();
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
		else if( e.getActionCommand().equals("cancel")) {
			// 取消了
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
	}

}
