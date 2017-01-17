package ruc.edu.window;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadingWindow extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3317101712961032605L;
	public LoadingWindow() {
		final JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);
	    final JPanel contentPane = new JPanel();
	    contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
	    contentPane.setLayout(new BorderLayout());
	    contentPane.add(new JLabel("Loading..."), BorderLayout.NORTH);
	    contentPane.add(progressBar, BorderLayout.CENTER);
	    setContentPane(contentPane);
	    
	}
   
}
