package edu.aut.comp726.webservice.data;

import java.math.BigInteger;

public class VerificationDetails {
	
	private final boolean exists;
	private final BigInteger tokenId;
	
	public VerificationDetails(boolean exists, BigInteger tokenId) {
		this.exists = exists;
		this.tokenId = tokenId;
	}

	public boolean isExists() {
		return exists;
	}

	public BigInteger getTokenId() {
		return tokenId;
	}
}
