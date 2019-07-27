package cn.simple.jcom.assist;

import javax.comm.SerialPort;

/**
 * 串口设置对象
 */
public class SerialPortObject {
	// 串口名称
	private String portName;
	// 波特率
	private int baudRate;
	// 数据位
	private int dataBits;
	// 停止位
	private int stopBits;
	// 校验位
	private int parity;

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public void setBaudRate(String rate) {
		baudRate = Integer.valueOf(rate);
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public void setDataBits(String dataBits) {
		this.dataBits = Integer.valueOf(dataBits);
	}
	
	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public void setStopBits(String stopBits) {
		if ("1.5".equals(stopBits)) {
			this.stopBits = 3;
		}
		this.stopBits = Integer.valueOf(stopBits);
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public void setParity(String parity) {
		if ("None".equals(parity)) {
			this.parity = 0;
		} else if ("Odd".equals(parity)) {
			this.parity = SerialPort.PARITY_ODD;
		} else if ("Even".equals(parity)) {
			this.parity = SerialPort.PARITY_EVEN;
		} else if ("Mark".equals(parity)) {
			this.parity = SerialPort.PARITY_MARK;
		} else if ("Space".equals(parity)) {
			this.parity = SerialPort.PARITY_SPACE;
		} else {
			this.parity = 0;
		}
	}
}
