package cn.simple.jcom.assist;

import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * 串口接收工具
 */
public class App {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		MainFrame mainFrame = new MainFrame();
		mainFrame.setTitle("串口接收工具");
		mainFrame.setSize(800, 600);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}
}
