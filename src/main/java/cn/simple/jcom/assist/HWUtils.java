package cn.simple.jcom.assist;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

/**
 * 硬件信息工具类
 */
public class HWUtils {
	/**
	 * 获取所有的MAC地址
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<String> getMacAddress() throws Exception {
		List<String> result = new ArrayList<String>();

		// 1. 获取所有网络接口
		List<InetAddress> addrs = getLocalAllInetAddress();

		if (addrs != null && addrs.size() > 0) {
			// 2. 获取所有网络接口的Mac地址
			for (InetAddress addr : addrs) {
				String mac = getMacByInetAddress(addr);
				if (!result.contains(mac)) {
					result.add(mac);
				}
			}
		}

		return result;
	}

	/**
	 * CPU序列号
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getCPUSerial() throws Exception {
		// 序列号
		String serialNumber = "";

		// 使用WMIC获取CPU序列号
		Process process = Runtime.getRuntime().exec("wmic cpu get processorid");
		process.getOutputStream().close();
		Scanner scanner = new Scanner(process.getInputStream());

		if (scanner.hasNext()) {
			scanner.next();
		}

		if (scanner.hasNext()) {
			serialNumber = scanner.next().trim();
		}

		scanner.close();
		return serialNumber;
	}

	/**
	 * 主板序列号
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getMainBoardSerial() throws Exception {
		// 序列号
		String serialNumber = "";

		// 使用WMIC获取主板序列号
		Process process = Runtime.getRuntime().exec("wmic baseboard get serialnumber");
		process.getOutputStream().close();
		Scanner scanner = new Scanner(process.getInputStream());

		if (scanner.hasNext()) {
			scanner.next();
		}

		if (scanner.hasNext()) {
			serialNumber = scanner.next().trim();
		}

		scanner.close();
		return serialNumber;
	}

	/**
	 * 获取所有网络接口
	 * 
	 * @return
	 * @throws Exception
	 */
	private static List<InetAddress> getLocalAllInetAddress() throws Exception {
		List<InetAddress> result = new ArrayList<InetAddress>(4);

		// 遍历所有的网络接口
		for (Enumeration<NetworkInterface> nif = NetworkInterface.getNetworkInterfaces(); nif.hasMoreElements();) {
			NetworkInterface iface = nif.nextElement();
			// 在所有的接口下再遍历IP
			for (Enumeration<InetAddress> addrs = iface.getInetAddresses(); addrs.hasMoreElements();) {
				InetAddress inetAddr = addrs.nextElement();
				// 排除LoopbackAddress、SiteLocalAddress、LinkLocalAddress、MulticastAddress类型的IP地址
				if (!inetAddr.isLoopbackAddress() && !inetAddr.isLinkLocalAddress() && !inetAddr.isMulticastAddress()) {
					result.add(inetAddr);
				}
			}
		}

		return result;
	}

	/**
	 * 获取网络接口的MAC地址
	 * 
	 * @param inetAddr
	 * @return
	 */
	private static String getMacByInetAddress(InetAddress inetAddr) {
		try {
			byte[] mac = NetworkInterface.getByInetAddress(inetAddr).getHardwareAddress();
			StringBuffer stringBuffer = new StringBuffer();

			for (int i = 0; i < mac.length; i++) {
				if (i != 0) {
					stringBuffer.append("-");
				}

				// 将十六进制byte转化为字符串
				String temp = Integer.toHexString(mac[i] & 0xff);
				if (temp.length() == 1) {
					stringBuffer.append("0" + temp);
				} else {
					stringBuffer.append(temp);
				}
			}

			return stringBuffer.toString().toUpperCase();
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return null;
	}
}
