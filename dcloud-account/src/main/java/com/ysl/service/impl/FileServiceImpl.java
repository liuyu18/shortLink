package com.ysl.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSSClientBuilder;
import com.ysl.config.OSSConfig;
import com.ysl.service.FileService;
import com.ysl.util.CommonUtil;
import com.ysl.util.LogUtil;

import groovy.util.logging.Slf4j;
import io.netty.handler.codec.DateFormatter;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    private OSSConfig ossConfig;

    FileServiceImpl(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    @Override
    public String uploadUserImg(MultipartFile file) {
        String bucketName = ossConfig.getBucketname();
        String endpoint = ossConfig.getEndpoint();
        String accessKeyId = ossConfig.getAccessKeyId();
        String accessKeySecret = ossConfig.getAccessKeySecert();

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        String originalFilename = file.getOriginalFilename();

        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        String folder = pattern.format(ldt);
        String fileName = CommonUtil.generateUUID();
        String extendsion = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "user/" + folder + "/" + fileName + extendsion;

        try {
            PutObjectResult putObjectResult = ossClient.putObject(bucketName, newFilename, file.getInputStream());
            if (putObjectResult != null) {
                String imgUrl = "https://" + bucketName + "." + endpoint + "/" + newFilename;
                return imgUrl;
            }
        } catch (IOException e) {
            LogUtil.error("文件上传失败:{}", e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return null;
    }

}
