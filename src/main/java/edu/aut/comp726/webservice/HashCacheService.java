package edu.aut.comp726.webservice;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HashCacheService {
	
	private final NFTService nftService;
	private final ConcurrentHashMap<String, BigInteger> hashCache = new ConcurrentHashMap<>();
	
	public HashCacheService(NFTService nftService) {
		this.nftService = nftService;
		buildCache();
	}
	
	public BigInteger getTokenIdByHash(String imageHash) {
		return hashCache.get(imageHash.toLowerCase());
	}
	
	@Scheduled(fixedRate = 300000)	// every 5 minutes
	public void buildCache() {
		try {
			
			BigInteger total = nftService.totalSupply();
			hashCache.clear();
			
			for (BigInteger i = BigInteger.ONE; i.compareTo(total) <= 0; i = i.add(BigInteger.ONE)) {
				try {
					String hash = nftService.getImageHash(i);
					if (hash != null) {
						hashCache.put(hash.toLowerCase(), i);
					}
				} catch (Exception ignored) {}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
