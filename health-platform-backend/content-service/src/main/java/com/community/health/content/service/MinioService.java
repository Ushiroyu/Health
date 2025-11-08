package com.community.health.content.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MinioService {
  private final MinioClient client;
  @Value("${minio.external-url}") private String external;

  public MinioService(@Value("${minio.endpoint}") String endpoint,
                      @Value("${minio.access-key}") String ak,
                      @Value("${minio.secret-key}") String sk) {
    this.client = MinioClient.builder().endpoint(endpoint).credentials(ak, sk).build();
  }

  private void ensureBucket(String bucket) throws Exception {
    boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    if (!exists) {
      client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
  }

  public String putObject(String bucket, String objectName, MultipartFile file) throws Exception {
    ensureBucket(bucket);
    client.putObject(PutObjectArgs.builder()
      .bucket(bucket)
      .object(objectName)
      .stream(file.getInputStream(), file.getSize(), -1)
      .contentType(file.getContentType())
      .build());
    return external + "/" + bucket + "/" + objectName;
  }

  public String presignGet(String bucket, String objectName, int expirySeconds) throws Exception {
    return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
      .method(Method.GET)
      .bucket(bucket)
      .object(objectName)
      .expiry(expirySeconds)
      .build());
  }
}
