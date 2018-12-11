package view;

import javax.swing.*;

public class MainWindow {
	
	private static JFrame jFrame;

	private static JPanel jPanel;

	private static JLable hotKey1,hotKey2,hotKey3,hotKey4,hotKey5,hotKey6;

	private static JTextField setKey1,setKey1,setKey1,setKey1,setKey1,setKey1,
	
	public MainWindow() {
		createMainUI();
	}

	private void createMainUI() {
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		
		jFrame = new JFrame("Maestro window");
		jFrame.setSize(350,200);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		jPanel = new JPanel();
		
		jFrame.add(jPanel);
		//jFrame.pack();
		jFrame.setVisible(true);
	}

	private setComponents(JPanel jPanel) {
		JLable subTitle = new JLable("set hotkey");
		
	}
	
	public JFrame getJFrame() {
		return jFrame;
	}
}
