package edu.aut.comp726.webservice.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NFTMetadata {
    private Long tokenId;
    private String ipfsCID;
    private String imageHash;
    private String description;
    private String minter;
    private Long timestamp;
	public Long getTokenId() {
		return tokenId;
	}
	public void setTokenId(Long tokenId) {
		this.tokenId = tokenId;
	}
	public String getIpfsCID() {
		return ipfsCID;
	}
	public void setIpfsCID(String ipfsCID) {
		this.ipfsCID = ipfsCID;
	}
	public String getImageHash() {
		return imageHash;
	}
	public void setImageHash(String imageHash) {
		this.imageHash = imageHash;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMinter() {
		return minter;
	}
	public void setMinter(String minter) {
		this.minter = minter;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}
