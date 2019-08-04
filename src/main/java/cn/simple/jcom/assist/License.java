package cn.simple.jcom.assist;

/**
 * 证书对象
 */
public class License {
	private String subject;
	private String issuedTime;
	private String expiryTime;
	private String consumerType;
	private String macAddress;
	private String cpuSerial;
	private String mainBoardSerial;
	private String encryptCode;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getIssuedTime() {
		return issuedTime;
	}

	public void setIssuedTime(String issuedTime) {
		this.issuedTime = issuedTime;
	}

	public String getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}

	public String getConsumerType() {
		return consumerType;
	}

	public void setConsumerType(String consumerType) {
		this.consumerType = consumerType;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getCpuSerial() {
		return cpuSerial;
	}

	public void setCpuSerial(String cpuSerial) {
		this.cpuSerial = cpuSerial;
	}

	public String getMainBoardSerial() {
		return mainBoardSerial;
	}

	public void setMainBoardSerial(String mainBoardSerial) {
		this.mainBoardSerial = mainBoardSerial;
	}

	public String getEncryptCode() {
		return encryptCode;
	}

	public void setEncryptCode(String encryptCode) {
		this.encryptCode = encryptCode;
	}
}
