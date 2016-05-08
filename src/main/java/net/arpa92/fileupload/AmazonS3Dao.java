package net.arpa92.fileupload;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AmazonS3Dao {
	
	private String accessKey;
	private String secretKey;
	private String bucket;
	
	private String remoteFileSeparator = "/";
	
	private String baseUrl =  "https://s3.amazonaws.com/"; //bookingable/


	AmazonS3 connection;
	
	public AmazonS3Dao(String accessKey, String secretKey, String bucket){
		System.out.println("======================================AmazonS3Dao init");

		//this.urlPrefix = "";

		this.accessKey = accessKey;
		this.secretKey  = secretKey;
		this.bucket = bucket;

		//System.out.println(this.accessKey + " " + this.secretKey);

		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.connection = new AmazonS3Client(credentials);
	}
	
	public String getAccessKey() {
		return accessKey;
	}

	public AmazonS3 getConnection() {

		return connection;
	}

	public void setConnection(AmazonS3 connection) {
		this.connection = connection;
	}



	public String getBucket() {
		return bucket;
	}


	public void setBucket(String bucket) {
		this.bucket = bucket;
	}


	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	
	@Async
	public void saveAsync(File[] files, String uploadDirName) {

		String threadName = Thread.currentThread().getName(); 
		
		try {
			
			if(uploadDirName==null) uploadDirName= "" + remoteFileSeparator;
			if(!uploadDirName.endsWith(remoteFileSeparator)) uploadDirName += remoteFileSeparator;
			
			

			for(File file : files){
				
				String uploadFileName =  uploadDirName + file.getName();
				
				/*
				// controllo se il file esite gia' su s3
				boolean exist = exist(uploadFileName);

				//System.out.println("***Uploading to S3 file " +uploadFileName + " exists? " + exist);

				if(exist){
					System.out.println("   " + threadName + " completed work on " + arg.getCanonicalPath());
					return;
				}
				 */
				
				
				PutObjectRequest putObj = new PutObjectRequest(this.bucket,uploadFileName , file);
				putObj.setCannedAcl(CannedAccessControlList.PublicRead);
				connection.putObject(putObj); 
				System.out.println("   " + threadName + " completed upload on " + uploadFileName);
				
				file.delete();
	
			}
			
			
			return;

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " +
					"means your request made it " +
					"to Amazon S3, but was rejected with an error response" +
					" for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " +
					"means the client encountered " +
					"an internal error while trying to " +
					"communicate with S3, " +
					"such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		} 


		return;
	}
	
	public LinkedList<FileMeta>  getList(String path){
		
		LinkedList<FileMeta> results = new LinkedList<FileMeta>();
		
		ListObjectsRequest listObjectRequest = new ListObjectsRequest().
				withBucketName(this.bucket).
				withPrefix(path).
				withDelimiter(this.remoteFileSeparator);

		ObjectListing current = connection.listObjects(listObjectRequest);

		System.out.println("====================== " +"DaoFileS3 getItemList connecting to remote storage key:" + path);


		List<S3ObjectSummary> keyList = current.getObjectSummaries();
		List<String> commonPrefixes = current.getCommonPrefixes();


		ObjectListing next = connection.listNextBatchOfObjects(current);
		keyList.addAll(next.getObjectSummaries());



		while (next.isTruncated()) {
			current=connection.listNextBatchOfObjects(next);
			keyList.addAll(current.getObjectSummaries());
			commonPrefixes.addAll(current.getCommonPrefixes());
			next =connection.listNextBatchOfObjects(current);
		}

		keyList.addAll(next.getObjectSummaries());
		
		
		for (String s : commonPrefixes){
			
			System.out.println("dir: " + s);
			
			FileMeta bean  = new FileMeta();
			bean.setBytes(s.getBytes());
			bean.setFileName(s);
			bean.setFileSize("0");
			bean.setFileType("dir");
			
			results.add(bean);
			
			//Item bean = buildItem(s);
			//if(bean != null) list.add(bean);
			
		}



		// gestione file
		for(S3ObjectSummary item:  keyList){

			System.out.println("file: " + item.getKey() + "\t" + item.getSize());
			
			FileMeta bean  = getFileMeta(item);

			/*
			bean.setFileName(item.getKey());
			bean.setFileSize(item.getSize()+"");
			bean.setFileType("image/jpg");
			*/
			
			/// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
			//System.out.println("FileMeta: " + bean.getFileName() + "\t" + bean.getFileSize() + "\t" + bean.getFileType());
			
			results.add(bean);
			
			/*
			Item bean = buildItem(item);

			if(bean!=null && !item.getKey().endsWith(FS)) {
				boolean add = true;
				
				if(imagesOnly==true && bean.isImage()==false){
					add=false;
				}
				
				if(add==true) {
					list.add(bean);
				}
			}
			*/
		}
		
		return results;

		
		
	}
	
	public FileMeta getFileMeta(S3ObjectSummary item){
		
		FileMeta fileMeta = new FileMeta();
		
		String fileName = item.getKey();
		
		if(fileName != null && fileName.indexOf(this.remoteFileSeparator) >-1){
			fileName = fileName.substring(fileName.lastIndexOf(this.remoteFileSeparator)+1);
			//System.out.println(fileName + "\t< " +   " " + item.getBucketName() );
		}
		
		fileMeta.setFileName(fileName);
		fileMeta.setFileSize(item.getSize()+"");
		fileMeta.setFileType("image/jpg");
		fileMeta.setFileUrl(this.baseUrl + item.getBucketName() + this.remoteFileSeparator + item.getKey());
		
		return fileMeta;
		
	}

}
