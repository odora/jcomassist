package cn.simple.jcom.assist;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	public static void addText2Pane(final JTextPane pane, final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Document doc = pane.getDocument();
				try {
					doc.insertString(doc.getLength(), text, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Packet readData(InputStream in, JTextPane pane) {
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
				addText2Pane(pane, new String(bytes));
				baos.write(bytes, 0, length);
				length = in.available();
			}
		} catch (IOException e) {
			return null;
		}
		// 返回读取到的数据
		return new Packet(baos.toByteArray());
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
		return new Object[] { "50", "75", "100", "150", "300", "600", "1200", "2400", "4800", "9600", "19200", "38400" };
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
	public static void saveLine2Xml(String path, String text) {
		File file = new File(path, "LiveData.xml");
		pool.submit(new XmlWriter(file, text));
	}
}