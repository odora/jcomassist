package cn.simple.jcom.assist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * XML文件的写入类
 */
public class XmlWriter implements Runnable {
	private File file;
	private String text;
	private RandomAccessFile raf;

	public XmlWriter(File file, String text) {
		this.file = file;
		this.text = text;
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
	 * 构造XML节点并插入到文件
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
			writeXmlLine(text);

			// 关闭XML文件
			raf.writeBytes("</TCSData>");
			raf.setLength(seek + text.length() + 10);

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
