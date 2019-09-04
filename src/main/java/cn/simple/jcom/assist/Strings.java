package cn.simple.jcom.assist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;

public class Strings {
	// 配置文件的配置项
	private static final Properties holder = new Properties();
	// 默认的配置项（中文）
	private static final Properties defaults = new Properties();

	static {
		// 所有的界面文字常量
		defaults.put("please.input.save.time", "请输入【保存时间】");
		defaults.put("save.time.must.be.digit", "【保存时间】必须为数字");
		defaults.put("please.select.save.path", "请选择【保存路径】");
		defaults.put("open.serial.port", "打开串口");
		defaults.put("failed.to.open.port", "打开串口失败");
		defaults.put("close.serial.port", "关闭串口");
		defaults.put("please.input.keywords", "请填写【关键字】");
		defaults.put("serial.port", "串口");
		defaults.put("baud.rate", "波特率");
		defaults.put("check.bit", "校验位");
		defaults.put("stop.bit", "停止位");
		defaults.put("clean.receiving.area", "清空接收区");
		defaults.put("stop.display", "停止显示");
		defaults.put("auto.clean", "自动清空");
		defaults.put("hex.display", "16进制显示");
		defaults.put("save.current.data", "保存显示数据");
		defaults.put("set.save.path", "指定保存路径");
		defaults.put("save.interval", "保存时间");
		defaults.put("save.keywords", "保存关键字");
		defaults.put("receiving.charset", "接收字符集");
		defaults.put("please.input.hello", "请填写【问候语】");
		defaults.put("keywords", "关键字");
		defaults.put("hello.words", "问候语");
		defaults.put("data.path", "数据路径");
		defaults.put("save.config", "保存配置");
		defaults.put("selecting", "选择");
		defaults.put("prompt.info", "提示信息");
		defaults.put("error.info", "错误信息");
	}

	public static void load() {
		String path = System.getProperty("user.dir");
		File file = new File(path, "_jcomassist.strings");
		BufferedReader fis = null;

		// 如果文件存在则读取否则写入默认值
		if (file.exists()) {
			try {
				fis = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				holder.load(fis);
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
	}

	public static void save() {
		String path = System.getProperty("user.home");
		File file = new File(path, "_jcomassist.strings");
		PrintWriter fos = null;
		try {
			fos = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
			defaults.store(fos, null);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * 获取国际化文字
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		String val = holder.getProperty(key);
		if (val == null || val.isEmpty()) {
			val = defaults.getProperty(key);
		}
		if (val == null || val.isEmpty()) {
			val = key;
		}
		return val;
	}
}
