package edu.aut.comp726.webservice;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.aut.comp726.webservice.data.GenerateHashResponse;
import edu.aut.comp726.webservice.data.NFTMetadata;
import edu.aut.comp726.webservice.data.PinataConfigInfo;
import edu.aut.comp726.webservice.data.PinataResponseInfo;
import edu.aut.comp726.webservice.data.VerificationDetails;
import edu.aut.comp726.webservice.data.VerifyImageResponse;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api")
public class PhotoUploadController {
	
	private final HttpClient httpClient = HttpClients.createDefault();
	private static final Logger log4j2Logger = LogManager.getLogger(PhotoUploadController.class);
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PhotoUploadController.class);
	
	private static final String IPFS_API_URL = "http://localhost:5001/api/v0/add";
	
	@Qualifier("pinataConfigInfo")
	@Autowired
	private PinataConfigInfo pinataConfigInfo;
	
	@Autowired
	private NFTService nftService;
	
	@Autowired
	private HashCacheService hashCacheService;
	
	public PhotoUploadController() {
		logger.info("PhotoUploadController initialized (SLF4J)");
		log4j2Logger.info("PhotoUploadController initialized (Log4j2)");
	}
	
	@GetMapping("/greeting")
	public ResponseEntity<String> helloWorld() {
		logger.info("Inside helloWorld() method ...");
		return new ResponseEntity<String>("Hello World", HttpStatus.OK);
	}
	
//	@CrossOrigin(origins = "http://localhost:5173")
//	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
/*	public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
		Map<String, String> responseMap = new HashMap<>();
		if (file.isEmpty()) {
			responseMap.put("error", "No file uploaded.");
			return ResponseEntity.badRequest().body(responseMap);
		}
		try {
			logger.info("File content Type: " + file.getContentType());
			logger.info("File Name: " + file.getName());
			logger.info("Original File Name: " + file.getOriginalFilename());
			
			// Prepare the multipart entity for IPFS upload
			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addBinaryBody("file", file.getInputStream(), 
					ContentType.create(file.getContentType()), file.getOriginalFilename());
			
			HttpPost httpPost = new HttpPost(IPFS_API_URL);
			httpPost.setEntity(entityBuilder.build());
			
			// Execute the request to IPFS
			ClassicHttpResponse response = (ClassicHttpResponse) httpClient.execute(httpPost);
			int statusCode = response.getCode();
			
			if (statusCode != 200) {
				responseMap.put("error", "Failed to upload to IPFS. Status code: " + statusCode);
				return ResponseEntity.status(500).body(responseMap);
			}
			
			String responseBody = EntityUtils.toString(response.getEntity());
			
			logger.info("Response from IPFS: {}", responseBody);
			
			// The response contains the hash in JSON format, e.g., {"Name":"filename","Hash":"Qm...","Size":"12345"}
			String hash = extractHashFromResponse(responseBody);
			
			if (hash == null) {
				responseMap.put("error", "Failed to parse IPFS response");
				return ResponseEntity.status(500).body(responseMap);
			}
			String ipfsUrl = "https://ipfs.io/ipfs/" + hash;
			responseMap.put("url", ipfsUrl);
			
			logger.info("ipfsUrl: " + ipfsUrl);
			
			
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
			//return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(json);
			
		} catch (IOException | ParseException e) {
			logger.error("Server error during photo upload", e);
			responseMap.put("error", "Server error: " + e.getMessage());
			return ResponseEntity.status(500).body(responseMap);
		}
	}
	*/
	
	@CrossOrigin(origins = "http://localhost:5173")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadPhotoToPinata(@RequestParam("file") MultipartFile file) {
		
		Map<String, String> responseMap = new HashMap<>();
		
		if (file.isEmpty()) {
			responseMap.put("error", "No file uploaded.");
			return ResponseEntity.badRequest().body(responseMap);
		}
		
		try {
			
			logger.info("File content Type: " + file.getContentType());
			logger.info("File Name: " + file.getName());
			logger.info("Original File Name: " + file.getOriginalFilename());
			
			if (pinataConfigInfo != null) {
				
				logger.info("pinataConfigInfo is not null");
				
				logger.info("pinataConfigInfo.getPinataApiUrl(): " + pinataConfigInfo.getPinataApiUrl());
				logger.info("pinataConfigInfo.getPintaJwt(): " + pinataConfigInfo.getPintaJwt());
				
				String PINATA_API_URL = pinataConfigInfo.getPinataApiUrl();
				String PINATA_JWT = "Bearer " + pinataConfigInfo.getPintaJwt();
				
				// Prepare the multipart entity for IPFS upload
				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.addBinaryBody("file", file.getInputStream(), 
						ContentType.create(file.getContentType()), file.getOriginalFilename());
				
				
				HttpEntity entity = entityBuilder.build();	
				
				HttpPost httpPost = new HttpPost(PINATA_API_URL);
				httpPost.setHeader("Authorization", PINATA_JWT);
				
				httpPost.setEntity(entity);
				
				// Execute the request to Pinata IPFS
				ClassicHttpResponse response = (ClassicHttpResponse) httpClient.execute(httpPost);
				int statusCode = response.getCode();
				
				if (statusCode != 200) {
					responseMap.put("error", "Failed to upload to IPFS. Status code: " + statusCode);
					return ResponseEntity.status(500).body(responseMap);
				}
				
				String responseBody = EntityUtils.toString(response.getEntity());
				
				logger.info("Response from IPFS: {}", responseBody);
				
				ObjectMapper mapper = new ObjectMapper();
				
				PinataResponseInfo objResponseInfo = mapper.readValue(responseBody, PinataResponseInfo.class);
				
				String ipfsHash = objResponseInfo.getIpfsHash();
				
				if (ipfsHash == null) {
					responseMap.put("error", "Failed to parse IPFS response");
					return ResponseEntity.status(500).body(responseMap);
				}
				
				String ipfsUrl = "https://ipfs.io/ipfs/" + ipfsHash;
				
				responseMap.put("url", ipfsUrl);
				
				logger.info("ipfsUrl: " + ipfsUrl);
				
				response.close();
				
				return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseMap);
				
			} else {
				logger.info("pinataConfigInfo is null");
			}
			
			logger.error("Server error during photo upload");
			responseMap.put("error", "Server error...");
			return ResponseEntity.status(500).body(responseMap);

		} catch (Exception ex) {
			logger.error("Server error during photo upload", ex);
			responseMap.put("error", "Server error: " + ex.getMessage());
			return ResponseEntity.status(500).body(responseMap);
		}
	}
	
	@CrossOrigin(origins = "http://localhost:5173")
	@PostMapping(value = "/upload-metadata", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadMetadata(@RequestBody Map<String, Object> body) {
		
		Map<String, String> responseMap = new HashMap<>();
		
		try {
			
			Object metadataObject = body.get("metadata");
			
			if (metadataObject == null) {
				responseMap.put("error", "Missing 'metadata' object in response body.");
				return ResponseEntity.badRequest().body(responseMap);
			}
			
			// Convert the metadata object back to a JSON string for pinning
			ObjectMapper mapper = new ObjectMapper();
			String metadataJson = mapper.writeValueAsString(metadataObject);
			
			// -- Logic to pin metadataJson to Pinata --
			String PINATA_API_URL_JSON = pinataConfigInfo.getPinataApiUrl().replace("pinFileToIPFS", "pinJsonToIPFS"); // Assume Pinata URL structure
			String PINATA_JWT = "Bearer " + pinataConfigInfo.getPintaJwt();
			
			// Create the JSON payload for pinJsonToIPFS
			Map<String, Object> pinataJsonBody = Map.of(
					"pinataContent", metadataObject,	// Pass the JSON structure directly
					"pinataOptions", Map.of("cidVersion", 1));
			
			StringEntity entity = new StringEntity(mapper.writeValueAsString(pinataJsonBody), ContentType.APPLICATION_JSON);
			
			// -- Send HTTP POST to Pinata using Apache HttpClient --
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				HttpPost request = new HttpPost(PINATA_API_URL_JSON);
				request.setHeader(HttpHeaders.AUTHORIZATION, PINATA_JWT);
				request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
				request.setEntity(entity);
				
				try (CloseableHttpResponse response = httpClient.execute(request)) {
					int statusCode = response.getCode();
					String responseBody = EntityUtils.toString(response.getEntity());
					
					if (statusCode != org.apache.hc.core5.http.HttpStatus.SC_OK 
							&& statusCode != org.apache.hc.core5.http.HttpStatus.SC_CREATED) {
						logger.error("Failed to pin metadata: {}", responseBody);
						responseMap.put("error", "Pinata API returned error: " + responseBody);
						return ResponseEntity.status(statusCode).body(responseMap);
					}
					
					ObjectMapper responseMapper = new ObjectMapper();
					PinataResponseInfo objResponseInfo = responseMapper.readValue(responseBody, PinataResponseInfo.class);
					
					String ipfsHash = objResponseInfo.getIpfsHash();
					if (ipfsHash == null) {
						responseMap.put("error", "Failed to parse Pinata JSON response.");
						return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
					}
					
					String ipfsUrl = "https://ipfs.io/ipfs/" + ipfsHash;
					
					responseMap.put("url", ipfsUrl);
					responseMap.put("cid", ipfsHash);
					
					logger.info("Metadata ipfsUrl: " + ipfsUrl);
					
					return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(responseMap);					
				}
			}

		} catch (Exception ex) {
			logger.error("Server error during metadata upload", ex);
			responseMap.put("error", "Server error: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMap);
		}
	}
	
	@CrossOrigin(origins = "http://localhost:5173")
	@PostMapping("/generateHash")
	public ResponseEntity<?> generateHash(@RequestParam("file") MultipartFile file) {
		
		GenerateHashResponse response = new GenerateHashResponse();
		
		try {
			
			if (file == null || file.isEmpty()) {
				response.setSuccess(false);
				response.setMessage("Uploaded file is empty or missing.");
				return ResponseEntity.badRequest().body(response);
			}
			
			byte[] bytes = file.getBytes();
			String hexInput = Numeric.toHexString(bytes);
			String hash = Hash.sha3(hexInput);
			
			response.setSuccess(true);
			response.setHash(hash);
			response.setSize(bytes.length);
			
			return ResponseEntity.ok(response);
			
			
		} catch (IOException ex) {
//			logger.error("Server error during hash calculation", ex);
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Error reading file: " + ex.getMessage()));
			
			response.setSuccess(false);
			response.setMessage("Error reading uploaded file: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		} catch (Exception ex) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Failed to generate hash: " + ex.getMessage()));
			response.setSuccess(false);
			response.setMessage("Failed to generate Hash: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@CrossOrigin(origins = "http://localhost:5173")
	@PostMapping("/verifyImage")
	public ResponseEntity<VerifyImageResponse> verifyImage(@RequestBody Map<String, String> body) {
		VerifyImageResponse response = new VerifyImageResponse();
		
		try {
			
			logger.info("[PhotoUploadController][verifyImage()]");
			
			String imageHash = body.get("imageHash");
			
			logger.info("[PhotoUploadController][verifyImage()] imageHash: " + imageHash);
			
			if (imageHash == null || (!imageHash.startsWith("0x")) || (imageHash.length() != 66)) {
				response.setMessage("Invalid hash format");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			
			boolean exists = nftService.isImageHashExists(imageHash);
			
			logger.info("[PhotoUploadController][verifyImage()] isImageHashExists on Smart Contract: " + exists);
			
			if (!exists) {
				response.setExists(false);
				response.setVerified(false);
				response.setMessage("Image not found in NFT registry");
				return ResponseEntity.status(HttpStatus.OK).body(response);
			}
			
			// --- DIRECT BLOCKCHAIN LOOKUP ---
			//BigInteger tokenId = nftService.getTokenIdByHash(imageHash);
			
			VerificationDetails objVerificationDetails = nftService.verifyImageByHash(imageHash);
			
			if (objVerificationDetails != null) {
				logger.info("[PhotoUploadController][verifyImage()] objVerificationDetails is not null ...");
				
				if (objVerificationDetails.isExists()) {
					
					logger.info("[PhotoUploadController][verifyImage()] imageHash exists on contract");
					
					BigInteger tokenId = objVerificationDetails.getTokenId();
					
					logger.info("[PhotoUploadController][verifyImage()] tokenId: " + tokenId);
					
		            // The smart contract is expected to return 0 if the hash is registered but no token ID is mapped (e.g., hash exists but minting failed).
		            // More commonly, it returns 0 if the hash doesn't exist at all, but we already handled that with isImageHashExists.
					if (tokenId.compareTo(BigInteger.ZERO) > 0) {
						// We found the token ID directly from the blockchain!
						
						logger.info("[PhotoUploadController][verifyImage()] tokenId: " + tokenId + " is greater than zero ...");
						
						response.setExists(true);
						response.setVerified(true);
						response.setTokenId(tokenId.longValue());
						response.setMessage("Image found via direct blockchain lookup.");
						
						logger.info("[PhotoUploadController][verifyImage()] tokenId found on blockchain");
						
						return ResponseEntity.status(HttpStatus.OK).body(response);
					} else {
						
						logger.info("[PhotoUploadController][verifyImage()] tokenId: " + tokenId + " is NOT greater than zero ...");
						
						// This indicates the hash is in the contract registry (isImageHashExists was true)
		                // but the token ID lookup failed (returned 0). This is a contract inconsistency.
						response.setExists(true);
						response.setVerified(false);
						response.setMessage("Image hash exists on contract, but corresponding Token ID was not retrieved.");
						
						logger.info("[PhotoUploadController][verifyImage()] Image hash exists on contract, but corresponding Token ID was not retrieved.");
						
						return ResponseEntity.status(HttpStatus.OK).body(response);
					}
				} else {
					
					logger.info("[PhotoUploadController][verifyImage()] imageHash does not exist on contract");
					
					// This indicates the hash is in the contract registry (isImageHashExists was true)
	                // but the token ID lookup failed (returned 0). This is a contract inconsistency.
					response.setExists(false);
					response.setVerified(false);
					response.setMessage("Image hash exists on contract, but corresponding Token ID was not retrieved.");
					
					logger.info("[PhotoUploadController][verifyImage()] Image hash exists on contract, but corresponding Token ID was not retrieved.");
					
					return ResponseEntity.status(HttpStatus.OK).body(response);					
				}
				
			} else {
				logger.info("[PhotoUploadController][verifyImage()] objVerificationDetails is null");
				
				response.setExists(false);
				response.setVerified(false);
				response.setMessage("Image hash does not exist on contract.");
				
				logger.info("[PhotoUploadController][verifyImage()] Image hash exists on contract, but corresponding Token ID was not retrieved.");
				
				return ResponseEntity.status(HttpStatus.OK).body(response);					
			}
			
			
			

			
			/*
			 * BigInteger total = nftService.getTotalMinted(); for (BigInteger i =
			 * BigInteger.ONE; i.compareTo(total) <= 0; i = i.add(BigInteger.ONE)) { String
			 * tokenHash = nftService.getImageHash(i); if (tokenHash != null &&
			 * tokenHash.equalsIgnoreCase(imageHash)) { NFTMetadata metadata =
			 * nftService.getMetaData(i); response.setExists(true);
			 * response.setVerified(true); response.setTokenId(i.longValue()); if (metadata
			 * != null) { response.setIpfsCID(metadata.getIpfsCID());
			 * response.setDescription(metadata.getDescription());
			 * response.setMinter(metadata.getMinter());
			 * response.setTimestamp(metadata.getTimestamp());
			 * response.setImageHash(metadata.getImageHash()); } return
			 * ResponseEntity.status(HttpStatus.OK).body(response); } }
			 * 
			 * response.setExists(false); response.setVerified(false);
			 * response.setMessage("Hash exists but token not found"); return
			 * ResponseEntity.status(HttpStatus.OK).body(response);
			 */
			
		} catch (Exception ex) {
			response.setMessage("Verification failed: " + ex.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
		
	}
	
	// Simple extraction of "Hash" from IPFS response
	private String extractHashFromResponse(String responseBody) {
		// IPFS responnse with something like: {"Name": "photo.jpg", "Hash": "Qm...", "Size": "12345"}
		int hashIndex = responseBody.indexOf("\"Hash\":");
		if (hashIndex == -1) {
			return null;
		}
		int quoteStart = responseBody.indexOf("\"", hashIndex + 7);
		int quoteEnd = responseBody.indexOf("\"", quoteStart + 1);
		
		if (quoteStart == -1 || quoteEnd == -1) {
			return null;
		}
		
		return responseBody.substring(quoteStart + 1, quoteEnd);
	}
	
}