package cn.simple.jcom.assist;

import java.io.File;

public class AppPath {
	public static void main(String[] args) {
		File f = new File(".");
		try {
			System.out.println(f.getCanonicalPath());
			System.out.println(System.getProperty("user.dir"));
		} catch (Exception e) {
		}
	}
}
