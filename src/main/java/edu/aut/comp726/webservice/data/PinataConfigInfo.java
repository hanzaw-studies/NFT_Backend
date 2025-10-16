package edu.aut.comp726.webservice.data;

public class PinataConfigInfo {
	
	private String pinataApiUrl;
	private String pintaJwt;
	
	public PinataConfigInfo() { }

	public String getPinataApiUrl() {
		return pinataApiUrl;
	}

	public void setPinataApiUrl(String pinataApiUrl) {
		this.pinataApiUrl = pinataApiUrl;
	}

	public String getPintaJwt() {
		return pintaJwt;
	}

	public void setPintaJwt(String pintaJwt) {
		this.pintaJwt = pintaJwt;
	}
}
