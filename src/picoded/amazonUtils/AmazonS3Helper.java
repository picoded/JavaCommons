package picoded.amazonUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

public class AmazonS3Helper {
	
	private AmazonS3Client s3Client;
	
	public AmazonS3Helper(String accessKey, String secretKey){
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
		s3Client = new AmazonS3Client(awsCreds);
		
		if(s3Client == null){
			System.out.println("Unable to initalise S3Client instance");
		}
	}
	
	public void putFile(String bucketName, String inFileKeyName, File inFile) throws AmazonServiceException, AmazonClientException, Exception{
		s3Client.putObject(bucketName, inFileKeyName, inFile);
	}
	
	public void putFile(String bucketName, String inFileKeyName, InputStream inStream, long contentLength, Map<String, String> metadataMap) throws AmazonServiceException, AmazonClientException, Exception{
		if(inStream == null){
			System.out.println("Input stream is null");
			return;
		}
		
		ObjectMetadata metadata = new ObjectMetadata();
		if(metadataMap != null){
			for(String key : metadataMap.keySet()){
				metadata.addUserMetadata(key, metadataMap.get(key));
			}
		}
		
		metadata.setContentLength(contentLength);
		
		s3Client.putObject(bucketName, inFileKeyName, inStream, metadata);
	}
}