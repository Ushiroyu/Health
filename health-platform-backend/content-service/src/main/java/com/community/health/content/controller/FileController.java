package com.community.health.content.controller;

import com.community.health.common.api.ApiResponse;
import com.community.health.content.service.MinioService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class FileController {
  private final MinioService minio;
  public FileController(MinioService s){ this.minio = s; }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<Map<String,String>> upload(@RequestParam("file") MultipartFile file) throws Exception {
    // 简单校验：类型与大小（可扩展为配置化）
    Set<String> allow = Set.of("image/png","image/jpeg","image/jpg","image/gif","application/pdf");
    if (file.getSize() <= 0 || file.getSize() > 10 * 1024 * 1024) {
      return ApiResponse.error("文件大小不合法（最大10MB）");
    }
    String contentType = file.getContentType();
    if (contentType == null || !allow.contains(contentType)) {
      return ApiResponse.error("不支持的文件类型");
    }
    String original = file.getOriginalFilename();
    String ext = "";
    if (original != null && original.contains(".")) {
      ext = original.substring(original.lastIndexOf('.'));
    }
    String objectName = UUID.randomUUID().toString().replace("-","") + ext;
    String url = minio.putObject("health-bucket", objectName, file);
    String presigned = minio.presignGet("health-bucket", objectName, 3600);
    return ApiResponse.ok(Map.of("url", url, "object", objectName, "presignedUrl", presigned));
  }

  @GetMapping("/presign")
  public ApiResponse<Map<String,String>> presign(@RequestParam("object") String object) throws Exception {
    String presigned = minio.presignGet("health-bucket", object, 3600);
    return ApiResponse.ok(Map.of("object", object, "presignedUrl", presigned));
  }
}
