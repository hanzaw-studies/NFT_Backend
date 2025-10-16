package edu.aut.comp726.webservice.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PinataResponseInfo {
	@JsonProperty("IpfsHash")
	private String ipfsHash;
	@JsonProperty("PinSize")
	private long pinSize;
	@JsonProperty("Timestamp")
	private String timestamp;
	@JsonProperty("ID")
	private String id;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("NumberOfFiles")
	private int numberOfFiles;
	@JsonProperty("MimeType")
	private String mimeType;
	@JsonProperty("GroupId")
	private String groupId;
	@JsonProperty("Keyvalues")
	private String keyvalues;
	@JsonProperty("isDuplicate")
	private boolean isDuplicate;

	public PinataResponseInfo() {}

	public String getIpfsHash() {
		return ipfsHash;
	}

	public void setIpfsHash(String ipfsHash) {
		this.ipfsHash = ipfsHash;
	}

	public long getPinSize() {
		return pinSize;
	}

	public void setPinSize(long pinSize) {
		this.pinSize = pinSize;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumberOfFiles() {
		return numberOfFiles;
	}

	public void setNumberOfFiles(int numberOfFiles) {
		this.numberOfFiles = numberOfFiles;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getKeyvalues() {
		return keyvalues;
	}

	public void setKeyvalues(String keyvalues) {
		this.keyvalues = keyvalues;
	}

	public boolean isDuplicate() {
		return isDuplicate;
	}

	public void setDuplicate(boolean isDuplicate) {
		this.isDuplicate = isDuplicate;
	}

	@Override
	public String toString() {
		return "PinataResponseInfo {" + 
				"ipfsHash='" + ipfsHash + '\'' + 
				", pinSize=" + pinSize + 
				", timestamp='" + timestamp + '\'' + 
				", id='" + id + '\'' + 
				", name='" + name + '\'' + 
				", numberOfFiles=" + numberOfFiles + 
				", mimeType='" + mimeType + '\'' + 
				", groupId='" + groupId + '\'' + 
				", keyvalues='" + keyvalues + '\'' + 
				", isDuplicate=" + isDuplicate + 
				'}';
	}
}