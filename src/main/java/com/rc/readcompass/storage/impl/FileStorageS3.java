package com.rc.readcompass.storage.impl;

import com.fasterxml.uuid.Generators;
import com.rc.readcompass.storage.BinaryContent;
import com.rc.readcompass.storage.FileStorage;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class FileStorageS3 implements FileStorage {

    private final S3Client s3Client;       // S3 업로드/삭제용
    private final S3Presigner s3Presigner; // Presigned URL 생성용

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.presigned-url-expiration:600}")
    private long presignedUrlExpiration; // Presigned URL 만료 시간 (초, 기본 10분)

    // S3 저장 경로 prefix
    private static final String PREFIX = "attachments";

    // 첨부파일 다건 저장
    @Override
    public List<BinaryContent> save(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        List<BinaryContent> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            result.add(saveOne(file));
        }
        return result;
    }

    // 단건 S3 업로드
    private BinaryContent saveOne(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";

        // UUID v7 기반 S3 키: attachments/UUIDv7.ext
        String renamed = Generators.timeBasedEpochGenerator().generate() + ext;
        String key = PREFIX + "/" + renamed;

        uploadToS3(key, file);
        log.info("첨부파일 S3 업로드 완료: {}", key);

        return BinaryContent.builder()
                .originFileUrl(originalName)
                .renamedFileUrl(renamed)  // DB에는 파일명만 저장 (PREFIX 제외)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    // 첨부파일 다건 삭제
    @Override
    public void delete(Collection<BinaryContent> files) {
        for (BinaryContent bc : files) {
            if (bc.getRenamedFileUrl() == null) continue;
            String key = PREFIX + "/" + bc.getRenamedFileUrl();
            deleteFromS3(key);
            log.info("첨부파일 S3 삭제 완료: {}", key);
        }
    }

    // Presigned URL 반환 (만료 시간 내 다운로드 가능)
    @Override
    public String getAttachFileUrl(String renamedFileName) {
        String key = PREFIX + "/" + renamedFileName;
        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                .getObjectRequest(getReq)
                .build();

        return s3Presigner.presignGetObject(presignReq).url().toExternalForm();
    }


    private void uploadToS3(String key, MultipartFile file) {
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putReq, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패: " + key, e);
        }
    }

    private void deleteFromS3(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            log.warn("S3 삭제 실패 (무시 가능): {}", key, e);
        }
    }
}
