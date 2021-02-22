package app.services;

import java.util.List;

import com.amazonaws.services.s3.model.S3Object;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonS3ClientService
{
    void uploadFileToS3Bucket(MultipartFile multipartFile,String filePath, boolean enablePublicReadAccess);

    void deleteFileFromS3Bucket(String fileName);

    S3Object getFileFromS3Bucket(String filename);
    
    List<String> getFileListFromFolder( String folderKey);

}
