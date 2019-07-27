package cn.simple.jcom.assist;

/**
 * 数据包对象
 */
public final class Packet {
	private final byte[] rawData;
	private final long timestamp;

	public Packet(byte[] rawData) {
		this.rawData = rawData;
		this.timestamp = System.currentTimeMillis();
	}

	public Packet(byte[] rawData, long time) {
		this.rawData = rawData;
		this.timestamp = time;
	}

	public byte[] getRawData() {
		return rawData;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
