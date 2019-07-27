package cn.simple.jcom.assist;

/**
 * 包装所有的异常给外层调用者
 */
@SuppressWarnings("serial")
public class SerialPortException extends RuntimeException {
	public SerialPortException(Throwable e) {
		super(e);
	}
}
