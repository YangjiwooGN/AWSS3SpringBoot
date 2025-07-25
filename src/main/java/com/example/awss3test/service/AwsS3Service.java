package com.example.awss3test.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.awss3test.utils.CommonUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

// 로그 기록을 위한 객체를 자동으로 생성해주는 어노테이션
@Slf4j
// 클래스 안에 인스턴스 변수가 존재하는 경우 생성자를 이용해서 주입받는 생성자를 생성해주는 어노테이션
@RequiredArgsConstructor
// 서비스 클래스를 명시하는 어노테이션인데 실제 기능은 싱글톤 패턴으로 인스턴스를 생성해주는 역할
// Service 대신 Component로 해도 됨
@Service
public class AwsS3Service {
    private AmazonS3 s3Client;

    // application.properties 파일에서 값을 찾아와서 대입하는 것
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    // 생성자가 동작한 이후에 호출되는 메서드
    @PostConstruct
    public void setS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    // 파일 검증을 위한 메서드
    private boolean validateFileExists(MultipartFile file) {
        boolean result = true;
        if(file.isEmpty()) {
            result = false;
        }
        return result;
    }

    // 파일 업로드를 위한 메서드 생성
    public String uploadFileV1(String category, MultipartFile multipartFile) throws IOException {
        boolean result = validateFileExists(multipartFile);
        if(!result) {
            return null;
        }
        String fileName = CommonUtils.buildFileName(category, multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        try(InputStream inputStream = multipartFile.getInputStream()){
            // 업로드
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        }catch (IOException e){
            return null;
        }
        return s3Client.getUrl(bucketName, fileName).toString();
    }

    // 다운로드에 사용할 메서드
    // 파일의 존재 여부 확인 메서드
    private boolean validateFileExistsAtUrl(String resourcePath){
        boolean result = true;
        if(!s3Client.doesObjectExist(bucketName, resourcePath)) {
            result = false;
        }
        return result;
    }

    // 다운로드할 파일 가져오는 메서드
    public byte[] downloadFileV1(String resourcePath) throws IOException {
        boolean result = validateFileExistsAtUrl(resourcePath);
        if(!result) {
            return null;
        }
        S3Object object = s3Client.getObject(bucketName, resourcePath);
        S3ObjectInputStream inputStream = object.getObjectContent();
        try{
            return IOUtils.toByteArray(inputStream);
        }catch (IOException e){
            return null;
        }
    }
}