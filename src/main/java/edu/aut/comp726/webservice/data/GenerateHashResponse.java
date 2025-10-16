package edu.aut.comp726.webservice.data;

import lombok.Data;

@Data
public class GenerateHashResponse {
    private boolean success;
    private String hash;
    private long size;
    private String message;
    
    public GenerateHashResponse() {}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}