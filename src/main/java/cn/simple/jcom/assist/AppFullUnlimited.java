package cn.simple.jcom.assist;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.google.gson.Gson;

/**
 * 串口接收工具
 */
public class AppFullUnlimited {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Strings.load();
		MainFrame1 mainFrame = new MainFrame1();
		mainFrame.setTitle(Strings.get("serial.port.tool"));
		mainFrame.setSize(800, 600);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
	}

	/**
	 * 校验证书
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String checkLicense() {
		String error = "证书格式错误";
		String home = System.getProperty("user.home");
		String work = System.getProperty("user.dir");
		File file = new File(home, "jcomassist-license.dat");
		if (!file.exists()) {
			file = new File(work, "jcomassist-license.dat");
		}
		String text = Objects.readFile(file);
		if (text == null) {
			return "证书文件不存在";
		}
		System.out.println(text);
		License obj = new Gson().fromJson(text, License.class);
		if (obj == null) {
			return error;
		}
		String code = obj.getEncryptCode();
		System.out.println(code);
		if (code == null || code.isEmpty()) {
			return error;
		}

		// 解码证书加密块
		try {
			code = AESUtils.decryptBase64(code);
		} catch (Exception e) {
			return error;
		}

		// 加密块还原
		License inner = new Gson().fromJson(code, License.class);
		if (inner == null) {
			return error;
		}

		// 证书的有效期间
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if (inner.getIssuedTime() != null) {
			try {
				Date date1 = sdf.parse(inner.getIssuedTime());
				if (System.currentTimeMillis() < date1.getTime()) {
					return "不在证书的有效期间内";
				}
			} catch (Exception e) {
				return error;
			}
		}

		// 证书的过期时间
		if (inner.getExpiryTime() != null) {
			try {
				Date date2 = sdf.parse(inner.getExpiryTime());
				if (System.currentTimeMillis() > date2.getTime()) {
					return "证书已经过期";
				}
			} catch (Exception e) {
				return error;
			}
		}

		// 硬件校验(现场部署的场合)
		String hwerr = "硬件信息与证书不匹配";
		if (inner.getMacAddress() != null) {
			try {
				List<String> addrs = HWUtils.getMacAddress();
				if (!addrs.isEmpty() && !inner.getMacAddress().equalsIgnoreCase(addrs.get(0))) {
					return hwerr;
				}
			} catch (Exception e) {
				return hwerr;
			}
		}
		if (inner.getCpuSerial() != null) {
			try {
				if (!inner.getCpuSerial().equalsIgnoreCase(HWUtils.getCPUSerial())) {
					return hwerr;
				}
			} catch (Exception e) {
				return hwerr;
			}
		}
		if (inner.getMainBoardSerial() != null) {
			try {
				if (!inner.getMainBoardSerial().equalsIgnoreCase(HWUtils.getMainBoardSerial())) {
					return hwerr;
				}
			} catch (Exception e) {
				return hwerr;
			}
		}

		return "ok";
	}
}
