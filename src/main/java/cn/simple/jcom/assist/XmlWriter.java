package cn.simple.jcom.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * XML文件的写入类
 */
public class XmlWriter implements Runnable {
	private static final Charset utf8 = Charset.forName("utf-8");
	private File file;
	private String text;
	private Pattern keys;
	private Map<String, Integer> words;
	private RandomAccessFile raf;

	public XmlWriter(File file, String text, Pattern keys) {
		this.file = file;
		this.text = text;
		this.keys = keys;
		this.words = words(keys);
	}

	/**
	 * 将真个表达式还原成单词
	 * 
	 * @param keys
	 * @return
	 */
	private Map<String, Integer> words(Pattern keys) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if (keys != null) {
			String pattern = keys.pattern();
			int len = pattern.length();
			pattern = pattern.substring(3, len - 1);
			String[] strs = pattern.split("\\|");
			for (int i = 0; i < strs.length; i++) {
				map.put(strs[i], i + 1);
			}
		}
		return map;
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
					Integer index = words.get(key);
					if (index != null) {
						s.append("<Field no=\"").append(index).append("\">")
						.append(val)
						.append("</Field>");
					}
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
			raf.write(text.getBytes(utf8));
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

			// 往回数1个字符用来寻找插入点
			long pos = len;
			raf.seek(--pos);
			while (raf.readByte() != '>' && pos > 0) {
				raf.seek(--pos);
			}
			raf.seek(pos - 9);

			// 在插入点写入新数据
			String str = text2xml();
			writeXmlLine(str);

			// 关闭XML文件
			raf.writeBytes("</TCSData>");
			// raf.setLength(pos + str.length());

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
