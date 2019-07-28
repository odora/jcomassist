package cn.simple.jcom.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

/**
 * XML文件的写入类
 */
public class XmlWriter implements Runnable {
	private File file;
	private String text;
	private Pattern keys;
	private RandomAccessFile raf;

	public XmlWriter(File file, String text, Pattern keys) {
		this.file = file;
		this.text = text;
		this.keys = keys;
	}

	/**
	 * 创建空的文件
	 */
	private void createEmptyFile(File file) {
		StringBuilder s = new StringBuilder();
		s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		s.append('\n').append("<TCSData></TCSData>");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(s.toString().getBytes());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 将文本转换成XML的节点形式
	 * 
	 * @return
	 */
	private String text2xml() {
		if (keys != null) {
			StringBuilder s = new StringBuilder("<LiveData>");
			for (String item : keys.split(text)) {
				if (!item.isEmpty() && item.length() > 3) {
					String key = item.substring(0, 3);
					String val = item.substring(3);
					s.append("<").append(key).append(">")
					.append(val)
					.append("</").append(key).append(">");
				}
			}
			s.append("</LiveData>");
			String result = s.toString();
			System.out.println(result);
			return result;
		}
		// 如果没有KEYS则原样输出字符串
		return "<LiveData>" + text + "</LiveData>";
	}

	/**
	 * 写入到XML文件
	 * 
	 * @param text
	 */
	private void writeXmlLine(String text) {
		try {
			raf.writeBytes(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// 若文件不存在则创建之
		if (!file.exists()) {
			// 确保目录都是存在的
			file.getParentFile().mkdirs();
			// 创建文件
			createEmptyFile(file);
		}

		// 找到文件的插入点插入所需的数据
		try {
			raf = new RandomAccessFile(file, "rw");
			long len = raf.length();
			if (len < 50) {
				System.err.println("file invalid");
				return;
			}

			// 往回数50个字符用来寻找插入点
			raf.seek(len - 50);
			byte[] bytes = new byte[50];
			raf.readFully(bytes);
			long seek = new String(bytes).indexOf("</TCSData>");
			if (seek >= 0) {
				seek = len - 50 + seek;
			}
			raf.seek(seek);

			// 在插入点写入新数据
			String str = text2xml();
			writeXmlLine(str);

			// 关闭XML文件
			raf.writeBytes("</TCSData>");
			raf.setLength(seek + str.length() + 10);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException e) {
			}
		}
	}

}
