package edu.aut.comp726.webservice;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
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
	
	public PhotoUploadController() {
		logger.info("PhotoUploadController initialized (SLF4J)");
		log4j2Logger.info("PhotoUploadController initialized (Log4j2)");
	}
	
	@GetMapping("/greeting")
	public ResponseEntity<String> helloWorld() {
		logger.info("Inside helloWorld() method ...");
		return new ResponseEntity<String>("Hello World", HttpStatus.OK);
	}
	
	@CrossOrigin(origins = "http://localhost:5173")
	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam("file") MultipartFile file) {
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