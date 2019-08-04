package cn.simple.jcom.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * XML文件的写入类(只写入最新的记录)
 */
public class XmlWriter2 implements Runnable {
	private static final Charset utf8 = Charset.forName("utf-8");
	private File file;
	private String text;
	private Pattern keys;
	private LinkedHashMap<String, Integer> words;
	private Map<String, String> fileBuf;

	public XmlWriter2(File file, String text, Pattern keys, Map<String, String> fileBuf) {
		this.file = file;
		this.text = text;
		this.keys = keys;
		this.words = words(keys);
		this.fileBuf = fileBuf;
	}

	/**
	 * 将真个表达式还原成单词
	 * 
	 * @param keys
	 * @return
	 */
	private LinkedHashMap<String, Integer> words(Pattern keys) {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
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
	 * 合并到缓冲区
	 */
	private void mergeWithFileContent() {
		for (String item : keys.split(text)) {
			if (!item.isEmpty() && item.length() > 3) {
				fileBuf.put(item.substring(0, 3), item.substring(3));
			}
		}
	}

	/**
	 * 将文本转换成XML的节点形式
	 * 
	 * @return
	 */
	private String text2xml() {
		if (keys != null && fileBuf != null && text != null) {
			mergeWithFileContent();
			StringBuilder s = new StringBuilder("<TCSData><LiveData>");
			for (Map.Entry<String, Integer> entry : words.entrySet()) {
				String value = fileBuf.get(entry.getKey());
				if (value != null) {
					s.append("<Field no=\"").append(entry.getValue()).append("\">");
					s.append(value).append("</Field>");
				}
			}
			s.append("</LiveData></TCSData>");
			String result = s.toString();
			System.out.println(result);
			return result;
		} else {
			System.out.println("empty data!");
		}
		return null;
	}

	@Override
	public void run() {
		// 若文件不存在则创建之
		if (!file.exists()) {
			// 确保目录都是存在的
			file.getParentFile().mkdirs();
		}

		// 写入文件
		FileOutputStream writer = null;
		try {
			String str = text2xml();
			if (str != null && !str.isEmpty()) {
				writer = new FileOutputStream(file);
				writer.write(str.getBytes(utf8));
				writer.flush();
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
