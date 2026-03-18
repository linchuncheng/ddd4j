package com.ddd4j.cloud.oss.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.ddd4j.cloud.oss.contract.exception.OssException;
import com.ddd4j.cloud.oss.service.OssTemplate;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * oss操作模板实现类
 *
 * @author zhouhengzhe
 */
@Slf4j
@RequiredArgsConstructor
public class OssTemplateImpl implements OssTemplate {

    private final AmazonS3 amazonS3;


    /**
     * 创建bucket
     * AmazonS3：https://docs.aws.amazon.com/AmazonS3/latest/API/API_CreateBucket.html
     *
     * @param bucketName bucket名称
     */
    @Override
    public void createBucket(String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            Bucket bucket = amazonS3.createBucket((bucketName));
            log.info("bucket创建成功：{}", bucket.getName());
        }
    }

    /**
     * 获取所有的bucket
     *
     * @return
     */
    @Override
    public List<Bucket> getAllBuckets() {
        return amazonS3.listBuckets();
    }

    /**
     * 通过bucket名称删除bucket
     *
     * @param bucketName
     */
    @Override
    public void removeBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }

    /**
     * 上传文件
     *
     * @param bucketName  bucket名称
     * @param objectName  文件名称
     * @param stream      文件流
     * @param contextType 文件类型
     */
    @Override
    public void putObject(String bucketName, String objectName, InputStream stream, String contextType) {
        try {
            putObject(bucketName, objectName, stream, stream.available(), contextType);
        } catch (Exception e) {
            throw new OssException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param stream     文件流
     */
    @Override
    public void putObject(String bucketName, String objectName, InputStream stream) {
        try {
            putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
        } catch (Exception e) {
            throw new OssException(e);
        }
    }

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return S3Object
     */
    @Override
    public S3Object getObject(String bucketName, String objectName) {
        return amazonS3.getObject(bucketName, objectName);
    }

    /**
     * 获取对象的url
     *
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    @Override
    public String getObjectUrl(String bucketName, String objectName, Integer expires) {
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, expires);
        URL url = amazonS3.generatePresignedUrl(bucketName, objectName, calendar.getTime());
        return url.toString();
    }

    /**
     * 通过bucketName和objectName删除对象
     *
     * @param bucketName
     * @param objectName
     */
    @Override
    public void removeObject(String bucketName, String objectName) {
        amazonS3.deleteObject(bucketName, objectName);
    }

    /**
     * 根据文件前置查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @param recursive  是否递归查询
     * @return S3ObjectSummary 列表
     */
    @Override
    public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix, boolean recursive) {
        ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);
        return objectListing.getObjectSummaries();
    }

    /**
     * 上传文件
     *
     * @param bucketName
     * @param objectName
     * @param stream
     * @param size
     * @param contextType
     * @return
     */
    @SneakyThrows
    private PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size,
                                      String contextType) {

        byte[] bytes = IOUtils.toByteArray(stream);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(size);
        objectMetadata.setContentType(contextType);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        // 上传
        return amazonS3.putObject(bucketName, objectName, byteArrayInputStream, objectMetadata);

    }
}
