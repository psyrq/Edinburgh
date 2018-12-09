package view;

import javax.swing.*;

public class MainWindow {
	
	private static JFrame jFrame;
	
	public MainWindow() {
		createMainUI();
	}

	private void createMainUI() {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		jFrame = new JFrame("Maestro window");
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JLabel jLable = new JLabel("set hotkey");
		jFrame.getContentPane().add(jLable);
		
		jFrame.pack();
		jFrame.setVisible(true);
	}
	
	public JFrame getJFrame() {
		return jFrame;
	}
}
