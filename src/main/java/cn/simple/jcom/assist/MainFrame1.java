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
import javax.swing.text.Document;

import com.jgoodies.forms.layout.*;

/*
 * Created by JFormDesigner on Sat Jul 27 00:00:04 CST 2019
 */

/**
 * @author CodeCracker
 */
@SuppressWarnings({ "serial" })
public class MainFrame1 extends JFrame implements SerialPortEventListener {
	// 用户在界面上打开的端口
	private SerialPort sport = null;
	// 打开的串口的输入流
	private InputStream inputStream = null;
	// 上次收到数据的时间戳
//	private long lastTimestamp = 0;
	// 字符缓冲区内容用于写xml文件
	// private StringBuffer buffer = new StringBuffer(1024);
	// 窗体是否关闭的标志
	private boolean isClosing = false;
	// 关键字正则表达式
	private Pattern regex = null;
	// 字符集选择
	private Charset charset = Charset.forName("UTF-8");

	public MainFrame1() {
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
			JTextPane output = jBtnStopOut.isSelected() ? null : jCtlOutput;
			boolean showHex = jCtlHexOut.isSelected();
			Packet packet = Objects.readData(inputStream, output, showHex, charset);
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
		jCtlCheckBit.setEnabled(enable);
		jCtlDigitBit.setEnabled(enable);
		jCtlPath.setEnabled(enable);
		jCtlRate.setEnabled(enable);
		jCtlSaveInterval.setEnabled(enable);
		jCtlSerialPort.setEnabled(enable);
		jCtlStopBit.setEnabled(enable);
		jCtlKeyword.setEnabled(enable);
		jCtlCharset.setEnabled(enable);
		jBtnChangeDir.setEnabled(enable);
		jBtnSaveKeyword.setEnabled(enable);
	}

	/**
	 * 打开串口前先检查下输入项
	 * 
	 * @return
	 */
	private boolean checkInput() {
		// 检查保存时间
		String val = jCtlSaveInterval.getText();
		if (val.isEmpty()) {
			Objects.errorBox(this, Strings.get("please.input.save.time"));
			return false;
		} else if (!val.matches("^[1-9][0-9]*$")) {
			Objects.errorBox(this, Strings.get("save.time.must.be.digit"));
			return false;
		}
		val = jCtlPath.getText();
		if (val.isEmpty()) {
			Objects.errorBox(this, Strings.get("please.select.save.path"));
			return false;
		}

		return true;
	}

	/**
	 * 绑定按钮事件
	 */
	private void initActions() {
		// 界面控件的初始化数据
		ComboBoxModel model = jCtlRate.getModel();
		model.setSelectedItem(model.getElementAt(9));
		model = jCtlDigitBit.getModel();// 数据位:5
		model.setSelectedItem(model.getElementAt(3));
		model = jCtlCharset.getModel();
		model.setSelectedItem(model.getElementAt(0));
		jCtlPath.setEnabled(false);// 用文件对话框选择
		jCtlOutput.setEditable(false);// 只是输出不能编辑
		jCtlOutput.setFont(new java.awt.Font("Asia", 0, 14));

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
						} catch (IOException ioe) {
						}
					}
					sport.close();
					sport = null;
					jBtnOpenOrClose.setText(Strings.get("open.serial.port"));
					enableInput(true);
				}
				// 本次是打开
				else {
					sport = Objects.openPort(makePortParams());
					if (sport == null) {
						Objects.errorBox(MainFrame1.this, Strings.get("failed.to.open.port"));
						return;
					}
					try {
						charset = Charset.forName((String) jCtlCharset.getSelectedItem());
						inputStream = sport.getInputStream();
						Objects.addListener(sport, MainFrame1.this);
						jBtnOpenOrClose.setText(Strings.get("close.serial.port"));
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

		// 保存所有的输出到XML文件
		jBtnSaveOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("do nothing");
			}
		});

		// 保存关键字按钮
		jBtnSaveKeyword.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String xmlfile = jCtlPath.getText();
				String keyword = jCtlKeyword.getText();
				if (xmlfile.isEmpty() || !new File(xmlfile).exists()) {
					Objects.errorBox(MainFrame1.this, Strings.get("please.select.save.path"));
					return;
				}
				if (keyword.isEmpty() || !keyword.matches("^[0-9A-Za-z]{1,}+(\\|[0-9A-Za-z]{1,})*$")) {
					Objects.errorBox(MainFrame1.this, Strings.get("please.input.keywords"));
					return;
				}
				Properties props = new Properties();
				props.put("xmlfile", xmlfile);
				props.put("keyword", keyword);
				Objects.saveConfig(props);
				// 正则表达式缓存用于写XML文件
				regex = Pattern.compile("(?=" + keyword + ")");
			}
		});

		// 清除打印区的文本
		jBtnClearOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Document doc = jCtlOutput.getDocument();
				try {
					doc.remove(0, doc.getLength());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * 对配置进行持久化
	 */
	private void initConfig() {
		Properties props = Objects.readConfig();
		jCtlPath.setText(props.get("xmlfile").toString());
		jCtlKeyword.setText(props.get("keyword").toString());
		if (!jCtlKeyword.getText().isEmpty()) {
			regex = Pattern.compile("(?=" + jCtlKeyword.getText() + ")");
		}
	}

	private SerialPortObject makePortParams() {
		SerialPortObject object = new SerialPortObject();
		// 串口名
		String portName = (String) jCtlSerialPort.getSelectedItem();
		object.setPortName(portName);
		// 波特率
		String baudRate = (String) jCtlRate.getSelectedItem();
		object.setBaudRate(baudRate);
		// 数据位
		String dataBits = (String) jCtlDigitBit.getSelectedItem();
		object.setDataBits(dataBits);
		// 停止位
		String stopBits = (String) jCtlStopBit.getSelectedItem();
		object.setStopBits(stopBits);
		// 校验位
		String parity = (String) jCtlCheckBit.getSelectedItem();
		object.setParity(parity);
		// 返回对象
		return object;
	}

	/**
	 * 点击关闭按钮时做些清理工作
	 */
	@Override
	protected void processWindowEvent(final WindowEvent pEvent) {
		if (pEvent.getID() == WindowEvent.WINDOW_CLOSING) {
			if (!isClosing) {
				isClosing = true;
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
	 * 初始化页面上的组件
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		jCtlSerialPort = new JComboBox(Objects.listPorts());
		label2 = new JLabel();
		jCtlRate = new JComboBox(Objects.listRates());
		label3 = new JLabel();
		jCtlCheckBit = new JComboBox(Objects.listCheckBits());
		label4 = new JLabel();
		jCtlDigitBit = new JComboBox(Objects.listDigitBit());
		label5 = new JLabel();
		jCtlStopBit = new JComboBox(Objects.listStopBit());
		label6 = new JLabel();
		jBtnOpenOrClose = new JButton();
		separator1 = new JSeparator();
		panel3 = new JPanel();
		jBtnClearOut = new JButton();
		jBtnStopOut = new JToggleButton();
		jCtlAutoClear = new JCheckBox();
		jCtlHexOut = new JCheckBox();
		panel4 = new JPanel();
		jBtnSaveOut = new JButton();
		jBtnChangeDir = new JButton();
		jCtlPath = new JTextField();
		panel5 = new JPanel();
		label7 = new JLabel();
		jCtlSaveInterval = new JTextField("100");
		jBtnSaveKeyword = new JButton();
		jCtlKeyword = new JTextField();
		label8 = new JLabel();
		jCtlCharset = new JComboBox(Objects.listCharset());
		scrollPane1 = new JScrollPane();
		jCtlOutput = new JTextPane();

		// ======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		// ======== panel1 ========
		{
			panel1.setLayout(new FormLayout("default", "4*($lgap, default)"));

			// ======== panel2 ========
			{
				panel2.setLayout(new FormLayout("$lcgap, default, $lcgap, default:grow, $lcgap",
						"6*(default, $lgap), 2*($lgap)"));

				// ---- label1 ----
				label1.setText(Strings.get("serial.port"));
				panel2.add(label1, CC.xy(2, 1));
				panel2.add(jCtlSerialPort, CC.xy(4, 1));

				// ---- label2 ----
				label2.setText(Strings.get("baud.rate"));
				panel2.add(label2, CC.xy(2, 3));
				panel2.add(jCtlRate, CC.xy(4, 3));

				// ---- label3 ----
				label3.setText(Strings.get("check.bit"));
				panel2.add(label3, CC.xy(2, 5));
				panel2.add(jCtlCheckBit, CC.xy(4, 5));

				// ---- label4 ----
				label4.setText(Strings.get("data.bit"));
				panel2.add(label4, CC.xy(2, 7));
				panel2.add(jCtlDigitBit, CC.xy(4, 7));

				// ---- label5 ----
				label5.setText(Strings.get("stop.bit"));
				panel2.add(label5, CC.xy(2, 9));
				panel2.add(jCtlStopBit, CC.xy(4, 9));

				// ---- label6 ----
				label6.setText("\u25cf");
				panel2.add(label6, CC.xy(2, 11));

				// ---- jBtnClose ----
				jBtnOpenOrClose.setText(Strings.get("open.serial.port"));
				panel2.add(jBtnOpenOrClose, CC.xy(4, 11));
				panel2.add(separator1, CC.xywh(2, 13, 3, 1));
			}
			panel1.add(panel2, CC.xy(1, 2));

			// ======== panel3 ========
			{
				panel3.setLayout(new FormLayout("2*($lcgap, default), $lcgap", "2*(default, $lgap), default"));

				// ---- jBtnClearOut ----
				jBtnClearOut.setText(Strings.get("clean.receiving.area"));
				panel3.add(jBtnClearOut, CC.xy(2, 1));

				// ---- jBtnStopOut ----
				jBtnStopOut.setText(Strings.get("stop.display"));
				panel3.add(jBtnStopOut, CC.xy(4, 1));

				// ---- jCtlAutoClear ----
				jCtlAutoClear.setText(Strings.get("auto.clean"));
				panel3.add(jCtlAutoClear, CC.xywh(2, 3, 3, 1));

				// ---- jCtlHexOut ----
				jCtlHexOut.setText(Strings.get("hex.display"));
				panel3.add(jCtlHexOut, CC.xywh(2, 5, 3, 1));
			}
			panel1.add(panel3, CC.xy(1, 4));

			// ======== panel4 ========
			{
				panel4.setLayout(new FormLayout("$lcgap, default, $lcgap, default:grow, $lcgap",
						"default, $lgap, default"));

				// ---- jBtnSaveOut ----
				jBtnSaveOut.setText(Strings.get("save.current.data"));
				panel4.add(jBtnSaveOut, CC.xy(2, 1));

				// ---- jBtnChangeDir ----
				jBtnChangeDir.setText(Strings.get("set.save.path"));
				panel4.add(jBtnChangeDir, CC.xy(4, 1));
				panel4.add(jCtlPath, CC.xywh(2, 3, 3, 1));
			}
			panel1.add(panel4, CC.xy(1, 6));

			// ======== panel5 ========
			{
				panel5.setLayout(new FormLayout("$lcgap, default, $lcgap, default:grow, $lcgap",
						"2*(default, $lgap), default"));

				// ---- label7 ----
				label7.setText(Strings.get("save.interval"));
				panel5.add(label7, CC.xy(2, 1));
				panel5.add(jCtlSaveInterval, CC.xy(4, 1));

				// ---- jBtnSaveKeyword ----
				jBtnSaveKeyword.setText(Strings.get("save.keywords"));
				panel5.add(jBtnSaveKeyword, CC.xy(2, 3));
				jCtlKeyword.setPreferredSize(new Dimension(1, 23));
				panel5.add(jCtlKeyword, CC.xy(4, 3));

				// ---- label8 ----
				label8.setText(Strings.get("receiving.charset"));
				panel5.add(label8, CC.xy(2, 5));
				panel5.add(jCtlCharset, CC.xy(4, 5));
			}
			panel1.add(panel5, CC.xy(1, 8));
		}
		contentPane.add(panel1, BorderLayout.WEST);

		// ======== scrollPane1 ========
		{
			scrollPane1.setViewportView(jCtlOutput);
		}
		contentPane.add(scrollPane1, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JPanel panel2;
	private JLabel label1;
	private JComboBox jCtlSerialPort;
	private JLabel label2;
	private JComboBox jCtlRate;
	private JLabel label3;
	private JComboBox jCtlCheckBit;
	private JLabel label4;
	private JComboBox jCtlDigitBit;
	private JLabel label5;
	private JComboBox jCtlStopBit;
	private JLabel label6;
	private JButton jBtnOpenOrClose;
	private JSeparator separator1;
	private JPanel panel3;
	private JButton jBtnClearOut;
	private JToggleButton jBtnStopOut;
	private JCheckBox jCtlAutoClear;
	private JCheckBox jCtlHexOut;
	private JPanel panel4;
	private JButton jBtnSaveOut;
	private JButton jBtnChangeDir;
	private JTextField jCtlPath;
	private JPanel panel5;
	private JLabel label7;
	private JTextField jCtlSaveInterval;
	private JButton jBtnSaveKeyword;
	private JTextField jCtlKeyword;
	private JLabel label8;
	private JComboBox jCtlCharset;
	private JScrollPane scrollPane1;

	private JTextPane jCtlOutput;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
