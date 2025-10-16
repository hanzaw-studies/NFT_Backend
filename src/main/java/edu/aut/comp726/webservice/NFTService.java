package edu.aut.comp726.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.utils.Numeric;

import edu.aut.comp726.webservice.data.ImageNFTContractInfo;
import edu.aut.comp726.webservice.data.NFTMetadata;

import java.math.BigInteger;
import java.util.*;

@Service
public class NFTService {
	
	@Autowired
	private Web3j web3j;
	
	@Autowired
	private ImageNFTContractInfo imageNFTContractInfo;
	
	public boolean isImageHashExists(String imageHash) throws Exception {
		// use the new contract function name: isImageHashExists
		Function function = new Function("isImageHashExists",
				List.of(new Bytes32(Numeric.hexStringToByteArray(imageHash))),
				List.of(new TypeReference<Bool>() {})
		);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		String response = web3j.ethCall(Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		List<Type> results = FunctionReturnDecoder.decode(response, function.getOutputParameters());
		
		return ((!results.isEmpty()) && ((Bool) results.get(0)).getValue());
	}
	
	public BigInteger totalSupply() throws Exception {
		Function function = new Function("totalSupply",
				List.of(),
				List.of(new TypeReference<Uint256>() {})
		);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		
		String response = web3j.ethCall(
				Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		List<Type> results = FunctionReturnDecoder.decode(response, function.getOutputParameters());
		
		return (results.isEmpty() ? BigInteger.ZERO : ((Uint256) results.get(0)).getValue());
	}
	
	/*
	public boolean isImageMinted(String imageHash) throws Exception {
		Function function = new Function("isImageMinted", 
				List.of(new Bytes32(Numeric.hexStringToByteArray(imageHash))), 
				List.of(new TypeReference<Bool>() {})
		);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		String response = web3j.ethCall(
				Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		List<Type> results = FunctionReturnDecoder.decode(response, function.getOutputParameters());
		
		return ((!results.isEmpty()) && ((Bool) results.get(0)).getValue());
	}
	*/
	
	/*
	public BigInteger getTotalMinted() throws Exception {
		Function function = new Function(
				"getTotalMinted",
				List.of(),
				List.of(new TypeReference<Uint256>() {})
				);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		String response = web3j.ethCall(
				Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		List<Type> results = FunctionReturnDecoder.decode(response, function.getOutputParameters());
		return (results.isEmpty() ? BigInteger.ZERO : ((Uint256) results.get(0)).getValue());
	}
	*/
	
	public String getImageHash(BigInteger tokenId) throws Exception {
		Function function = new Function(
				"getImageHash",
				List.of(new Uint256(tokenId)),
				List.of(new TypeReference<Bytes32>() {})
				);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		String response = web3j.ethCall(
				Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		List<Type> results = FunctionReturnDecoder.decode(response, function.getOutputParameters());
		return results.isEmpty() ? null : Numeric.toHexString(((Bytes32) results.get(0)).getValue());
	}
	
	/*
	public NFTMetadata getMetaData(BigInteger tokenId) throws Exception {
		Function function = new Function(
				"getMetadata",
				List.of(new Uint256(tokenId)),
				List.of(new TypeReference<DynamicStruct>() {})
		);
		
		String contractAddress = imageNFTContractInfo.getContractAddress();
		
		String encoded = FunctionEncoder.encode(function);
		String response = web3j.ethCall(
				Transaction.createEthCallTransaction(null, contractAddress, encoded), 
				DefaultBlockParameterName.LATEST).send().getValue();
		
		if (response == null || response.equals("0x")) {
			return null;
		}
		
        // Decoding struct manually (simplified)
        // You might want to use autogenerated contract wrappers with web3j codegen for reliability
        // For now, assume you decode based on ABI order
        // tokenId, ipfsCID, imageHash, description, minter, timestamp
		
		List<Type> decoded = FunctionReturnDecoder.decode(
			    response,
			    (List<TypeReference<Type>>) (List<?>) List.of(new TypeReference<DynamicStruct>() {})
			);
		
		if (decoded.isEmpty()) {
			return null;
		}
		
		// Handle actual tuple manually or use web3j’s generated wrapper
		NFTMetadata metadata = new NFTMetadata();
		metadata.setTokenId(tokenId.longValue());
		
		return metadata;
	}
	*/
	
}
