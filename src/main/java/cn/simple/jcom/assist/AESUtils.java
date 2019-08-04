package cn.simple.jcom.assist;

import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AESUtils {
	private static final String KEY_ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
	private static final byte[] defaultKey = initSecretKey();

	/**
	 * 加密字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String encrypt(String str) {
		try {
			byte[] res = encrypt(str.getBytes("UTF-8"), defaultKey);
			return Base64.encodeBase64String(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密字符串
	 * 
	 * @param str
	 * @return
	 */
	public static String decrypt(String str) {
		try {
			byte[] res = Base64.decodeBase64(str);
			res = decrypt(res, defaultKey);
			return new String(res, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] initSecretKey() {
		// 返回生成指定算法密钥生成器的 KeyGenerator 对象
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return new byte[0];
		}
		// 初始化此密钥生成器，使其具有确定的密钥大小
		// AES 要求密钥长度为 128
		kg.init(128);
		// 生成一个密钥
		SecretKey secretKey = kg.generateKey();
		return secretKey.getEncoded();
	}

	private static Key toKey(byte[] key) {
		return new SecretKeySpec(key, KEY_ALGORITHM);
	}

	public static byte[] encrypt(byte[] data, Key key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	private static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		return encrypt(data, k, cipherAlgorithm);
	}

	private static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		// 实例化
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		// 使用密钥初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, key);
		// 执行操作
		return cipher.doFinal(data);
	}

	private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}

	private static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
		// 还原密钥
		Key k = toKey(key);
		return decrypt(data, k, cipherAlgorithm);
	}

	private static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
		// 实例化
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		// 使用密钥初始化，设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, key);
		// 执行操作
		return cipher.doFinal(data);
	}
}