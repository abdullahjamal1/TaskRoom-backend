package app.services;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.simpleworkflow.flow.annotations.Asynchronous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@EnableAsync
@Component
public class AwsS3Service implements AmazonS3ClientService {

    private String awsS3AudioBucket;
    private AmazonS3 s3Client;
    private static final Logger logger = LoggerFactory.getLogger(AwsS3Service.class);

    @Autowired
    public AwsS3Service(Region awsRegion, AWSCredentialsProvider awsCredentialsProvider, String awsS3AudioBucket) 
    {
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion(awsRegion.getName()).build();
        this.awsS3AudioBucket = awsS3AudioBucket;
    }

    @Async
    public void uploadFileToS3Bucket(MultipartFile multipartFile, String fileName, boolean enablePublicReadAccess) 
    {

        try {
            //creating the file in the server (temporarily)
            File file = new File(multipartFile.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(multipartFile.getBytes());
            fos.close();

            PutObjectRequest putObjectRequest = new PutObjectRequest(awsS3AudioBucket, fileName, file);

            if (enablePublicReadAccess) {
                putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);
            }
            this.s3Client.putObject(putObjectRequest);
            //removing the file created in the server
            file.delete();
        } catch (IOException | AmazonServiceException ex) {
            logger.error("error [" + ex.getMessage() + "] occurred while uploading [" + fileName + "] ");
        }
    }


    public S3Object getFileFromS3Bucket(String filename){
        S3Object object = s3Client.getObject(awsS3AudioBucket, filename);
        return object;
    }

    @Async
    public void deleteFileFromS3Bucket(String fileName) 
    {
        try {
            s3Client.deleteObject(new DeleteObjectRequest(awsS3AudioBucket, fileName));
        } catch (AmazonServiceException ex) {
            logger.error("error [" + ex.getMessage() + "] occurred while removing [" + fileName + "] ");
        }
    }

    // public void getAllFiles(String bucketName, String folderKey){

    //     List<String> list = getFileListFromFolder(bucketName, folderKey);
    //     for(String file : list){
    //         BytesArrayResource
    //     }
    // }

    public List<String> getFileListFromFolder(String folderKey) {
   
        ListObjectsRequest listObjectsRequest = 
                                      new ListObjectsRequest()
                                            .withBucketName(awsS3AudioBucket)
                                            .withPrefix(folderKey + "/");
       
        List<String> keys = new ArrayList<>();
       
        ObjectListing objects = s3Client.listObjects(listObjectsRequest);

        for (;;) {
          List<S3ObjectSummary> summaries = objects.getObjectSummaries();
          if (summaries.size() < 1) {
            break;
          }
          summaries.forEach(s -> keys.add(s.getKey()));
          objects = s3Client.listNextBatchOfObjects(objects);
        }
       
        return keys;
      }

}