package com.rc.readcompass.storage.impl;

import com.fasterxml.uuid.Generators;
import com.rc.readcompass.config.FileConfig;
import com.rc.readcompass.book.entity.BinaryContent;
import com.rc.readcompass.storage.FileStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class FileStorageDev implements FileStorage {

    private final FileConfig fileConfig;

    @Value("${server.port:8080}")
    private int serverPort;

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

    // 단건 저장
    private BinaryContent saveOne(MultipartFile file) {
        Path attachDir = fileConfig.getRootPath().resolve("attachments");

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";

        // UUID v7 기반 리네임 (시간순 정렬 가능)
        String renamed = Generators.timeBasedEpochGenerator().generate() + ext;
        Path dest = attachDir.resolve(renamed);

        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + dest.toAbsolutePath(), e);
        }

        return BinaryContent.builder()
                .originFileUrl(originalName)
                .renamedFileUrl(renamed)
                .size(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    // 첨부파일 다건 삭제
    @Override
    public void delete(Collection<BinaryContent> files) {
        Path attachDir = fileConfig.getRootPath().resolve("attachments");
        for (BinaryContent bc : files) {
            if (bc.getRenamedFileUrl() == null) continue;
            try {
                Files.deleteIfExists(attachDir.resolve(bc.getRenamedFileUrl()));
            } catch (IOException e) {
                throw new RuntimeException("파일 삭제 실패: " + bc.getRenamedFileUrl(), e);
            }
        }
    }

    // 로컬 파일 접근 URL → FileResourceConfig의 /files/** 핸들러와 매핑
    // request context 없을 때도 안전하게 동작 (스케줄러, 테스트 등)
    @Override
    public String getAttachFileUrl(String renamedFileName) {
        return "http://localhost:" + serverPort + "/files/attachments/" + renamedFileName;
    }
}
