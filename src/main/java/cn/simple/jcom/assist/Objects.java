package cn.simple.jcom.assist;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.comm.CommPortIdentifier;

@SuppressWarnings({"rawtypes"})
public class Objects {
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

	public static CommPortIdentifier findByPortName(String name) {
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId;
		while (enumeration.hasMoreElements()) {
			portId = (CommPortIdentifier) enumeration.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equalsIgnoreCase(name)) {
					return portId;
				}
			}
		}
		return null;
	}

	/**
	 * 所有可选的波特率
	 * 
	 * @return
	 */
	public static Object[] listRates() {
		return new Object[] {
			50,
			75,
			100,
			150,
			300,
			600,
			1200,
			2400,
			4800,
			9600,
			19200,
			38400
		};
	}

	/**
	 * 选出校验位的文字描述
	 * 
	 * @return
	 */
	public static Object[] listCheckBits() {
		return new Object[] {
				"None",
				"Odd",
				"Even",
				"Mark",
				"Space"
		};
	}

	/**
	 * 所有可选的数据位
	 * 
	 * @return
	 */
	public static Object[] listDigitBit() {
		return new Object[] { 5, 6, 7, 8 };
	}

	/**
	 * 所有可选的校验位
	 * 
	 * @return
	 */
	public static Object[] listStopBit() {
		return new Object[] { 1, 1.5, 2 };
	}
}
