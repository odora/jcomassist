package cn.simple.jcom.assist;

import java.awt.Color;
import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.comm.CommPort;
import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * 通用工具类
 * 
 * @author xu.jun.fan@gmail.com
 * 
 */
@SuppressWarnings({ "rawtypes" })
public class Objects {
	// 线程池用来进行文件写入操作的互斥
	public static final ExecutorService pool = Executors.newSingleThreadExecutor();
	// 这里是线程不安全的对象,但是本例中不需要线程安全。不会产生并发执行的可能
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

	/**
	 * 获取本机所有串口
	 * 
	 * @return
	 */
	public static Object[] listPorts() {
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId;
		java.util.List<String> ports = new ArrayList<String>();
		while (enumeration.hasMoreElements()) {
			portId = (CommPortIdentifier) enumeration.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("port name :" + portId.getName());
				ports.add(portId.getName());
			}
		}
		return ports.toArray();
	}

	/**
	 * 根据名称获取串口标示
	 * 
	 * @param name
	 * @return
	 */
	public static CommPortIdentifier findPort(String name) {
		try {
			return CommPortIdentifier.getPortIdentifier(name);
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 打开串口
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static final SerialPort openPort(SerialPortObject obj) {
		CommPort commPort = null;
		try {
			// 通过端口名识别端口
			CommPortIdentifier portIdentifier = findPort(obj.getPortName());
			// 打开端口, 设置端口名与timeout(打开操作的超时时间)
			commPort = portIdentifier.open(obj.getPortName(), 2000);
			// 判断是不是串口
			if (commPort instanceof SerialPort) {
				SerialPort serialPort = (SerialPort) commPort;

				try {
					// 设置串口的波特率等参数
					serialPort.setSerialPortParams(obj.getBaudRate(), obj.getDataBits(), obj.getStopBits(),
							obj.getParity());
				} catch (UnsupportedCommOperationException e) {
					e.printStackTrace();
					serialPort.close();
					serialPort = null;
				}
				return serialPort;
			} else {
				System.out.println("不是串口");
				commPort.close();
			}
		} catch (PortInUseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 关闭串口
	 * 
	 * @param serialPort
	 */
	public static void closePort(SerialPort serialPort) {
		if (serialPort != null) {
			serialPort.close();
		}
	}

	/**
	 * 发送数据给串口
	 * 
	 * @param serialPort
	 * @param order
	 */
	public static boolean sendData(SerialPort serialPort, byte[] order) {
		OutputStream out = null;
		try {
			out = serialPort.getOutputStream();
			out.write(order);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
			}
		}
		return true;
	}

	/**
	 * 读取串口数据
	 * 
	 * @param serialPort
	 * @return
	 */
	public static Packet readData(SerialPort serialPort) {
		InputStream in = null;
		byte[] bytes = new byte[0];
		// 收集所有的字节数组
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			in = serialPort.getInputStream();
			// 获取buffer里的数据长度
			int length = in.available();
			while (length != 0) {
				// 初始化byte数组为buffer中数据的长度
				bytes = new byte[length];
				in.read(bytes);
				baos.write(bytes, 0, length);
				length = in.available();
			}
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
			}
		}
		// 返回读取到的数据
		return new Packet(baos.toByteArray());
	}

	/**
	 * 插入文本到右侧的显示窗口
	 * 
	 * @param pane
	 * @param text
	 */
	public static void addText2Pane(final JTextPane pane, final String text, final Color color) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SimpleAttributeSet set = null;
				Document doc = pane.getDocument();
				try {
					if (color != null) {
						// 如果传递有彩色文字就设置文本属性
						set = new SimpleAttributeSet();
						StyleConstants.setForeground(set, color);
					}
					doc.insertString(doc.getLength(), text, set);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Packet readData(InputStream in, JTextPane pane, boolean showHex, Charset charset) {
		byte[] bytes = new byte[0];
		// 收集所有的字节数组
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			// 获取buffer里的数据长度
			int length = in.available();
			while (length != 0) {
				// 初始化byte数组为buffer中数据的长度
				bytes = new byte[length];
				in.read(bytes);
				// 这里直接插入到界面中
				if (pane != null) {
					if (showHex) {
						addText2Pane(pane, Objects.byte2hex(bytes), null);
					} else {
						addText2Pane(pane, Objects.byte2utf(bytes, charset), null);
					}
				}
				baos.write(bytes, 0, length);
				length = in.available();
			}
		} catch (IOException e) {
			return null;
		}

		// 在显示区打印时间戳便于调试
		Date now = new Date();
		if (pane != null) {
			addText2Pane(pane, sdf.format(now), Color.red);
		}

		// 返回读取到的数据
		return new Packet(baos.toByteArray(), now.getTime());
	}

	/**
	 * 监听串口
	 * 
	 * @param port
	 * @param listener
	 */
	public static void addListener(SerialPort port, SerialPortEventListener listener) {
		try {
			// 给串口添加监听器
			port.addEventListener(listener);
			// 设置当有数据到达时唤醒监听接收线程
			port.notifyOnDataAvailable(true);
			// 设置当通信中断时唤醒中断线程
			port.notifyOnBreakInterrupt(true);
		} catch (TooManyListenersException e) {
			e.printStackTrace();
			throw new SerialPortException(e);
		}
	}

	/**
	 * 所有可选的波特率
	 * 
	 * @return
	 */
	public static Object[] listRates() {
		return new Object[] { "50", "75", "100", "150", "300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "115200" };
	}

	/**
	 * 选出校验位的文字描述
	 * 
	 * @return
	 */
	public static Object[] listCheckBits() {
		return new Object[] { "None", "Odd", "Even", "Mark", "Space" };
	}

	/**
	 * 所有可选的数据位
	 * 
	 * @return
	 */
	public static Object[] listDigitBit() {
		return new Object[] { "5", "6", "7", "8" };
	}

	/**
	 * 所有可选的停止位
	 * 
	 * @return
	 */
	public static Object[] listStopBit() {
		return new Object[] { "1", "1.5", "2" };
	}

	/**
	 * 弹出错误框
	 * 
	 * @param root
	 * @param text
	 * @param title
	 */
	public static void errorBox(Component root, String text, Object... params) {
		String msg = params == null || params.length == 0 ? text : String.format(text, params);
		JOptionPane.showMessageDialog(root, msg, "错误信息", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * 弹出信息框
	 * 
	 * @param root
	 * @param text
	 * @param title
	 */
	public static void infoBox(Component root, String text, Object... params) {
		String msg = params == null || params.length == 0 ? text : String.format(text, params);
		JOptionPane.showMessageDialog(root, msg, "提示信息", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 选择文件夹
	 */
	public static String choosePath() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.showDialog(new JLabel(), "选择");
		File file = jfc.getSelectedFile();
		if (file != null) {
			System.out.println(file.getAbsolutePath());
			return file.getAbsolutePath();
		}
		return null;
	}

	/**
	 * 保存文本到XML文件
	 * 
	 * @param path
	 * @param text
	 */
	public static void saveLine2Xml(String path, String text, Pattern keys) {
		File file = new File(path, "LiveData.xml");
		pool.submit(new XmlWriter(file, text, keys));
	}

	/**
	 * 读取配置文件
	 * 
	 * @return
	 */
	public static Properties readConfig() {
		Properties props = new Properties();
		String path = System.getProperty("user.home");
		File file = new File(path, "_jcomassist");
		FileInputStream fis = null;

		// 如果文件存在则读取否则写入默认值
		if (file.exists()) {
			try {
				fis = new FileInputStream(file);
				props.load(fis);
				return props;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e1) {
					}
				}
			}
		}

		// 如果不存在配置文件或者数据不完备则写入默认值
		if (!file.exists() || !props.contains("xmlfile") || !props.contains("keyword")) {
			String keys = "COV|OVB|OVR|RRQ|RRR|BTN|BTS|FTN|FTS|B1N|B1S|B1B|B2N|B2S|B2B|F1N|F1S|LWK|DLT|DLP";
			if (!props.contains("xmlfile")) {
				props.put("xmlfile", path);
			}
			if (!props.contains("keyword")) {
				// 共20个指标用逗号分隔存储到配置文件
				props.put("keyword", keys);
			}
			saveConfig(props);
		}

		return props;
	}

	/**
	 * 保存配置文件
	 * 
	 * @param props
	 */
	public static void saveConfig(Properties props) {
		String path = System.getProperty("user.home");
		File file = new File(path, "_jcomassist");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			props.store(fos, null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	/**
	 * 显示16进制
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byte2hex(byte[] bytes) {
		String strHex = "";
		if (bytes == null || bytes.length == 0) {
			return strHex;
		}
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int n = 0; n < bytes.length; n++) {
			strHex = Integer.toHexString(bytes[n] & 0xFF);
			sb.append(' ').append((strHex.length() == 1) ? "0" + strHex : strHex);
		}
		return sb.substring(1);
	}

	/**
	 * 转换成cp437字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byte2utf(byte[] bytes, Charset charset) {
		return new String(bytes, charset);
	}

	/**
	 * 所有可选的数据位
	 * 
	 * @return
	 */
	public static Object[] listCharset() {
		return new Object[] { "Cp437", "ISO8859_1", "GBK", "UTF8" };
	}
}
