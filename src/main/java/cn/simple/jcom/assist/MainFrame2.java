package cn.simple.jcom.assist;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.swing.*;

import com.jgoodies.forms.layout.*;
/*
 * Created by JFormDesigner on Fri Aug 23 18:19:07 CST 2019
 */



/**
 * @author CodeCracker
 */
@SuppressWarnings({ "serial" })
public class MainFrame2 extends JFrame implements SerialPortEventListener {
	// 用户在界面上打开的端口
	private SerialPort sport = null;
	// 打开的串口的输入流
	private InputStream inputStream = null;
	// 窗体是否关闭的标志
	private boolean isClosing = false;
	// 关键字正则表达式
	private Pattern regex = null;
	// 字符集选择
	private Charset charset = Charset.forName("UTF-8");
	// 是否连接成功
	private volatile boolean success = false;

	public MainFrame2() {
		initComponents();
		initActions();
		initConfig();
		// ----------------------------------------
		// 窗体自己处理关闭事件处理串口的关闭
		// ----------------------------------------
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
	}

	/**
	 * 监听数据到来事件
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			if (!success) {
				try {
					int length = inputStream.available();
					if (length > 0) {
						byte[] bytes = new byte[length];
						inputStream.read(bytes);
						String str = new String(bytes, charset);
						if ("OK".equalsIgnoreCase(str)) {
							System.out.println("connect successful " + System.currentTimeMillis());
							this.success = true;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									label6.setText("OK");
									label6.setForeground(Color.red);
								}
							});
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			//JTextPane output = jBtnStopOut.isSelected() ? null : jCtlOutput;
			//boolean showHex = jCtlHexOut.isSelected();
			Packet packet = Objects.readData(inputStream, null, false, charset);
			if (packet != null) {
				byte[] bytes = packet.getRawData();
				long timestamp = packet.getTimestamp();

				// ----------------------------------------
				// 判断是否为1行的结束
				// ----------------------------------------
				System.out.println("insert to xml file now " + timestamp);
				String path = jCtlPath.getText();
				if (path != null && !path.isEmpty()) {
					Objects.saveLine2Xml(path, new String(bytes, charset), regex);
				}
			}
		}
	}

	/**
	 * 启用和禁用输入项编辑。打开端口后部分项目不能编辑
	 * 
	 * @param enable
	 */
	private void enableInput(boolean enable) {
		//jCtlCheckBit.setEnabled(enable);
		//jCtlDigitBit.setEnabled(enable);
		jCtlPath.setEnabled(enable);
		//jCtlRate.setEnabled(enable);
		//jCtlSaveInterval.setEnabled(enable);
		jCtlSerialPort.setEnabled(enable);
		//jCtlStopBit.setEnabled(enable);
		jCtlKeyword.setEnabled(enable);
		//jCtlCharset.setEnabled(enable);
		jBtnChangeDir.setEnabled(enable);
		jBtnSaveKeyword.setEnabled(enable);
	}

	/**
	 * 打开串口前先检查下输入项
	 * 
	 * @return
	 */
	private boolean checkInput() {
		String val = jCtlPath.getText();
		if (val.isEmpty()) {
			Objects.errorBox(this, "请选择【保存路径】");
			return false;
		}

		return true;
	}

	/**
	 * 绑定按钮事件
	 */
	private void initActions() {
		// 界面控件的初始化数据
		jCtlPath.setEnabled(false);// 用文件对话框选择

		// ----------------------------------------
		// 界面按钮事件监听
		// ----------------------------------------

		// 打开/关闭 串口 按钮
		jBtnOpenOrClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("open/close port clicked");
				if (!checkInput()) {
					return;
				}

				// 本次是关闭
				if (sport != null) {
					sport.removeEventListener();
					if (inputStream != null) {
						try {
							inputStream.close();
							inputStream = null;
						} catch (IOException ioe) {
						}
					}
					sport.close();
					sport = null;
					jBtnOpenOrClose.setText("打开串口");
					success = false;
					label6.setText("\u25cf");
					label6.setForeground(Color.black);
					enableInput(true);
				}
				// 本次是打开
				else {
					sport = Objects.openPort(makePortParams());
					String hello = jCtlHello.getText().trim();
					if (sport == null) {
						Objects.errorBox(MainFrame2.this, "打开串口失败");
						return;
					}
					try {
						inputStream = sport.getInputStream();
						Objects.sendData(sport, hello.getBytes(charset));
						Objects.addListener(sport, MainFrame2.this);
						jBtnOpenOrClose.setText("关闭串口");
						enableInput(false);
					} catch (Exception ex) {
						ex.printStackTrace();
						sport.close();
					}
				}
			}
		});

		// 改变文件保存路径按钮
		jBtnChangeDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("select path to save");
				String path = Objects.choosePath();
				if (path != null) {
					jCtlPath.setText(path);
				}
			}
		});

		// 保存关键字按钮
		jBtnSaveKeyword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String xmlfile = jCtlPath.getText();
				String keyword = jCtlKeyword.getText();
				String hello = jCtlHello.getText();
				if (xmlfile.isEmpty() || !new File(xmlfile).exists()) {
					Objects.errorBox(MainFrame2.this, "请选择【保存路径】");
					return;
				}
				if (keyword.isEmpty() || !keyword.matches("^[0-9A-Za-z]{1,}+(\\|[0-9A-Za-z]{1,})*$")) {
					Objects.errorBox(MainFrame2.this, "请填写【关键字】");
					return;
				}
				if (hello.isEmpty() || hello.trim().isEmpty()) {
					Objects.errorBox(MainFrame2.this, "请填写【问候语】");
					return;
				}
				Properties props = new Properties();
				props.put("xmlfile", xmlfile);
				props.put("keyword", keyword);
				props.put("hello", hello);
				Objects.saveConfig(props);
				// 正则表达式缓存用于写XML文件
				regex = Pattern.compile("(?=" + keyword + ")");
			}
		});
	}

	private SerialPortObject makePortParams() {
		SerialPortObject object = new SerialPortObject();
		// 串口名
		if (jCtlSerialPort.getItemCount() > 0) {
			String portName = (String) jCtlSerialPort.getSelectedItem();
			object.setPortName(portName);
			// 波特率
			String baudRate = "115200";
			object.setBaudRate(baudRate);
			// 数据位
			String dataBits = "8";
			object.setDataBits(dataBits);
			// 停止位
			String stopBits = "1";
			object.setStopBits(stopBits);
			// 校验位
			String parity = "None";
			object.setParity(parity);
			// 返回对象
			return object;
		} else {
			return null;
		}
	}

	/**
	 * 点击关闭按钮时做些清理工作
	 */
	@Override
	protected void processWindowEvent(final WindowEvent pEvent) {
		if (pEvent.getID() == WindowEvent.WINDOW_CLOSING) {
			if (!isClosing) {
				isClosing = true;
				success = false;
			} else {
				return;
			}

			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (sport != null) {
					sport.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// wait all writer finish
			Objects.pool.shutdown();
			System.exit(0);
		} else {
			super.processWindowEvent(pEvent);
		}
	}

	/**
	 * 对配置进行持久化
	 */
	private void initConfig() {
		Properties props = Objects.readConfig();
		jCtlPath.setText(props.get("xmlfile").toString());
		jCtlKeyword.setText(props.get("keyword").toString());
		jCtlHello.setText(props.get("hello").toString());
		if (!jCtlKeyword.getText().isEmpty()) {
			regex = Pattern.compile("(?=" + jCtlKeyword.getText() + ")");
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		jCtlSerialPort = new JComboBox(Objects.listPorts());
		label6 = new JLabel();
		jBtnOpenOrClose = new JButton();
		panel5 = new JPanel();
		label2 = new JLabel();
		jCtlKeyword = new JTextField();
		label3 = new JLabel();
		jCtlHello = new JTextField();
		jBtnChangeDir = new JButton();
		jCtlPath = new JTextField();
		jBtnSaveKeyword = new JButton();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== panel1 ========
		{
			panel1.setPreferredSize(new Dimension(240, 187));
			panel1.setLayout(new FormLayout(
				"default:grow",
				"2*($lgap, default), $lgap"));

			//======== panel2 ========
			{
				panel2.setLayout(new FormLayout(
					"$lcgap, default, $lcgap, default:grow, $lcgap",
					"2*(default, $lgap)"));

				//---- label1 ----
				label1.setText("串口");
				panel2.add(label1, CC.xy(2, 1));
				panel2.add(jCtlSerialPort, CC.xy(4, 1));

				//---- label6 ----
				label6.setText("\u25cf");
				panel2.add(label6, CC.xy(2, 3));

				//---- jBtnClose ----
				jBtnOpenOrClose.setText("打开串口");
				panel2.add(jBtnOpenOrClose, CC.xy(4, 3));
			}
			panel1.add(panel2, CC.xy(1, 2));

			//======== panel5 ========
			{
				panel5.setLayout(new FormLayout(
					"$lcgap, default, $lcgap, default:grow, $lcgap",
					"4*(default, $lgap)"));

				//---- label2 ----
				label2.setText("关键字");
				panel5.add(label2, CC.xy(2, 1));

				//---- jCtlKeyword ----
				jCtlKeyword.setPreferredSize(new Dimension(120, 23));
				panel5.add(jCtlKeyword, CC.xy(4, 1));

				//---- label3 ----
				label3.setText("问候语");
				panel5.add(label3, CC.xy(2, 3));
				panel5.add(jCtlHello, CC.xy(4, 3));

				//---- jBtnChangeDir ----
				jBtnChangeDir.setText("数据路径");
				panel5.add(jBtnChangeDir, CC.xy(2, 5));
				panel5.add(jCtlPath, CC.xy(4, 5));

				//---- jBtnSaveKeyword ----
				jBtnSaveKeyword.setText("保存配置");
				panel5.add(jBtnSaveKeyword, CC.xywh(2, 7, 3, 1));
			}
			panel1.add(panel5, CC.xy(1, 4));
		}
		contentPane.add(panel1, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JComboBox jCtlSerialPort;
	private JLabel label6;
	private JButton jBtnOpenOrClose;
	private JPanel panel5;
	private JLabel label2;
	private JTextField jCtlKeyword;
	private JLabel label3;
	private JTextField jCtlHello;
	private JButton jBtnChangeDir;
	private JTextField jCtlPath;
	private JButton jBtnSaveKeyword;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}

